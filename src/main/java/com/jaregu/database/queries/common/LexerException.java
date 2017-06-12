package com.jaregu.database.queries.common;

import com.jaregu.database.queries.QueryException;

@SuppressWarnings("serial")
public class LexerException extends QueryException {

	public LexerException(Throwable cause) {
		super(cause);
	}

	public LexerException(String message) {
		super(message);
	}

	public LexerException(String message, Throwable cause) {
		super(message, cause);
	}
}
