package com.jaregu.database.queries.ext.caffeine;

import java.util.function.Supplier;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

/**
 * {@link Caffeine} cache wrapper
 * 
 */
public final class CaffeineCacheWrapper implements QueriesCache {

	private final Cache<SourceId, ParsedQueries> parsedCache;
	private final Cache<QueryId, PreparedQuery> preparedCache;

	public static CaffeineCacheWrapper of(Cache<SourceId, ParsedQueries> parsedCache,
			Cache<QueryId, PreparedQuery> preparedCache) {
		return new CaffeineCacheWrapper(parsedCache, preparedCache);
	}

	CaffeineCacheWrapper(Cache<SourceId, ParsedQueries> parsedCache, Cache<QueryId, PreparedQuery> preparedCache) {
		this.parsedCache = parsedCache;
		this.preparedCache = preparedCache;
	}

	@Override
	public ParsedQueries getParsedQueries(SourceId sourceId, Supplier<ParsedQueries> queriesSupplier) {
		if (parsedCache == null) {
			return queriesSupplier.get();
		} else {
			return parsedCache.get(sourceId, (k) -> queriesSupplier.get());
		}
	}

	@Override
	public PreparedQuery getPreparedQuery(QueryId queryId, Supplier<PreparedQuery> querySupplier) {
		if (preparedCache == null) {
			return querySupplier.get();
		} else {
			return preparedCache.get(queryId, (k) -> querySupplier.get());
		}
	}
}
