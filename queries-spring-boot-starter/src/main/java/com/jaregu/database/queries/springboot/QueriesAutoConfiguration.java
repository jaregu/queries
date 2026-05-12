package com.jaregu.database.queries.springboot;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.QueriesConfigurator;
import com.jaregu.database.queries.ext.spring.SpringQueriesMappers;
import com.jaregu.database.queries.parsing.QueriesSource;

/**
 * Spring Boot auto-configuration for the Jaregu Queries library.
 *
 * <p>Activates whenever a {@link DataSource} is on the classpath and a
 * {@code Queries} bean has not been user-defined. Wires:
 * <ul>
 *   <li>a {@link JdbcClient} backed by the auto-configured {@code DataSource}
 *       (unless the user already declared one)</li>
 *   <li>a {@link Queries} bean built from every {@link QueriesSource} bean in
 *       the context plus the four Spring-backed {@code QueryMapperFactory}
 *       implementations</li>
 * </ul>
 *
 * <p>Users can register additional {@link QueriesSource} beans (directly or via
 * {@link QueriesScan}) and/or supply {@link QueriesConfigurator} beans to tune
 * the builder (dialect, cache, parameter binder, custom mappers). The same
 * {@code QueriesConfigurator} type is used by the Guice integration, so
 * configurators are portable between both worlds.
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass({ Queries.class, JdbcClient.class })
@ConditionalOnBean(DataSource.class)
public class QueriesAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public JdbcClient jdbcClient(DataSource dataSource) {
		return JdbcClient.create(dataSource);
	}

	@Bean
	@ConditionalOnMissingBean
	public Queries queries(
			JdbcClient jdbcClient,
			ObjectProvider<QueriesSource> sources,
			ObjectProvider<QueriesEntity> entities,
			ObjectProvider<QueriesConfigurator> configurators) {

		Queries.Builder builder = Queries.builder();
		sources.orderedStream().forEach(builder::source);
		entities.orderedStream().forEach(e -> builder.entity(e.entityClass(), e.alias()));
		SpringQueriesMappers.register(builder, jdbcClient);
		configurators.orderedStream().forEach(c -> c.configure(builder));
		return builder.build();
	}
}
