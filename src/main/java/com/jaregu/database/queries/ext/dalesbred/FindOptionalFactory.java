package com.jaregu.database.queries.ext.dalesbred;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.dalesbred.Database;

import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

import jakarta.inject.Provider;

public class FindOptionalFactory implements QueryMapperFactory {

	private Provider<Database> database;

	public FindOptionalFactory(Provider<Database> database) {
		this.database = database;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		FindOptional findOptional = (FindOptional) annotation;
		Class<?> rowClass = findOptional.value();
		boolean useOptional = findOptional.useOptional();
		return (query, args) -> {
			Optional<?> optional = database.get().findOptional(rowClass, QueriesUtil.toQuery(query));
			return useOptional ? optional : optional.orElse(null);
		};
	}
}
