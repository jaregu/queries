package com.jaregu.database.queries.ext.dalesbred;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.dalesbred.Database;

@Singleton
public class DalesbredDatabaseProvider implements Provider<Database> {

	private final DataSource dataSource;

	@Inject
	DalesbredDatabaseProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Database get() {
		return Database.forDataSource(dataSource);
	}
}
