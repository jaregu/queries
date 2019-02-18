package com.jaregu.database.queries.ext.guice;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.compiling.Entities;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;
import com.jaregu.database.queries.parsing.Sources;

public class QueriesModule {

	private QueriesModule() {
	}

	public static Module queriesModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				// MME called at least one time for guice to create empty list if no source is supplied
				Multibinder.newSetBinder(binder(), QueriesSource.class);

				// MME called at least one time for guice to create empty list if no configurator is supplied
				Multibinder.newSetBinder(binder(), QueriesConfigurator.class);

				// MME called at least one time for guice to create empty list if no entity class is registered
				MapBinder.newMapBinder(binder(),
						new TypeLiteral<String>() {
						}, new TypeLiteral<Class<?>>() {
						});

				TypeLiteral<Set<QueriesSource>> sourcesType = new TypeLiteral<Set<QueriesSource>>() {
				};
				TypeLiteral<Map<String, Class<?>>> entitiesType = new TypeLiteral<Map<String, Class<?>>>() {
				};
				TypeLiteral<Set<QueriesConfigurator>> configuratorType = new TypeLiteral<Set<QueriesConfigurator>>() {
				};
				Provider<Set<QueriesSource>> sourcesProvider = getProvider(Key.get(sourcesType));
				Provider<Map<String, Class<?>>> entitiesProvider = getProvider(Key.get(entitiesType));
				Provider<Set<QueriesConfigurator>> configuratorsProvider = getProvider(Key.get(configuratorType));

				bind(Queries.class).toProvider(() -> {
					Queries.Builder queriesBuilder = Queries
							.builder()
							.sources(sourcesProvider.get());

					entitiesProvider.get()
							.entrySet()
							.forEach(e -> queriesBuilder.entity(e.getValue(), e.getKey()));

					configuratorsProvider.get().forEach(c -> {
						c.configure(queriesBuilder);
					});

					return queriesBuilder.build();
				}).in(Singleton.class);
			}
		};
	}

	public static Module queriesConfiguratorModule(Class<? extends QueriesConfigurator> configuratorClass) {
		return new AbstractModule() {
			@Override
			protected void configure() {
				Multibinder<QueriesConfigurator> sourcesBinder = Multibinder.newSetBinder(binder(),
						QueriesConfigurator.class);
				sourcesBinder.addBinding().to(configuratorClass);
			}
		};
	}

	public static Module queriesConfiguratorModule(QueriesConfigurator configurator) {
		return new AbstractModule() {
			@Override
			protected void configure() {
				Multibinder<QueriesConfigurator> sourcesBinder = Multibinder.newSetBinder(binder(),
						QueriesConfigurator.class);
				sourcesBinder.addBinding().toInstance(configurator);
			}
		};
	}

	public static SourceModuleBuilder sourceModuleBuilder() {
		return new SourceModuleBuilder();
	}

	public static Module sourceModule(QueriesSource... sources) {
		return sourceModuleBuilder().sources(sources).build();
	}

	/**
	 * Module to add {@link Entities#entity(Class)}
	 * 
	 * @param entityClass
	 * @return
	 */
	public static <T> Module entityModule(Class<T> entityClass) {
		return entityModule(entityClass, entityClass.getSimpleName());
	}

	/**
	 * Module to add {@link Entities#entity(Class, String)}
	 * 
	 * @param entityClass
	 * @param alias
	 * @return
	 */
	public static <T> Module entityModule(Class<T> entityClass, String alias) {

		return new AbstractModule() {
			@Override
			protected void configure() {
				MapBinder<String, Class<?>> entitiesBinder = MapBinder.newMapBinder(binder(),
						new TypeLiteral<String>() {
						}, new TypeLiteral<Class<?>>() {
						});

				entitiesBinder.addBinding(alias).toInstance(entityClass);
			}
		};
	}

	public static <T> Module proxyModule(Class<T> proxyInterface) {

		return new AbstractModule() {
			@Override
			protected void configure() {
				Provider<Queries> queriesProvider = getProvider(Queries.class);

				Multibinder<QueriesSource> sourcesBinder = Multibinder.newSetBinder(binder(), QueriesSource.class);
				sourcesBinder.addBinding().toInstance(QueriesSource.ofClass(proxyInterface));

				bind(proxyInterface).toProvider(new ProxyInterfaceProvider<T>(queriesProvider, proxyInterface))
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
