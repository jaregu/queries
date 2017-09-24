package com.jaregu.database.queries.dialect;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.ext.OffsetLimit;
import com.jaregu.database.queries.ext.SortProperties;

/**
 * Represents database dialect used to build some database specific query
 * features.
 * <p>
 * Dialect is using existing query to create new query with additional SQL part
 * added in database specific syntax. New query contains some SQL part addition
 * like <code>ORDER BY</code> clause or <code>LIMIT ? OFFSET ?</code>
 * functionality.
 * <p>
 * 
 * Use {@link Queries#builder()} <code>dialect...</code> methods for configuring
 * correct dialect. See {@link Dialects} for all possible built-in values or
 * supply your own implementation.
 * <p>
 * 
 * See all <code>to...()</code> methods for all available conversions.
 * <p>
 * 
 */
public interface Dialect {

	/**
	 * 
	 * Returns new query which has dialect specific <code>LIMIT, OFFSET</code>
	 * part added using passed {@link OffsetLimit}
	 * 
	 * @param query
	 * @param offsetLimit
	 * @return new {@link Query} which has paging part added
	 */
	Query toPagedQuery(Query query, OffsetLimit offsetLimit);

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * passed ({@link SortProperties})
	 * 
	 * @param query
	 * @param sortProperties
	 * @return new {@link Query} which has <code>ORDER BY</code> clause added
	 */
	Query toSortedQuery(Query query, SortProperties sortProperties);

	/**
	 * Returns new query which has original query wrapped as sub-query inside
	 * <code>COUNT</code> query like
	 * 
	 * <pre>
	 * SELECT COUNT(1) FROM ({original_query})
	 * </pre>
	 * 
	 * @param query
	 * @return new {@link Query} which is count query implementation
	 */
	Query toCountQuery(Query query);
}
