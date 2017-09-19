package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ConstantInteger extends ConstantBaseImpl<Integer> {

	public ConstantInteger(Integer value) {
		super(value);
	}

	@Override
	public Object multiply(Operand object) {
		return withNumber(object, super::multiply, (v, o) -> v * o);
	}

	@Override
	public Object divide(Operand object) {
		return withNumber(object, super::divide, (v, o) -> v / o);
	}

	@Override
	public Object add(Operand object) {
		return withNumber(object, super::add, (v, o) -> v + o);
	}

	@Override
	public Object subtract(Operand object) {
		return withNumber(object, super::subtract, (v, o) -> v - o);
	}

	@Override
	public boolean greater(Operand object) {
		return withNumber(object, super::greater, (v, o) -> v > o);
	}

	@Override
	public boolean greaterOrEqual(Operand object) {
		return withNumber(object, super::greaterOrEqual, (v, o) -> v >= o);
	}

	@Override
	public boolean lesser(Operand object) {
		return withNumber(object, super::lesser, (v, o) -> v < o);
	}

	@Override
	public boolean lesserOrEqual(Operand object) {
		return withNumber(object, super::lesserOrEqual, (v, o) -> v <= o);
	}

	@Override
	public boolean equal(Operand object) {
		return withNumber(object, super::equal, (v, o) -> v.compareTo(o) == 0);
	}

	@Override
	public boolean notEqual(Operand object) {
		return withNumber(object, super::notEqual, (v, o) -> v.compareTo(o) != 0);
	}

	private <T> T withNumber(Operand operand, Function<Operand, T> defaultCall,
			BiFunction<Integer, Integer, T> function) {
		Object otherValue = operand.getValue();
		if (otherValue != null && otherValue instanceof Number) {
			return function.apply(getValue(), ((Number) otherValue).intValue());
		} else {
			return defaultCall.apply(operand);
		}
	}

	public static Optional<Constant> parse(String expression) {
		try {
			int longValue = Integer.parseInt(expression);
			return Optional.of(new ConstantInteger(longValue));
		} catch (NumberFormatException nfe) {
			return Optional.empty();
		}
	}

	public static Optional<Constant> of(Object value) {
		if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
			return Optional.of(new ConstantInteger(((Number) value).intValue()));
		} else {
			return Optional.empty();
		}
	}
}
