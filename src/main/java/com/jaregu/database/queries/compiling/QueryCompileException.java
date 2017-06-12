package com.jaregu.database.queries.compiling;

import com.jaregu.database.queries.QueryException;

@SuppressWarnings("serial")
public class QueryCompileException extends QueryException {

	public QueryCompileException(Throwable cause) {
		super(cause);
	}

	public QueryCompileException(String message) {
		super(message);
	}

	public QueryCompileException(String message, Throwable cause) {
		super(message, cause);
	}
}
