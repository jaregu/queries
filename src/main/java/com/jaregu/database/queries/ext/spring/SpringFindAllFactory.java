package com.jaregu.database.queries.ext.spring;

import java.lang.annotation.Annotation;

import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.ext.dalesbred.FindAll;
import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

/**
 * Spring-backed factory for the {@link FindAll} annotation. Returns a
 * {@code List<rowClass>} mapped via {@link JdbcClient}'s default property
 * row mapper (snake_case to camelCase, constructor/setter binding,
 * record support).
 */
public final class SpringFindAllFactory implements QueryMapperFactory {

	private final JdbcClient jdbcClient;

	public SpringFindAllFactory(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		Class<?> rowClass = ((FindAll) annotation).value();
		return (query, args) -> jdbcClient.sql(query.getSql())
				.params(query.getParameters())
				.query(rowClass)
				.list();
	}
}
