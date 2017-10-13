package com.jaregu.database.queries.proxy;

import com.jaregu.database.queries.QueryException;

@SuppressWarnings("serial")
public class QueryProxyException extends QueryException {

	public QueryProxyException(Throwable cause) {
		super(cause);
	}

	public QueryProxyException(String message) {
		super(message);
	}

	public QueryProxyException(String message, Throwable cause) {
		super(message, cause);
	}
}