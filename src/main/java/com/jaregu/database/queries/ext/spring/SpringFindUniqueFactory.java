package com.jaregu.database.queries.ext.spring;

import java.lang.annotation.Annotation;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.proxy.FindUnique;
import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

/**
 * Spring-backed factory for the {@link FindUnique} annotation. Returns the
 * single matching row of {@code rowClass} mapped via
 * {@link SpringColumnAwareRowMapper}. Throws
 * {@code IncorrectResultSizeDataAccessException} when the row count is not
 * exactly one.
 */
public final class SpringFindUniqueFactory implements QueryMapperFactory {

	private final JdbcClient jdbcClient;

	public SpringFindUniqueFactory(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		return build(((FindUnique) annotation).value());
	}

	private <T> QueryMapper<T> build(Class<T> rowClass) {
		RowMapper<T> rowMapper = SpringColumnAwareRowMapper.forClass(rowClass);
		return (query, args) -> jdbcClient.sql(query.getSql())
				.params(query.getParameters())
				.query(rowMapper)
				.single();
	}
}
