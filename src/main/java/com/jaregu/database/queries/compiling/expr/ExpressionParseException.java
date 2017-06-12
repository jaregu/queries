package com.jaregu.database.queries.compiling.expr;

@SuppressWarnings("serial")
public class ExpressionParseException extends ExpressionException {

	final private String expression = ParsingContext.peekCurrent().map(ParsingContext::getExpression)
			.orElse(null);

	public ExpressionParseException(Throwable cause) {
		super(cause);
	}

	public ExpressionParseException(String message) {
		super(message);
	}

	public ExpressionParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		String basicToString = super.toString();
		if (expression != null)
			return basicToString + " (expression: " + expression + ')';
		else
			return basicToString;
	}

}
