package com.jaregu.database.queries.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.Queries;

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
	void customizerHookRunsAndCanOverrideDefaults() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(CustomizerApp.class)
				.web(WebApplicationType.NONE)
				.properties(
						"spring.datasource.url=jdbc:hsqldb:mem:starter-customizer-test",
						"spring.datasource.username=SA",
						"spring.datasource.password=",
						"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver")
				.run();
		try {
			RecordingCustomizer recording = ctx.getBean(RecordingCustomizer.class);
			assertThat(recording.invoked).isTrue();
		} finally {
			ctx.close();
		}
	}

	@SpringBootApplication
	@QueriesScan(basePackageClasses = StarterTestDAO.class)
	static class CustomizerApp {

		@Bean
		RecordingCustomizer recordingCustomizer() {
			return new RecordingCustomizer();
		}
	}

	static class RecordingCustomizer implements QueriesCustomizer {
		volatile boolean invoked;

		@Override
		public void customize(Queries.Builder builder) {
			invoked = true;
		}
	}
}
