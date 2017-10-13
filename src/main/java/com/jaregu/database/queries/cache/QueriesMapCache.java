package com.jaregu.database.queries.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

public class QueriesMapCache implements QueriesCache {

	private ConcurrentHashMap<SourceId, ParsedQueries> sources = new ConcurrentHashMap<>();
	private ConcurrentHashMap<QueryId, PreparedQuery> queries = new ConcurrentHashMap<>();

	public QueriesMapCache() {
	}

	@Override
	public ParsedQueries getParsedQueries(SourceId sourceId, Supplier<ParsedQueries> queriesSupplier) {
		return sources.computeIfAbsent(sourceId, (k) -> queriesSupplier.get());
	}

	@Override
	public PreparedQuery getPreparedQuery(QueryId queryId, Supplier<PreparedQuery> querySupplier) {
		return queries.computeIfAbsent(queryId, (k) -> querySupplier.get());
	}
}
