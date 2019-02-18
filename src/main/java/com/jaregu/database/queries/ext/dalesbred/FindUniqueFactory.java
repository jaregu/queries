package com.jaregu.database.queries.ext.dalesbred;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.dalesbred.Database;

import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

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
