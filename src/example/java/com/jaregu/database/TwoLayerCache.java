package com.jaregu.database;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.parsing.ParsedQueries;

/**
 * Example cache where prepared queries are cached in two layers. If some query
 * is asked more than one time in firstLayerExpireMin minutes, then it is cashed
 * in second layer for longer (secondLayerExpireMin) period
 * 
 * @author
 *
 */
public class TwoLayerCache implements QueriesCache {

	private Cache<QueryId, PreparedQuery> shortTermCache;
	private Cache<QueryId, PreparedQuery> longTermCache;

	public static TwoLayerCache of(int firstLayerCapacity, int firstLayerExpireMin, int secondLayerCapacity,
			int secondLayerExpireMin) {
		return new TwoLayerCache(firstLayerCapacity, firstLayerExpireMin, secondLayerCapacity, secondLayerExpireMin);
	}

	TwoLayerCache(int firstLayerCapacity, int firstLayerExpireMin, int secondLayerCapacity, int secondLayerExpireMin) {
		shortTermCache = Caffeine.newBuilder().expireAfterAccess(firstLayerExpireMin, TimeUnit.MINUTES)
				.maximumSize(firstLayerCapacity).build();
		longTermCache = Caffeine.newBuilder().expireAfterAccess(secondLayerExpireMin, TimeUnit.MINUTES)
				.maximumSize(secondLayerCapacity).build();
	}

	@Override
	public ParsedQueries getParsedQueries(SourceId sourceId, Function<SourceId, ParsedQueries> queriesSupplier) {
		return queriesSupplier.apply(sourceId);
	}

	@Override
	public PreparedQuery getPreparedQuery(QueryId queryId, Function<QueryId, PreparedQuery> querySupplier) {
		PreparedQuery query = longTermCache.getIfPresent(queryId);
		if (query == null) {
			query = shortTermCache.getIfPresent(queryId);
			if (query != null) {
				longTermCache.put(queryId, query);
			} else {
				shortTermCache.get(queryId, querySupplier);
			}
		}
		return query;
	}
}
