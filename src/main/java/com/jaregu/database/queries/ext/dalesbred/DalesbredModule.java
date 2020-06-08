package com.jaregu.database.queries.ext.dalesbred;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.aopalliance.intercept.MethodInterceptor;
import org.dalesbred.Database;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.jaregu.database.queries.ext.guice.QueriesModule;

/**
 * Module to support java interface to sql bridge using dalesbred library
 *
 */
public class DalesbredModule extends AbstractModule {

	private DalesbredModuleConfiguration configuration;

	private DalesbredModule(DalesbredModuleConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void configure() {

		bind(Database.class).toProvider(DalesbredDatabaseProvider.class).in(Singleton.class);
		bind(DalesbredModuleConfiguration.class).toInstance(configuration);

		Provider<Database> databaseProvider = binder().getProvider(Database.class);

		install(QueriesModule.queriesConfiguratorModule((builder) -> {
			builder.mapper(ExecuteUpdate.class, new ExecuteUpdateFactory(databaseProvider));
			builder.mapper(FindAll.class, new FindAllFactory(databaseProvider));
			builder.mapper(FindOptional.class, new FindOptionalFactory(databaseProvider));
			builder.mapper(FindUnique.class, new FindUniqueFactory(databaseProvider));
		}));

		MethodInterceptor interceptor = new TransactionalMethodInterceptor(databaseProvider);

		bindInterceptor(any(), annotatedWith(Transactional.class), interceptor);
		bindInterceptor(annotatedWith(Transactional.class), any(), interceptor);
	}

	public static Module create() {
		return create(DalesbredModuleConfiguration.builder().build());
	}

	public static Module create(DalesbredModuleConfiguration configuration) {
		return new DalesbredModule(configuration);
	}
}
