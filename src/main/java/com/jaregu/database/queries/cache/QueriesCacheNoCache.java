package com.jaregu.database.queries.cache;

import java.util.function.Supplier;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

public class QueriesCacheNoCache implements QueriesCache {

	private static final QueriesCache INSTANCE = new QueriesCacheNoCache();

	private QueriesCacheNoCache() {
	}

	@Override
	public ParsedQueries getParsedQueries(SourceId sourceId, Supplier<ParsedQueries> queriesSupplier) {
		return queriesSupplier.get();
	}

	@Override
	public PreparedQuery getPreparedQuery(QueryId queryId, Supplier<PreparedQuery> querySupplier) {
		return querySupplier.get();
	}

	public static QueriesCache getInstance() {
		return INSTANCE;
	}
}
