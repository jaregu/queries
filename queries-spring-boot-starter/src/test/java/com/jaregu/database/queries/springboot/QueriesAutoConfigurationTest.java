package com.jaregu.database.queries.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.QueriesConfigurator;

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
}
