package com.jaregu.database.queries;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryBuilder;
import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.CompiledQuery;
import com.jaregu.database.queries.compiling.QueryCompiler;
import com.jaregu.database.queries.parsing.QueriesParser;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;
import com.jaregu.database.queries.parsing.SourceQueries;
import com.jaregu.database.queries.parsing.SourceQuery;

public final class QueriesImpl implements Queries {

	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final QueriesConfig config;
	private final QueriesSources sources;
	private final QueriesCache cache;
	private final QueriesParser parser;
	private final QueryCompiler compiler;
	private final QueryBuilder builder;

	private volatile Map<SourceId, QueriesSource> sourcesById;

	private QueriesImpl(QueriesConfig config, QueriesSources sources, QueriesCache cache, QueriesParser parser,
			QueryCompiler compiler, QueryBuilder builder) {
		this.config = config;
		this.sources = sources;
		this.cache = cache;
		this.parser = parser;
		this.compiler = compiler;
		this.builder = builder;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	@Override
	public Query get(QueryId queryId) {
		return get(queryId, ParamsResolver.empty());
	}

	@Override
	public Query get(QueryId queryId, Object params) {
		return get(queryId, ParamsResolver.forBean(params));
	}

	@Override
	public Query get(QueryId queryId, Map<String, Object> params) {
		return get(queryId, ParamsResolver.forMap(params));
	}

	@Override
	public Query get(QueryId queryId, ParamsResolver resolver) {
		CompiledQuery compiledQuery = cache.getCompiledQuery(queryId, this::compileQuery);
		log.debug("Starting to build queryId: {}!", queryId);
		return builder.build(compiledQuery, resolver);
	}

	private CompiledQuery compileQuery(QueryId queryId) {
		SourceQuery sourceQuery = cache.getSourceQueries(queryId.getSourceId(), this::parseQueries).get(queryId);
		log.debug("Starting to compile queryId: {}!", queryId);
		return compiler.compile(sourceQuery);
	}

	private SourceQueries parseQueries(SourceId sourceId) {
		QueriesSource queriesSource = ensureSources().get(sourceId);
		if (queriesSource == null) {
			throw new QueryException("Can't find source with id: " + sourceId);
		}
		log.debug("Starting to parse queries source: {}!", sourceId);
		return parser.parse(queriesSource);
	}

	private Map<SourceId, QueriesSource> ensureSources() {
		if (sourcesById == null) {
			synchronized (this) {
				log.debug("Initializing queries sources!");
				if (sourcesById == null) {
					Map<SourceId, QueriesSource> sourcesById = sources.getSources().stream()
							.collect(Collectors.toMap(QueriesSource::getSourceId, Function.identity()));
					this.sourcesById = sourcesById;
				}
			}
		}
		return sourcesById;
	}

	public static class Builder {

		private QueriesConfigImpl config = QueriesConfigImpl.getDefault();
		private Optional<QueriesSources> sources = Optional.empty();
		private Optional<QueriesCache> cache = Optional.empty();
		private Optional<QueriesParser> parser = Optional.empty();
		private Optional<QueryCompiler> compiler = Optional.empty();
		private Optional<QueryBuilder> builder = Optional.empty();

		public Builder setSources(QueriesSources sources) {
			this.sources = Optional.of(sources);
			return this;
		}

		public Builder setCache(QueriesCache cache) {
			this.cache = Optional.of(cache);
			return this;
		}

		public Builder setParser(QueriesParser parser) {
			this.parser = Optional.of(parser);
			return this;
		}

		public Builder setCompiler(QueryCompiler compiler) {
			this.compiler = Optional.of(compiler);
			return this;
		}

		public Builder setBuilder(QueryBuilder builder) {
			this.builder = Optional.of(builder);
			return this;
		}

		public Builder setOriginalArgumentCommented(boolean isOriginalArgumentCommented) {
			this.config = this.config.setOriginalArgumentCommented(isOriginalArgumentCommented);
			return this;
		}

		public Queries build() {
			return new QueriesImpl(config, sources.orElse(QueriesSources.empty()), cache.orElse(QueriesCache.noCache()),
					parser.orElse(QueriesParser.createDefault()), compiler.orElse(QueryCompiler.createDefault(config)),
					builder.orElse(QueryBuilder.createDefault()));
		}
	}
}
