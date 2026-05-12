package com.jaregu.database.queries.ext.spring;

import java.lang.annotation.Annotation;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.proxy.FindAll;
import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

/**
 * Spring-backed factory for the {@link FindAll} annotation. Returns a
 * {@code List<rowClass>} mapped via {@link SpringColumnAwareRowMapper} —
 * supports records, JavaBeans, the project's {@code @Column} annotation,
 * and snake_case → camelCase fallback.
 */
public final class SpringFindAllFactory implements QueryMapperFactory {

	private final JdbcClient jdbcClient;

	public SpringFindAllFactory(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		return build(((FindAll) annotation).value());
	}

	private <T> QueryMapper<List<T>> build(Class<T> rowClass) {
		RowMapper<T> rowMapper = SpringColumnAwareRowMapper.forClass(rowClass);
		return (query, args) -> jdbcClient.sql(query.getSql())
				.params(query.getParameters())
				.query(rowMapper)
				.list();
	}
}
