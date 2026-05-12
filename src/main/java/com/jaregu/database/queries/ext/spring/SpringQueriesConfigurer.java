package com.jaregu.database.queries.ext.spring;

import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.ext.dalesbred.ExecuteUpdate;
import com.jaregu.database.queries.ext.dalesbred.FindAll;
import com.jaregu.database.queries.ext.dalesbred.FindOptional;
import com.jaregu.database.queries.ext.dalesbred.FindUnique;

/**
 * One-shot helper that registers the four Spring-backed
 * {@link com.jaregu.database.queries.proxy.QueryMapperFactory} implementations
 * on a {@link Queries.Builder}. Typical use in plain Spring (non-Boot) setups:
 *
 * <pre>{@code
 * @Bean
 * Queries queries(JdbcClient jdbc, List<QueriesSource> sources) {
 *     Queries.Builder b = Queries.builder();
 *     sources.forEach(b::sources);
 *     SpringQueriesConfigurer.configure(b, jdbc);
 *     return b.build();
 * }
 * }</pre>
 *
 * <p>Spring Boot users get this wired automatically by the
 * {@code queries-spring-boot-starter} module.
 */
public final class SpringQueriesConfigurer {

	private SpringQueriesConfigurer() {
	}

	/**
	 * Registers the four Spring factories ({@link SpringExecuteUpdateFactory},
	 * {@link SpringFindAllFactory}, {@link SpringFindOptionalFactory},
	 * {@link SpringFindUniqueFactory}) on the supplied builder. Returns the
	 * builder for chaining.
	 */
	public static Queries.Builder configure(Queries.Builder builder, JdbcClient jdbcClient) {
		return builder
				.mapper(ExecuteUpdate.class, new SpringExecuteUpdateFactory(jdbcClient))
				.mapper(FindAll.class, new SpringFindAllFactory(jdbcClient))
				.mapper(FindOptional.class, new SpringFindOptionalFactory(jdbcClient))
				.mapper(FindUnique.class, new SpringFindUniqueFactory(jdbcClient));
	}
}
