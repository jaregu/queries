package com.jaregu.database.queries.building;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Orderable;

public interface ToOrderedQuery {

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toOrderedQuery(Query, Orderable)}
	 */
	Query toOrderedQuery(Orderable orderable);

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * configured ({@link Dialect}) implementation.
	 * 
	 * See {@link Dialect#toOrderedQuery(Query, Orderable)}
	 */
	default Query toOrderedQuery(String... orderByItems) {
		return toOrderedQuery(orderByItems == null ? () -> Collections.emptyList() : () -> Arrays.asList(orderByItems));
	}

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toOrderedQuery(Query, Orderable)}
	 */
	default Query toOrderedQuery(Iterable<String> orderByItems) {
		return toOrderedQuery(orderByItems == null ? () -> Collections.emptyList()
				: () -> StreamSupport.stream(orderByItems.spliterator(), false).collect(Collectors.toList()));
	}
}
