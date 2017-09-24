package com.jaregu.database.queries.cache;

import java.util.function.Function;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

public class QueriesCacheNoCache implements QueriesCache {

	private static final QueriesCache INSTANCE = new QueriesCacheNoCache();

	private QueriesCacheNoCache() {
	}

	@Override
	public ParsedQueries getParsedQueries(SourceId sourceId, Function<SourceId, ParsedQueries> queriesSupplier) {
		return queriesSupplier.apply(sourceId);
	}

	@Override
	public PreparedQuery getPreparedQuery(QueryId queryId, Function<QueryId, PreparedQuery> querySupplier) {
		return querySupplier.apply(queryId);
	}

	public static QueriesCache getInstance() {
		return INSTANCE;
	}
}
