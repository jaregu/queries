package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;

public interface Variable extends Operand {

	String getName();

	static Optional<Variable> parse(String expression) {
		final String prefix = ".";
		if (expression.startsWith(prefix)) {
			if (expression.contains(prefix + prefix) || expression.endsWith(prefix)) {
				throw new ExpressionParseException(
						"Can't parse variable expression, variable has naming format problems: " + expression + "!");
			}
			return Optional.of(new VariableImpl(expression.substring(prefix.length())));
		} else {
			return Optional.empty();
		}
	}
}
