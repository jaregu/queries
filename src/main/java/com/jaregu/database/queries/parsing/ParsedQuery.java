package com.jaregu.database.queries.parsing;

import java.util.List;

import com.jaregu.database.queries.QueryId;

public interface ParsedQuery {

	QueryId getQueryId();

	List<ParsedQueryPart> getParts();

	/**
	 * Returns true if this query was marked as a multi-statement batch
	 * (e.g. {@code -- name (batch)}). Batch queries keep their
	 * {@code ;} separators and ship to JDBC as one literal batch string
	 * instead of being split into multiple statements.
	 *
	 * @return true if this query is a batch query, false otherwise
	 */
	default boolean isBatch() {
		return false;
	}
}
