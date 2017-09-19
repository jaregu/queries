package com.jaregu.database;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.QueriesConfigImpl;
import com.jaregu.database.queries.QueriesImpl.Builder;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.parsing.QueriesSource;

@Singleton
public class QueriesProvider implements Provider<Queries> {

	private Helper helper;

	@javax.inject.Inject
	public QueriesProvider(Helper helper) {
		this.helper = helper;
	}

	@Override
	public Queries get() {
		QueriesConfigImpl.Builder configBuilder = Queries.configBuilder();
		if (ApplicationMode.isDevelopment()) {
			configBuilder.cache(QueriesCache.noCache());
		}
		QueriesConfig config = configBuilder.build();
		Builder builder = Queries.builder().config(config);
		return builder.sources(this::getSources).build();
	}

	private Collection<QueriesSource> getSources() {
		return helper.queriesSources;
	}

	public static Module queriesModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(Queries.class).toProvider(QueriesProvider.class);
			}
		};
	}

	public static Module sources(QueriesSource... sources) {
		return new AbstractModule() {
			@Override
			protected void configure() {
				Multibinder<QueriesSource> sourcesBinder = Multibinder.newSetBinder(binder(), QueriesSource.class);
				for (QueriesSource source : sources) {
					sourcesBinder.addBinding().toInstance(source);
				}
			}
		};
	}

	@SafeVarargs
	public static Module sourceClasses(Class<QueriesSource>... sources) {
		return new AbstractModule() {
			@Override
			protected void configure() {
				Multibinder<QueriesSource> sourcesBinder = Multibinder.newSetBinder(binder(), QueriesSource.class);
				for (Class<QueriesSource> source : sources) {
					sourcesBinder.addBinding().to(source);
				}
			}
		};
	}

	@Singleton
	public static class Helper {

		@Inject(optional = true)
		Set<QueriesSource> queriesSources = Collections.emptySet();
	}
}
