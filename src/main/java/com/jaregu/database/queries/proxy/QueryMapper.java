package com.jaregu.database.queries.proxy;

import com.jaregu.database.queries.building.Query;

@FunctionalInterface
public interface QueryMapper<T> {

	/**
	 * Converts query to other result
	 * 
	 */
	T map(Query source, Object[] invocationArgs);
}
