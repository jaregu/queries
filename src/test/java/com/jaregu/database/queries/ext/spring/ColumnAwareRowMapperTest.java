package com.jaregu.database.queries.ext.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Optional;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.Queries;

/**
 * Proves that {@link SpringColumnAwareRowMapper} honours
 * {@link com.jaregu.database.queries.annotation.Column} annotations on the
 * read path. The {@link ColumnAwareUser} fixture deliberately uses column
 * names that diverge from Spring's standard snake_case → camelCase
 * derivation, so a passing test confirms the {@code @Column} mapping is in
 * effect — not the snake_case fallback.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ColumnAwareRowMapperTest {

	private JDBCDataSource dataSource;
	private ColumnAwareDAO dao;

	@BeforeAll
	void initialise() {
		dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:column-aware-test");
		dataSource.setUser("SA");
		dataSource.setPassword("");

		JdbcClient jdbcClient = JdbcClient.create(dataSource);

		Queries queries = SpringQueriesMappers.register(
				Queries.builder().sourceOfClass(ColumnAwareDAO.class),
				jdbcClient
		).build();

		dao = queries.proxy(ColumnAwareDAO.class);
	}

	@BeforeEach
	void resetTable() {
		dao.dropTable();
		dao.createTable();
	}

	@AfterAll
	void shutdown() {
		dataSource.setUrl("jdbc:hsqldb:mem:column-aware-test;shutdown=true");
		try (var ignored = dataSource.getConnection()) {
			// best-effort shutdown
		} catch (Exception ignored) {
		}
	}

	@Test
	void columnAnnotationsDriveTheReverseMapping() {
		// All four columns diverge from snake_case-to-camelCase derivation.
		// Without @Column support, every field would come back null.
		dao.insert(new ColumnAwareUser(1, "Alice", "Anderson", 30));
		dao.insert(new ColumnAwareUser(2, "Bob", "Brown", null));

		List<ColumnAwareUser> all = dao.findAll();
		assertThat(all)
				.extracting(
						ColumnAwareUser::getId,
						ColumnAwareUser::getFirstName,
						ColumnAwareUser::getLastName,
						ColumnAwareUser::getAge)
				.containsExactly(
						tuple(1, "Alice", "Anderson", 30),
						tuple(2, "Bob", "Brown", null));

		Optional<ColumnAwareUser> alice = dao.findById(1);
		assertThat(alice).isPresent();
		assertThat(alice.get().getFirstName()).isEqualTo("Alice");
		assertThat(alice.get().getLastName()).isEqualTo("Anderson");
		assertThat(alice.get().getAge()).isEqualTo(30);

		Optional<ColumnAwareUser> missing = dao.findById(424242);
		assertThat(missing).isEmpty();
	}
}
