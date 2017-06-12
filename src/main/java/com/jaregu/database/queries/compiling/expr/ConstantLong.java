package com.jaregu.database.queries.compiling.expr;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConstantLong extends ConstantBaseImpl<Long> {

	public ConstantLong(Long value) {
		super(value);
	}

	@Override
	public Object multiply(Object object) {
		return withNumber(object, super::multiply, (v, o) -> v * o);
	}

	@Override
	public Object divide(Object object) {
		return withNumber(object, super::divide, (v, o) -> v / o);
	}

	@Override
	public Object add(Object object) {
		return withNumber(object, super::add, (v, o) -> v + o);
	}

	@Override
	public Object subtract(Object object) {
		return withNumber(object, super::subtract, (v, o) -> v - o);
	}

	@Override
	public boolean greater(Object object) {
		return withNumber(object, super::greater, (v, o) -> v > o);
	}

	@Override
	public boolean greaterOrEqual(Object object) {
		return withNumber(object, super::greaterOrEqual, (v, o) -> v >= o);
	}

	@Override
	public boolean lesser(Object object) {
		return withNumber(object, super::lesser, (v, o) -> v < o);
	}

	@Override
	public boolean lesserOrEqual(Object object) {
		return withNumber(object, super::lesserOrEqual, (v, o) -> v <= o);
	}

	@Override
	public boolean equal(Object object) {
		return withNumber(object, super::lesserOrEqual, (v, o) -> v.compareTo(o) == 0);
	}

	@Override
	public boolean notEqual(Object object) {
		return withNumber(object, super::lesserOrEqual, (v, o) -> v.compareTo(o) != 0);
	}

	private <T> T withNumber(Object operand, Function<Object, T> defaultCall, BiFunction<Long, Long, T> function) {
		if (operand != null && operand instanceof Number) {
			return function.apply(getValue(), ((Number) operand).longValue());
		} else {
			return defaultCall.apply(operand);
		}
	}

	public static Optional<Constant> parse(String expression) {
		try {
			long longValue = Long.parseLong(expression);
			return Optional.of(new ConstantLong(longValue));
		} catch (NumberFormatException nfe) {
			return Optional.empty();
		}
	}

	public static Optional<Constant> of(Object value) {
		if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
			return Optional.of(new ConstantLong(((Number) value).longValue()));
		} else {
			return Optional.empty();
		}
	}
}
