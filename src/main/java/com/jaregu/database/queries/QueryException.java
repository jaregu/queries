package com.jaregu.database.queries;

/**
 * Base class for all of query parser exceptions.
 */
@SuppressWarnings("serial")
public class QueryException extends RuntimeException {

	public QueryException(String message) {
		super(message);
	}

	public QueryException(Throwable cause) {
		super(cause);
	}

	public QueryException(String message, Throwable cause) {
		super(message, cause);
	}
}