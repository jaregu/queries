package com.jaregu.database.queries.ext.spring;

import java.lang.annotation.Annotation;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.jaregu.database.queries.ext.dalesbred.ExecuteUpdate;
import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

/**
 * Spring-backed factory for the {@link ExecuteUpdate} annotation. Executes the
 * built {@code Query} via {@link JdbcClient} and returns the updated row count
 * (or {@code null} when {@code unique = true}).
 */
public final class SpringExecuteUpdateFactory implements QueryMapperFactory {

	private final JdbcClient jdbcClient;

	public SpringExecuteUpdateFactory(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		boolean executeUnique = ((ExecuteUpdate) annotation).unique();
		return (query, args) -> {
			int rows = jdbcClient.sql(query.getSql())
					.params(query.getParameters())
					.update();
			if (executeUnique) {
				if (rows != 1) {
					throw new IncorrectResultSizeDataAccessException(
							"Expected exactly 1 row to be updated", 1, rows);
				}
				return null;
			}
			return rows;
		};
	}
}
