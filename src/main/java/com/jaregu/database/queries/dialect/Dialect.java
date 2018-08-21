package com.jaregu.database.queries.dialect;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.building.Query;

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
	 * Returns new query with dialect specific <code>LIMIT, OFFSET</code>
	 * functionality clause added using given {@link Pageable}
	 * 
	 * @param query
	 * @param pageable
	 * @return new {@link Query} which has paging part added
	 */
	Query toPagedQuery(Query query, Pageable pageable);

	/**
	 * Returns new query with <code>ORDER BY</code> clause added using given
	 * {@link Orderable} items
	 * 
	 * @param query
	 * @param orderable
	 * @return new {@link Query} with <code>ORDER BY</code> clause added
	 */
	Query toOrderedQuery(Query query, Orderable orderable);

	/**
	 * Returns new query with original query wrapped as <code>COUNT</code>
	 * sub-query like:
	 * 
	 * <pre>
	 * SELECT COUNT(1) FROM ({original_query})
	 * </pre>
	 * 
	 * @param query
	 * @return new {@link Query} wrapped in COUNT query
	 */
	Query toCountQuery(Query query);
}
