package com.jaregu.database.queries.cache;

import java.util.function.Supplier;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

/**
 * Parsed or prepared query statements cache. If cache doesn't contain value,
 * must call passed supplier to obtain value and before returning it can cache
 * it.
 *
 */
public interface QueriesCache {

	ParsedQueries getParsedQueries(SourceId sourceId, Supplier<ParsedQueries> queriesSupplier);

	PreparedQuery getPreparedQuery(QueryId queryId, Supplier<PreparedQuery> querySupplier);
}
