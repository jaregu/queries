package com.jaregu.database.queries.parsing;

import com.jaregu.database.queries.QueryException;

@SuppressWarnings("serial")
public class QueriesParseException extends QueryException {

	public QueriesParseException(Throwable cause) {
		super(cause);
	}

	public QueriesParseException(String message) {
		super(message);
	}

	public QueriesParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
