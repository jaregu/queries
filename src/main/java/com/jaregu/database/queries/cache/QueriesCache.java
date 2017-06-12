package com.jaregu.database.queries.cache;

import java.util.function.Function;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.CompiledQuery;
import com.jaregu.database.queries.parsing.SourceQueries;

public interface QueriesCache {

	SourceQueries getSourceQueries(SourceId sourceId, Function<SourceId, SourceQueries> queriesSupplier);

	CompiledQuery getCompiledQuery(QueryId queryId, Function<QueryId, CompiledQuery> querySupplier);

	void invalidate(QueryId queryId);

	void invalidate(SourceId sourceId);

	void invalidateAll();

	static QueriesCache noCache() {
		return QueriesCacheNoCache.getInstance();
	}

	static QueriesCache newMapCache() {
		return new QueriesMapCache();
	}
}