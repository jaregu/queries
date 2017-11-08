package com.jaregu.database.queries.proxy;

import com.jaregu.database.queries.building.Query;

@FunctionalInterface
public interface QueryConverter {

	/**
	 * Converts query to other query
	 * 
	 */
	Query convert(Query source, Object[] invocationArgs);
}
