package com.jaregu.database.queries.building;

import com.jaregu.database.queries.QueryException;

/**
 * Exception thrown when there is problems with {@link Query} building
 */
@SuppressWarnings("serial")
public class QueryBuildException extends QueryException {

	public QueryBuildException(Throwable cause) {
		super(cause);
	}

	public QueryBuildException(String message) {
		super(message);
	}

	public QueryBuildException(String message, Throwable cause) {
		super(message, cause);
	}
}