package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;

public final class ConstantBoolean extends ConstantBaseImpl<Boolean> {

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
	public boolean and(Operand operand) {
		if (getValue()) {
			Object otherValue = operand.getValue();
			if (otherValue != null && otherValue instanceof Boolean) {
				return (Boolean) otherValue;
			} else {
				return super.and(operand);
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean or(Operand operand) {
		if (getValue()) {
			return true;
		} else {
			Object otherValue = operand.getValue();
			if (otherValue != null && otherValue instanceof Boolean) {
				return (Boolean) otherValue;
			} else {
				return super.and(operand);
			}
		}
	}

	@Override
	public Object ternary(Operand first, Operand second) {
		if (getValue()) {
			return first.getValue();
		} else {
			return second.getValue();
		}
	}

	@Override
	public boolean not() {
		return !getValue();
	}
}
