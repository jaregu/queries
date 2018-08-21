package com.jaregu.database.queries;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jaregu.database.queries.building.Binders;
import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.cache.Caches;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.compiling.QueryCompiler;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Dialects;
import com.jaregu.database.queries.parsing.QueriesParser;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;
import com.jaregu.database.queries.parsing.QueryParseException;
import com.jaregu.database.queries.parsing.Sources;
import com.jaregu.database.queries.proxy.ClassQueryMapper;
import com.jaregu.database.queries.proxy.Converters;
import com.jaregu.database.queries.proxy.Mappers;
import com.jaregu.database.queries.proxy.QueriesSourceClass;
import com.jaregu.database.queries.proxy.QueriesSourceId;
import com.jaregu.database.queries.proxy.QueriesSourceResource;
import com.jaregu.database.queries.proxy.QueryConverterFactory;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

/**
 * Queries framework enables easy dynamic SQL creation and using in java code.
 * <p>
 * Create <code>Queries</code> instance using {@link Queries#builder()} method
 * and returned builder.
 * <p>
 * 
 * To create <code>Queries</code> instance there must be at least one
 * {@link QueriesSource} supplied. See all builder <code>source...</code>
 * methods for all possible values.
 * <p>
 * 
 * With builder it is possible to set:
 * <ul>
 * <li>{@link ParameterBinder} for some SQL <code>IN (?)</code> clause support
 * or other purposes
 * <li>{@link Dialect} for some built-in conversions like OFFSET ? LIMIT ?
 * support or dynamic ORDER BY clause
 * <li>{@link QueriesCache} for some production ready caching mechanisms
 * </ul>
 * <p>
 * 
 * It is possible to use some dependency injection framework to develop feature
 * modules (using the same Queries instance) independently one of another (each
 * module registers own query sources)
 * <p>
 * 
 * And there is annotation support for interface proxy creation, to use only
 * interface to access SQL sources and pass parameters, like:
 * 
 * <pre>
 * Queries queries = Queries.builder()...build();
 * SomeInterface some = queries.proxy(SomeInterface.class);
 * 
 * Query query = some.getFooBarStatement(1, "foo", "BAR");
 * con.prepareStatement(query.getSql())
 * </pre>
 * 
 */
public interface Queries extends QueriesFinder<QueryId> {

	/**
	 * Returns cached {@link PreparedQuery} (see {@link QueriesCache}) or
	 * prepare one using supplied SQL sources. Shorthand for
	 * Queries.get(QueryId) with {@link QueryId#of(String)}
	 * 
	 */
	PreparedQuery get(String queryId);

	RelativeQueries relativeTo(SourceId sourceId);

	/**
	 * Interface must have at least one of ({@link QueriesSourceClass},
	 * {@link QueriesSourceResource}, {@link QueriesSourceId}) source
	 * identification annotations.
	 * <p>
	 * 
	 * By default interface method must return Query instance or there will be
	 * {@link ClassCastException} exception
	 * <p>
	 * 
	 * Use {@link ClassQueryMapper} annotation to perform additional
	 * {@link Query} mapping to something else. ClassQueryMapper expects class
	 * to be instantiable class with accessible zero argument constructor or use
	 * custom annotations for more powerful conversions. Use
	 * {@link Builder#converter(Class, QueryConverterFactory)} builder method to
	 * register custom factories.
	 * 
	 * @param classOfInterface
	 * @return
	 */
	<T> T proxy(Class<T> classOfInterface);

	/**
	 * Shorthand for <code>builder().sources(...).build()</code>. Use
	 * {@link #builder()} for more configuration options
	 * 
	 * @param sources
	 * @return
	 */
	static Queries of(QueriesSources sources) {
		return builder().sources(sources).build();
	}

	/**
	 * Shorthand for <code>builder().sources(...).build()</code>. Use
	 * {@link #builder()} for more configuration options
	 * 
	 * @param sources
	 * @return
	 */
	static Queries of(Collection<QueriesSource> sources) {
		return of(QueriesSources.of(sources));
	}

	/**
	 * Shorthand for <code>builder().sources(...).build()</code>. Use
	 * {@link #builder()} for more configuration options
	 * 
	 * @param sources
	 * @return
	 */
	static Queries of(QueriesSource... sources) {
		return of(QueriesSources.of(sources));
	}

	/**
	 * Creates builder for <code>Queries</code> instance creation. To create
	 * Queries instance creator must supply at least one SQL queries source. See
	 * all <code>builder.source...</code> methods for possible options.
	 * <p>
	 * {@link Builder#sources(QueriesSources)}
	 */
	static Queries.Builder builder() {
		return new Builder();
	}

	/**
	 * Queries builder class. Use builder to configure Queries instance.
	 *
	 */
	public static class Builder implements Sources<Builder>, Binders<Builder>, Dialects<Builder>, Caches<Builder>,
			Mappers<Builder>, Converters<Builder> {

		private List<QueriesSource> sources = new LinkedList<>();
		private Optional<QueriesCache> cache = Optional.empty();
		private Optional<Dialect> dialect = Optional.empty();
		private Optional<ParameterBinder> parameterBinder = Optional.empty();
		private Map<Class<? extends Annotation>, QueryMapperFactory> mappers = new HashMap<>();
		private Map<Class<? extends Annotation>, QueryConverterFactory> converters = new HashMap<>();

		Builder() {
		}

		@Override
		public Builder sources(QueriesSources sources) {
			this.sources.addAll(sources.getSources());
			return this;
		}

		@Override
		public Builder binder(ParameterBinder binder) {
			this.parameterBinder = Optional.of(binder);
			return this;
		}

		@Override
		public Builder dialect(Dialect dialect) {
			this.dialect = Optional.of(dialect);
			return this;
		}

		@Override
		public Builder cache(QueriesCache cache) {
			this.cache = Optional.of(cache);
			return this;
		}

		@Override
		public Builder mapper(Class<? extends Annotation> annotatedWith, QueryMapperFactory factory) {
			this.mappers.put(annotatedWith, factory);
			return this;
		}

		@Override
		public Builder converter(Class<? extends Annotation> annotatedWith, QueryConverterFactory factory) {
			this.converters.put(annotatedWith, factory);
			return this;
		}

		public Queries build() {
			if (sources.isEmpty())
				throw new QueryParseException("Can't build Queries, sources is empty");

			QueriesConfig config = new QueriesConfigImpl(dialect.orElse(Dialects.defaultDialect()),
					parameterBinder.orElse(Binders.defaultBinder()), mappers, converters);
			return new QueriesImpl(QueriesSources.of(sources), QueriesParser.create(), QueryCompiler.of(config),
					cache.orElse(Caches.noCache()), config);
		}
	}
}