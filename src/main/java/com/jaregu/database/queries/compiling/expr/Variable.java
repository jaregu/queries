package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;

public interface Variable extends Operand {

	String getName();

	static Optional<Variable> parse(String expression) {
		final String prefix = ":";
		final String nestedDelimiter = ".";
		if (expression.startsWith(prefix)) {
			String variableName = expression.substring(prefix.length());
			if (variableName.contains(prefix) || variableName.startsWith(nestedDelimiter)
					|| expression.endsWith(nestedDelimiter)
					|| variableName.contains(nestedDelimiter + nestedDelimiter)) {
				throw new ExpressionParseException(
						"Can't parse variable expression, variable has naming format problems: " + expression + "!");
			}
			return Optional.of(new VariableImpl(variableName));
		} else {
			return Optional.empty();
		}
	}
}
