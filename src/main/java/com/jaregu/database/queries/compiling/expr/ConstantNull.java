package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;

final public class ConstantNull extends ConstantObject {

	private static final ConstantNull instance = new ConstantNull();

	private ConstantNull() {
		super(null);
	}

	public static Optional<Constant> parse(String expression) {
		return "null".equalsIgnoreCase(expression) ? Optional.of(instance) : Optional.empty();
	}

	public static Optional<Constant> of(Object value) {
		return value == null ? Optional.of(instance) : Optional.empty();
	}
}
