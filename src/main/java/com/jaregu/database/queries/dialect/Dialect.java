package com.jaregu.database.queries.dialect;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.parsing.QueriesSource;

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
	 * Returns suffix associated with this dialect. Used to configure some
	 * dialect specific source files using {@link QueriesSource#ofClass(Class)}
	 * 
	 * <p>
	 * When {@link Queries} is configured with some dialect
	 * {@link Queries.Builder#dialect(Dialect)}, then source files used together
	 * with dialect suffix (for example: someFile.mariadb.sql) are used instead
	 * of default file (someFile.sql)
	 * 
	 * @return
	 */
	String getSuffix();

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
