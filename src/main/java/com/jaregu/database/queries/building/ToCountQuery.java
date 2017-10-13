package com.jaregu.database.queries.building;

import com.jaregu.database.queries.dialect.Dialect;

public interface ToCountQuery {

	/**
	 * Returns new query which has original query wrapped as sub-query inside
	 * <code>COUNT</code> query using configured ({@link Dialect})
	 * implementation
	 * 
	 * See {@link Dialect#toCountQuery(Query)}
	 * 
	 * @return
	 */
	Query toCountQuery();
}
