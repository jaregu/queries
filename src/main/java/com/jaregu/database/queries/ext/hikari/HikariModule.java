package com.jaregu.database.queries.ext.hikari;

import javax.inject.Singleton;
import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class HikariModule extends AbstractModule {

	private HikariModule() {
	}

	@Override
	protected void configure() {
		bind(DataSource.class).toProvider(HikariDataSourceProvider.class).in(Singleton.class);
	}

	public static Module create(HikariIntegration integration) {
		return Modules.combine(new HikariModule(), new AbstractModule() {
			@Override
			protected void configure() {
				bind(HikariIntegration.class).toInstance(integration);
			}
		});
	}

	public static Module create(Class<? extends HikariIntegration> integrationClass) {
		return Modules.combine(new HikariModule(), new AbstractModule() {
			@Override
			protected void configure() {
				bind(HikariIntegration.class).to(integrationClass).in(Singleton.class);
			}
		});
	}
}
