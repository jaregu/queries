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
 * Module to support java interface <-> sql bridge using dalesbred library
 *
 */
public class DalesbredModule extends AbstractModule {

	private DalesbredModule() {
	}

	@Override
	protected void configure() {

		bind(Database.class).toProvider(DalesbredDatabaseProvider.class).in(Singleton.class);

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
		return new DalesbredModule();
	}
}
