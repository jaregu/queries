package com.jaregu.database.queries.ext.dalesbred;

import javax.sql.DataSource;

import org.dalesbred.Database;

import com.jaregu.database.queries.ext.dalesbred.DalesbredModuleConfiguration.ConversionRegistration;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class DalesbredDatabaseProvider implements Provider<Database> {

	private final DataSource dataSource;
	private final DalesbredModuleConfiguration configuration;

	@Inject
	DalesbredDatabaseProvider(DataSource dataSource, DalesbredModuleConfiguration configuration) {
		this.dataSource = dataSource;
		this.configuration = configuration;
	}

	@Override
	public Database get() {
		Database database = Database.forDataSource(dataSource);
		for (ConversionRegistration<?, ?> conversion : configuration.getConversions()) {
			@SuppressWarnings("unchecked")
			ConversionRegistration<Object, Object> castedConversion = (ConversionRegistration<Object, Object>) conversion;
			database.getTypeConversionRegistry().registerConversions(
					castedConversion.getDatabaseType(),
					castedConversion.getJavaType(),
					castedConversion.getFromDatabase(),
					castedConversion.getToDatabase());
		}
		return database;
	}
}
