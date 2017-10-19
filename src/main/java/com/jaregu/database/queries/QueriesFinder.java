package com.jaregu.database.queries;

import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.PreparedQuery;

public interface QueriesFinder<T> {

	/**
	 * Returns cached {@link PreparedQuery} (see {@link QueriesCache}) or
	 * prepare one using supplied SQL sources.
	 * 
	 */
	PreparedQuery get(T id);
}
