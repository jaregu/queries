package com.jaregu.database.queries.cache;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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
	public ParsedQueries getParsedQueries(SourceId sourceId, Function<SourceId, ParsedQueries> queriesSupplier) {
		return sources.computeIfAbsent(sourceId, queriesSupplier);
	}

	@Override
	public PreparedQuery getPreparedQuery(QueryId queryId, Function<QueryId, PreparedQuery> querySupplier) {
		return queries.computeIfAbsent(queryId, querySupplier);
	}

	@Override
	public void invalidate(SourceId sourceId) {
		synchronized (this) {
			sources.remove(sourceId);
			Iterator<Entry<QueryId, PreparedQuery>> iter = queries.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<QueryId, PreparedQuery> entry = iter.next();
				if (entry.getKey().getSourceId().equals(sourceId)) {
					iter.remove();
				}
			}
		}
	}

	@Override
	public void invalidate(QueryId queryId) {
		synchronized (this) {
			sources.remove(queryId.getSourceId());
			queries.remove(queryId);
		}
	}

	@Override
	public void invalidateAll() {
		synchronized (this) {
			sources.clear();
			queries.clear();
		}
	}
}
