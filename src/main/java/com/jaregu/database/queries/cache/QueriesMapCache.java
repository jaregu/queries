package com.jaregu.database.queries.cache;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.CompiledQuery;
import com.jaregu.database.queries.parsing.SourceQueries;

public class QueriesMapCache implements QueriesCache {

	private ConcurrentHashMap<SourceId, SourceQueries> sources = new ConcurrentHashMap<>();
	private ConcurrentHashMap<QueryId, CompiledQuery> queries = new ConcurrentHashMap<>();

	public QueriesMapCache() {
	}

	@Override
	public SourceQueries getSourceQueries(SourceId sourceId, Function<SourceId, SourceQueries> queriesSupplier) {
		return sources.computeIfAbsent(sourceId, queriesSupplier);
	}

	@Override
	public CompiledQuery getCompiledQuery(QueryId queryId, Function<QueryId, CompiledQuery> querySupplier) {
		return queries.computeIfAbsent(queryId, querySupplier);
	}

	@Override
	public void invalidate(SourceId sourceId) {
		synchronized (this) {
			sources.remove(sourceId);
			Iterator<Entry<QueryId, CompiledQuery>> iter = queries.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<QueryId, CompiledQuery> entry = iter.next();
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
