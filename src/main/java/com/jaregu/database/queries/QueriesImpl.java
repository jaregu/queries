package com.jaregu.database.queries;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.compiling.QueryCompiler;
import com.jaregu.database.queries.parsing.ParsedQueries;
import com.jaregu.database.queries.parsing.ParsedQuery;
import com.jaregu.database.queries.parsing.QueriesParser;
import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.parsing.QueriesSources;

public final class QueriesImpl implements Queries {

	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final QueriesSources sources;
	private final QueriesParser parser;
	private final QueryCompiler compiler;
	private final QueriesCache cache;

	private volatile Map<SourceId, QueriesSource> sourcesById;

	QueriesImpl(QueriesSources sources, QueriesConfig config) {
		this.sources = sources;
		this.parser = QueriesParser.create();
		this.compiler = QueryCompiler.of(config);
		this.cache = config.getCache();
	}

	@Override
	public PreparedQuery get(QueryId queryId) {
		return cache.getPreparedQuery(queryId, this::prepareQuery);
	}

	@Override
	public RetativeQueries ofSource(SourceId sourceId) {
		return new RetativeQueries() {

			@Override
			public PreparedQuery get(String id) {
				return QueriesImpl.this.get(sourceId.getQueryId(id));
			}
		};
	}

	private PreparedQuery prepareQuery(QueryId queryId) {
		ParsedQuery sourceQuery = cache.getParsedQueries(queryId.getSourceId(), this::parseQueries).get(queryId);
		log.debug("Starting to compile queryId: {}!", queryId);
		return compiler.compile(sourceQuery);
	}

	private ParsedQueries parseQueries(SourceId sourceId) {
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
							.collect(Collectors.toMap(QueriesSource::getId, Function.identity()));
					this.sourcesById = sourcesById;
				}
			}
		}
		return sourcesById;
	}
}
