package com.jaregu.database.queries.cache;

import java.util.function.Function;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.CompiledQuery;
import com.jaregu.database.queries.parsing.SourceQueries;

public class QueriesCacheNoCache implements QueriesCache {

	private static final QueriesCache INSTANCE = new QueriesCacheNoCache();

	private QueriesCacheNoCache() {
	}

	@Override
	public SourceQueries getSourceQueries(SourceId sourceId, Function<SourceId, SourceQueries> queriesSupplier) {
		return queriesSupplier.apply(sourceId);
	}

	@Override
	public CompiledQuery getCompiledQuery(QueryId queryId, Function<QueryId, CompiledQuery> querySupplier) {
		return querySupplier.apply(queryId);
	}

	@Override
	public void invalidate(QueryId queryId) {
	}

	@Override
	public void invalidate(SourceId sourceId) {
	}

	@Override
	public void invalidateAll() {
	}

	public static QueriesCache getInstance() {
		return INSTANCE;
	}
}
