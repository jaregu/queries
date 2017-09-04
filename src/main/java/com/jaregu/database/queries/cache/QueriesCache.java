package com.jaregu.database.queries.cache;

import java.util.function.Function;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

public interface QueriesCache {

	ParsedQueries getParsedQueries(SourceId sourceId, Function<SourceId, ParsedQueries> queriesSupplier);

	PreparedQuery getPreparedQuery(QueryId queryId, Function<QueryId, PreparedQuery> querySupplier);

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
