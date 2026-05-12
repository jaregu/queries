package com.jaregu.database.queries.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.QueriesConfigurator;
import com.jaregu.database.queries.parsing.QueriesSource;

/**
 * End-to-end check that:
 * <ul>
 *   <li>the auto-configuration's {@code AutoConfiguration.imports} entry is
 *       picked up by Spring Boot;</li>
 *   <li>{@link JdbcClient} and {@link Queries} beans are created on top of the
 *       Boot-auto-configured HSQL {@code DataSource};</li>
 *   <li>{@link QueriesScan} discovers {@link StarterTestDAO} and registers it
 *       as an injectable Spring bean;</li>
 *   <li>the Spring-backed mapper factories execute CRUD against a real DB.</li>
 * </ul>
 */
class QueriesAutoConfigurationTest {

	@SpringBootApplication
	@QueriesScan(basePackageClasses = StarterTestDAO.class)
	static class TestApp {
	}

	@Test
	void contextLoadsAndDaoIsInjectableAndExecutes() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			assertThat(ctx.getBean(Queries.class)).isNotNull();
			assertThat(ctx.getBean(JdbcClient.class)).isNotNull();

			StarterTestDAO dao = ctx.getBean(StarterTestDAO.class);
			dao.createTable();
			dao.insert(new StarterItem(1, "alpha"));
			dao.insert(new StarterItem(2, "beta"));

