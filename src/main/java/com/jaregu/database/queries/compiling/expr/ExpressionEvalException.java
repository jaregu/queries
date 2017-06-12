package com.jaregu.database.queries.compiling.expr;

@SuppressWarnings("serial")
public class ExpressionEvalException extends ExpressionException {

	final private String expression = EvaluationContext.peekCurrent().map(EvaluationContext::getBaseExpression)
			.map(Object::toString).orElse(null);

	public ExpressionEvalException(Throwable cause) {
		super(cause);
	}

	public ExpressionEvalException(String message) {
		super(message);
	}

	public ExpressionEvalException(String message, Throwable cause) {
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
