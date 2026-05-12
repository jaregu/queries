package com.jaregu.database.queries.ext.spring;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.proxy.FindOptional;
import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

/**
 * Spring-backed factory for the {@link FindOptional} annotation. Returns an
 * {@code Optional<rowClass>} (or {@code null} when {@code useOptional = false}).
 * Throws {@code IncorrectResultSizeDataAccessException} if more than one row
 * matches.
 */
public final class SpringFindOptionalFactory implements QueryMapperFactory {

	private final JdbcClient jdbcClient;

	public SpringFindOptionalFactory(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		FindOptional findOptional = (FindOptional) annotation;
		Class<?> rowClass = findOptional.value();
		boolean useOptional = findOptional.useOptional();
		return (query, args) -> {
			Optional<?> optional = jdbcClient.sql(query.getSql())
					.params(query.getParameters())
					.query(rowClass)
					.optional();
			return useOptional ? optional : optional.orElse(null);
		};
	}
}
