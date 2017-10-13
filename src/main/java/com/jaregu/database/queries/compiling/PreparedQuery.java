package com.jaregu.database.queries.compiling;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryBuilder;

/**
 * Represent immutable (thread safe) compiled/prepared SQL statement
 * <i>logic</i> used to build SQL statement representations. Can be cached for
 * subsequent use. Calling one of <code>build</code> methods returns
 * {@link Query} instance which can be used in database executing layer.
 *
 */
public interface PreparedQuery extends QueryBuilder<Query> {

	/**
	 * This query identification, each query has unique ID
	 * 
	 * @return
	 */
	QueryId getQueryId();

	@Override
	Query build(ParametersResolver resolver);
}
