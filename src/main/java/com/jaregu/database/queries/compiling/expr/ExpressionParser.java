package com.jaregu.database.queries.compiling.expr;

public interface ExpressionParser {

	/**
	 * Fast way to determine if passed expression looks like expression, it is
	 * possible that expression will contain errors.
	 * 
	 * @param expression
	 * @return
	 */
	boolean isLikeExpression(String expression);

	Expression parse(String expression) throws ExpressionParseException;
}