			assertThat(dao.count()).isEqualTo(2);
			assertThat(dao.findAll()).containsExactly(
					new StarterItem(1, "alpha"),
					new StarterItem(2, "beta"));
		} finally {
			ctx.close();
		}
	}

	@Test
	void scannedEntityIsRegisteredAsQueriesEntityBean() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-entity-scan-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			// @Table-annotated StarterEntity sits in the scanned package; the
			// registrar must have registered a QueriesEntity bean for it with
			// alias defaulted to the simple class name.
			QueriesEntity[] entities = ctx.getBeanProvider(QueriesEntity.class).stream()
					.toArray(QueriesEntity[]::new);
			assertThat(entities)
					.extracting(QueriesEntity::entityClass, QueriesEntity::alias)
					.contains(tuple(StarterEntity.class, "StarterEntity"));
		} finally {
			ctx.close();
		}
	}

	@Test
	void explicitlyDeclaredQueriesEntityBeanIsPickedUp() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ExplicitEntityApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-entity-explicit-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			QueriesEntity[] entities = ctx.getBeanProvider(QueriesEntity.class).stream()
					.toArray(QueriesEntity[]::new);
			// One scanned (StarterEntity) + one explicit (StarterItem with custom alias).
			assertThat(entities)
					.extracting(QueriesEntity::entityClass, QueriesEntity::alias)
					.contains(
							tuple(StarterEntity.class, "StarterEntity"),
							tuple(StarterItem.class, "item"));
		} finally {
			ctx.close();
		}
	}

	@SpringBootApplication
	@QueriesScan(basePackageClasses = StarterTestDAO.class)
	static class ExplicitEntityApp {

		@Bean
		QueriesEntity itemEntity() {
			return QueriesEntity.of(StarterItem.class, "item");
		}
	}

	@Test
	void pageableAndOrderableConvertersDriveOrderByLimitOffset() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-paged-sorted-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			StarterTestDAO dao = ctx.getBean(StarterTestDAO.class);
			dao.createTable();
			dao.insert(new StarterItem(1, "alpha"));
			dao.insert(new StarterItem(2, "bravo"));
			dao.insert(new StarterItem(3, "charlie"));
			dao.insert(new StarterItem(4, "delta"));
			dao.insert(new StarterItem(5, "echo"));

			// No paging, ascending by label — verifies @QueryRef(toSorted=true)
			// applies an ORDER BY derived from OrderableSearch.getOrderBy().
			List<StarterItem> ascending = dao.searchPaged(
					StarterSearch.unconstrained().asc("label"));
			assertThat(ascending).extracting(StarterItem::label)
					.containsExactly("alpha", "bravo", "charlie", "delta", "echo");

			// Descending by label — verifies the "label DESC" suffix wiring.
			List<StarterItem> descending = dao.searchPaged(
					StarterSearch.unconstrained().desc("label"));
			assertThat(descending).extracting(StarterItem::label)
					.containsExactly("echo", "delta", "charlie", "bravo", "alpha");

			// First page (limit 2 from offset 0), ascending — @QueryRef(toPaged=true)
			// must apply LIMIT/OFFSET derived from PageableSearch.
			List<StarterItem> page1 = dao.searchPaged(
					StarterSearch.unconstrained().asc("id").withOffset(0).withLimit(2));
			assertThat(page1).extracting(StarterItem::id).containsExactly(1, 2);

			// Second page picks up the middle of the range.
			List<StarterItem> page2 = dao.searchPaged(
					StarterSearch.unconstrained().asc("id").withOffset(2).withLimit(2));
			assertThat(page2).extracting(StarterItem::id).containsExactly(3, 4);

			// Last (partial) page — only one row at offset 4 with limit 2.
			List<StarterItem> page3 = dao.searchPaged(
					StarterSearch.unconstrained().asc("id").withOffset(4).withLimit(2));
			assertThat(page3).extracting(StarterItem::id).containsExactly(5);

			// Combined: descending order + paging — exercises both converters
			// stacked, in the order the proxy registers them.
			List<StarterItem> combined = dao.searchPaged(
					StarterSearch.unconstrained().desc("id").withOffset(1).withLimit(2));
			assertThat(combined).extracting(StarterItem::id).containsExactly(4, 3);
		} finally {
			ctx.close();
		}
	}

	@Test
	void entityFieldGeneratorMacroExpandsAndQueryExecutes() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-entity-fields-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			// The DAO's SQL file uses entityFieldGenerator(...) macros that
			// resolve `entityClass = 'StarterEntity'` against the QueriesEntity
			// bean the scanner registered. If any link in the chain (scan →
			// bean → builder.entity → EntityFieldsFeature) is broken, the
			// query won't compile and this test fails loudly.
			StarterEntityDAO dao = ctx.getBean(StarterEntityDAO.class);
			dao.dropTable();
			dao.createTable();

			StarterEntity first = new StarterEntity();
			first.setId(1);
			first.setName("alpha");
			dao.insert(first);

			StarterEntity second = new StarterEntity();
			second.setId(2);
			second.setName("beta");
			dao.insert(second);

			List<StarterEntity> all = dao.findAll();
			assertThat(all)
					.extracting(StarterEntity::getId, StarterEntity::getName)
					.containsExactly(
							tuple(1, "alpha"),
							tuple(2, "beta"));

			// Exercise the columnAndValue template via the update query.
			first.setName("alpha-renamed");
			dao.update(first);
			assertThat(dao.findAll())
					.extracting(StarterEntity::getId, StarterEntity::getName)
					.containsExactly(
							tuple(1, "alpha-renamed"),
							tuple(2, "beta"));
		} finally {
			ctx.close();
		}
	}

	@Test
	void configuratorHookRuns() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ConfiguratorApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-configurator-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			RecordingConfigurator recording = ctx.getBean(RecordingConfigurator.class);
			assertThat(recording.invoked).isTrue();
		} finally {
			ctx.close();
		}
	}

	@SpringBootApplication
	@QueriesScan(basePackageClasses = StarterTestDAO.class)
	static class ConfiguratorApp {

		@Bean
		RecordingConfigurator recordingConfigurator() {
			return new RecordingConfigurator();
		}
	}

	static class RecordingConfigurator implements QueriesConfigurator {
		volatile boolean invoked;

		@Override
		public void configure(Queries.Builder builder) {
			invoked = true;
		}
	}

	// ------------------------------------------------------------------
	// Q1 hardening tests
	// ------------------------------------------------------------------

	@Test
	void springTransactionalRollsBackInsertOnException() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TransactionalApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-tx-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			StarterTestDAO dao = ctx.getBean(StarterTestDAO.class);
			TransactionalService service = ctx.getBean(TransactionalService.class);

			dao.createTable();
			assertThat(dao.count()).isEqualTo(0);

			// The service inserts a row then throws — Spring's @Transactional
			// must roll the connection back so JdbcClient (which participates
			// in the active transaction via DataSourceUtils) leaves no trace.
			assertThatThrownBy(() -> service.insertThenFail(new StarterItem(1, "rollback-me")))
					.isInstanceOf(IllegalStateException.class);

			assertThat(dao.count())
					.as("row inserted before the exception must have been rolled back")
					.isEqualTo(0);

			// Sanity: a non-throwing transactional call commits.
			service.insertThenCommit(new StarterItem(2, "commit-me"));
			assertThat(dao.count()).isEqualTo(1);
		} finally {
			ctx.close();
		}
	}

	@SpringBootApplication
	@QueriesScan(basePackageClasses = StarterTestDAO.class)
	static class TransactionalApp {

		@Bean
		TransactionalService transactionalService(StarterTestDAO dao) {
			return new TransactionalService(dao);
		}
	}

	@Service
	static class TransactionalService {

		private final StarterTestDAO dao;

		TransactionalService(StarterTestDAO dao) {
			this.dao = dao;
		}

		@Transactional
		public void insertThenFail(StarterItem item) {
			dao.insert(item);
			throw new IllegalStateException("forced rollback");
		}

		@Transactional
		public void insertThenCommit(StarterItem item) {
			dao.insert(item);
		}
	}

	@Test
	void userDefinedQueriesBeanOverridesAutoConfig() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(UserQueriesApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-user-queries-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			Queries q = ctx.getBean(Queries.class);
			assertThat(q)
					.as("autoconfig must back off when the user declares a Queries bean")
					.isSameAs(UserQueriesApp.userInstance);
			// Only one Queries bean total — autoconfig didn't add a parallel one.
			assertThat(ctx.getBeansOfType(Queries.class)).hasSize(1);
		} finally {
			ctx.close();
		}
	}

	@SpringBootApplication
	static class UserQueriesApp {

		static volatile Queries userInstance;

		@Bean
		Queries queries() {
			Queries built = Queries.of(QueriesSource.ofClass(StarterTestDAO.class));
			userInstance = built;
			return built;
		}
	}

	@Test
	void userDefinedJdbcClientBeanOverridesAutoConfig() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(UserJdbcClientApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-user-jdbcclient-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			JdbcClient client = ctx.getBean(JdbcClient.class);
			assertThat(client)
					.as("autoconfig must back off when the user declares a JdbcClient bean")
					.isSameAs(UserJdbcClientApp.userInstance);
			assertThat(ctx.getBeansOfType(JdbcClient.class)).hasSize(1);
		} finally {
			ctx.close();
		}
	}

	@SpringBootApplication
	@QueriesScan(basePackageClasses = StarterTestDAO.class)
	static class UserJdbcClientApp {

		static volatile JdbcClient userInstance;

		@Bean
		JdbcClient jdbcClient(DataSource dataSource) {
			JdbcClient built = JdbcClient.create(dataSource);
			userInstance = built;
			return built;
		}
	}

	@Test
	void autoConfigStaysOffWithoutDataSource() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(NoDataSourceApp.class)
				.web(WebApplicationType.NONE)
				.run();
		try {
			// The @ConditionalOnBean(DataSource.class) guard must keep both
			// JdbcClient and Queries beans out of the context entirely.
			assertThatThrownBy(() -> ctx.getBean(Queries.class))
					.isInstanceOf(NoSuchBeanDefinitionException.class);
			assertThatThrownBy(() -> ctx.getBean(JdbcClient.class))
					.isInstanceOf(NoSuchBeanDefinitionException.class);
		} finally {
			ctx.close();
		}
	}

	@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class })
	static class NoDataSourceApp {
	}
}
