package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;

public interface OutputVariable extends Operand {

	String getName();

	static Optional<OutputVariable> parse(String expression) {
		if (Character.isLetter(expression.charAt(0))) {
			for (int i = 1; i < expression.length(); i++) {
				if (!Character.isLetterOrDigit(expression.charAt(i))) {
					return Optional.empty();
				}
			}
			return Optional.of(new OutputVariableImpl(expression));
		} else {
			return Optional.empty();
		}
	}
}
