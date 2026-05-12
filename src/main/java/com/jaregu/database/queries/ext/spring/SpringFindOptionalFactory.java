package com.jaregu.database.queries.ext.spring;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.proxy.FindOptional;
import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

/**
 * Spring-backed factory for the {@link FindOptional} annotation. Returns
 * {@code Optional<rowClass>} (or {@code null} when {@code useOptional = false})
 * mapped via {@link SpringColumnAwareRowMapper}. Throws
 * {@code IncorrectResultSizeDataAccessException} if more than one row matches.
 */
public final class SpringFindOptionalFactory implements QueryMapperFactory {

	private final JdbcClient jdbcClient;

	public SpringFindOptionalFactory(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		FindOptional findOptional = (FindOptional) annotation;
		return build(findOptional.value(), findOptional.useOptional());
	}

	private <T> QueryMapper<?> build(Class<T> rowClass, boolean useOptional) {
		RowMapper<T> rowMapper = SpringColumnAwareRowMapper.forClass(rowClass);
		return (query, args) -> {
			Optional<T> optional = jdbcClient.sql(query.getSql())
					.params(query.getParameters())
					.query(rowMapper)
					.optional();
			return useOptional ? optional : optional.orElse(null);
		};
	}
}
