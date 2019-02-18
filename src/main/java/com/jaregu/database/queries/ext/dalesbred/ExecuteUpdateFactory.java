package com.jaregu.database.queries.ext.dalesbred;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.dalesbred.Database;

import com.jaregu.database.queries.proxy.QueryMapper;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

public class ExecuteUpdateFactory implements QueryMapperFactory {

	private Provider<Database> database;

	public ExecuteUpdateFactory(Provider<Database> database) {
		this.database = database;
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		ExecuteUpdate executeUpdate = ((ExecuteUpdate) annotation);
		boolean executeUnique = executeUpdate.unique();
		return (query, args) -> {
			if (executeUnique) {
				database.get().updateUnique(QueriesUtil.toQuery(query));
				return null;
			} else {
				return database.get().update(QueriesUtil.toQuery(query));
			}
		};
	}
}
