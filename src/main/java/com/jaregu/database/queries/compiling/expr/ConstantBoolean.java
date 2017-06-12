package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConstantBoolean extends ConstantBaseImpl<Boolean> {

	final private static ConstantBoolean TRUE = new ConstantBoolean(true);
	final private static ConstantBoolean FALSE = new ConstantBoolean(false);

	private ConstantBoolean(Boolean value) {
		super(value);
	}

	public static Optional<Constant> parse(String expression) {
		if ("true".equalsIgnoreCase(expression) || "false".equalsIgnoreCase(expression)) {
			return Optional.of("true".equalsIgnoreCase(expression) ? TRUE : FALSE);
		} else {
			return Optional.empty();
		}
	}

	public static Optional<Constant> of(Object value) {
		if (value instanceof Boolean) {
			return Optional.of(((Boolean) value).booleanValue() ? TRUE : FALSE);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public boolean and(Object object) {
		return withBoolean(object, super::and, (v, o) -> v && o);
	}

	@Override
	public boolean or(Object object) {
		return withBoolean(object, super::or, (v, o) -> v || o);
	}

	private <T> T withBoolean(Object operand, Function<Object, T> defaultCall,
			BiFunction<Boolean, Boolean, T> function) {
		if (operand != null && operand instanceof Boolean) {
			return function.apply(getValue(), (Boolean) operand);
		} else {
			return defaultCall.apply(operand);
		}
	}
}
