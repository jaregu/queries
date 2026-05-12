package com.jaregu.database.queries.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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
