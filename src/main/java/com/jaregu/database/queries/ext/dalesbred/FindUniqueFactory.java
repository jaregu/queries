package com.jaregu.database.queries.ext.dalesbred;

import java.lang.annotation.Annotation;

import org.dalesbred.Database;

import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

import jakarta.inject.Provider;

public class FindUniqueFactory implements QueryMapperFactory {

	private Provider<Database> database;

	public FindUniqueFactory(Provider<Database> database) {
		this.database = database;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		Class<?> rowClass = ((FindUnique) annotation).value();
		return (query, args) -> database.get().findUnique(rowClass, QueriesUtil.toQuery(query));
	}
}
