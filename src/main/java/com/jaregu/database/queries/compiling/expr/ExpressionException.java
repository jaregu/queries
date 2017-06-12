package com.jaregu.database.queries.compiling.expr;

import com.jaregu.database.queries.QueryException;

@SuppressWarnings("serial")
public class ExpressionException extends QueryException {

	public ExpressionException(Throwable cause) {
		super(cause);
	}

	public ExpressionException(String message) {
		super(message);
	}

	public ExpressionException(String message, Throwable cause) {
		super(message, cause);
	}
}
