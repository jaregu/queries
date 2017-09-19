package com.jaregu.database.queries.compiling.expr;

import java.util.List;

public interface ExpressionParser {

	/**
	 * Fast way to determine if passed expression looks like expression, it is
	 * possible that expression will contain errors.
	 * 
	 * @param expression
	 * @return
	 */
	boolean isLikeExpression(String expression);

	List<Expression> parse(String expression) throws ExpressionParseException;

	static ExpressionParser createDefault() {
		return new ExpressionParserImpl();
	}
}
