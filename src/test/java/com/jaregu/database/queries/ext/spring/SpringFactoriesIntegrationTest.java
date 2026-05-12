package com.jaregu.database.queries.ext.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.Queries;

/**
 * Integration test for the four Spring-backed
 * {@link com.jaregu.database.queries.proxy.QueryMapperFactory} implementations
 * — runs every operation against an in-memory HSQL database through a real
 * {@link JdbcClient}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringFactoriesIntegrationTest {

	private JDBCDataSource dataSource;
	private SpringTestDAO dao;

	@BeforeAll
	void initialise() {
		dataSource = new JDBCDataSource();
		// No shutdown=true — HSQL would wipe the in-memory DB between connections
		// (JDBCDataSource doesn't pool), losing the table before the next operation.
		dataSource.setUrl("jdbc:hsqldb:mem:spring-factories-test");
		dataSource.setUser("SA");
		dataSource.setPassword("");

		JdbcClient jdbcClient = JdbcClient.create(dataSource);

		Queries queries = SpringQueriesConfigurer
				.configure(Queries.builder().sourceOfClass(SpringTestDAO.class), jdbcClient)
				.build();

		dao = queries.proxy(SpringTestDAO.class);
	}

	@BeforeEach
	void resetTable() {
		dao.dropTable();
		dao.createTable();
	}

	@AfterAll
	void shutdown() {
		// Trigger HSQL shutdown so the in-memory DB releases its resources at
		// the end of the test class — opening one final connection with the
		// shutdown=true flag is the documented way to do this.
		dataSource.setUrl("jdbc:hsqldb:mem:spring-factories-test;shutdown=true");
		try (var ignored = dataSource.getConnection()) {
			// no-op; opening + closing with shutdown=true is the signal
		} catch (Exception ignored) {
			// shutdown is best-effort
		}
	}

	@Test
	void crudLifecycle() {
		// @ExecuteUpdate(unique = true)
		dao.insert(new SpringTestItem(1, "first", "desc 1"));
		dao.insert(new SpringTestItem(2, "second", null));

		// @FindUnique with Integer row class
		assertThat(dao.count()).isEqualTo(2);

		// @FindAll — list of records via SimplePropertyRowMapper
		List<SpringTestItem> all = dao.findAll();
		assertThat(all).containsExactly(
				new SpringTestItem(1, "first", "desc 1"),
				new SpringTestItem(2, "second", null));

		// @FindUnique with row class
		assertThat(dao.getById(1)).isEqualTo(new SpringTestItem(1, "first", "desc 1"));

		// @FindOptional — present and absent
		assertThat(dao.findById(2)).contains(new SpringTestItem(2, "second", null));
		assertThat(dao.findById(99)).isEmpty();

		// @ExecuteUpdate(unique = true) — update path
		dao.updateName(1, "renamed");
		assertThat(dao.getById(1).name()).isEqualTo("renamed");

		// @ExecuteUpdate(unique = true) — delete path
		dao.delete(2);
		assertThat(dao.count()).isEqualTo(1);
	}

	@Test
	void uniqueUpdateThrowsWhenNoRowsAffected() {
		assertThatThrownBy(() -> dao.delete(424242))
				.isInstanceOf(IncorrectResultSizeDataAccessException.class);
	}

	@Test
	void findUniqueThrowsWhenMissing() {
		assertThatThrownBy(() -> dao.getById(424242))
				.isInstanceOf(IncorrectResultSizeDataAccessException.class);
	}
}
