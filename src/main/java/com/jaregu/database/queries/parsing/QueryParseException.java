package com.jaregu.database.queries.parsing;

import com.jaregu.database.queries.QueryException;

@SuppressWarnings("serial")
public class QueryParseException extends QueryException {

	public QueryParseException(Throwable cause) {
		super(cause);
	}

	public QueryParseException(String message) {
		super(message);
	}

	public QueryParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
