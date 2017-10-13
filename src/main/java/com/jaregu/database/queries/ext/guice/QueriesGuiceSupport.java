package com.jaregu.database.queries.ext.guice;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;
import com.jaregu.database.queries.parsing.Sources;

@Singleton
public class QueriesGuiceSupport {

	private QueriesGuiceSupport() {
	}

	public static Module queriesModule(Function<QueriesSources, Queries> queriesSupplier) {
		return new AbstractModule() {
			@Override
			protected void configure() {

				com.google.inject.Provider<SourcesCollector> provider = binder().getProvider(SourcesCollector.class);

				bind(Queries.class).toProvider(new Provider<Queries>() {

					@Override
					public Queries get() {
						return queriesSupplier.apply(provider.get().getSources());
					}
				});
			}
		};
	}

	public static SourceModuleBuilder sourceModuleBuilder() {
		return new SourceModuleBuilder();
	}

	public static Module sourceModule(QueriesSource... sources) {
		return sourceModuleBuilder().sources(sources).build();
	}

	public static <T> Module proxyModule(Class<T> proxyInterface) {

		return new AbstractModule() {
			@Override
			protected void configure() {
				Provider<Queries> provider = getProvider(Queries.class);

				Multibinder<QueriesSource> sourcesBinder = Multibinder.newSetBinder(binder(), QueriesSource.class);
				sourcesBinder.addBinding().toInstance(QueriesSource.ofClass(proxyInterface));

				bind(proxyInterface).toProvider(new ProxyInterfaceProvider<T>(provider, proxyInterface))
						.in(Singleton.class);
			}
		};
	}

	@SafeVarargs
	public static Module sourceClassesModule(Class<QueriesSource>... sources) {
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

	public static class SourceModuleBuilder implements Sources<SourceModuleBuilder> {

		private List<QueriesSource> sources = new LinkedList<>();

		@Override
		public SourceModuleBuilder sources(QueriesSources sources) {
			this.sources.addAll(sources.getSources());
			return this;
		}

		public Module build() {
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
	}

	@Singleton
	static class SourcesCollector {

		@Inject(optional = true)
		Set<QueriesSource> queriesSources = Collections.emptySet();

		QueriesSources getSources() {
			return QueriesSources.of(queriesSources);
		}
	}

	private static class ProxyInterfaceProvider<T> implements Provider<T> {

		private Provider<Queries> queriesProvider;
		private Class<T> proxyInterface;

		ProxyInterfaceProvider(Provider<Queries> queriesProvider, Class<T> proxyInterface) {
			this.queriesProvider = queriesProvider;
			this.proxyInterface = proxyInterface;
		}

		@Override
		public T get() {
			return queriesProvider.get().proxy(proxyInterface);
		}
	}
}
