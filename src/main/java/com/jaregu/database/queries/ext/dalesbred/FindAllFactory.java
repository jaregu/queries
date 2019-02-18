package com.jaregu.database.queries.ext.dalesbred;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.dalesbred.Database;

import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

public class FindAllFactory implements QueryMapperFactory {

	private Provider<Database> database;

	public FindAllFactory(Provider<Database> database) {
		this.database = database;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		Class<?> rowClass = ((FindAll) annotation).value();
		return (query, args) -> database.get().findAll(rowClass, QueriesUtil.toQuery(query));
	}
}
