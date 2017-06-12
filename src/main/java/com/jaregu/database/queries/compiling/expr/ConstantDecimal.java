package com.jaregu.database.queries.compiling.expr;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConstantDecimal extends ConstantBaseImpl<BigDecimal> {

	public ConstantDecimal(BigDecimal value) {
		super(value);
	}

	@Override
	public Object multiply(Object object) {
		return withNumber(object, super::multiply, (v, o) -> v.multiply(o));
	}

	@Override
	public Object divide(Object object) {
		return withNumber(object, super::divide, (v, o) -> v.divide(o));
	}

	@Override
	public Object add(Object object) {
		return withNumber(object, super::add, (v, o) -> v.add(o));
	}

	@Override
	public Object subtract(Object object) {
		return withNumber(object, super::subtract, (v, o) -> v.subtract(o));
	}

	@Override
	public boolean greater(Object object) {
		return withNumber(object, super::greater, (v, o) -> v.compareTo(o) > 0);
	}

	@Override
	public boolean greaterOrEqual(Object object) {
		return withNumber(object, super::greaterOrEqual, (v, o) -> v.compareTo(o) >= 0);
	}

	@Override
	public boolean lesser(Object object) {
		return withNumber(object, super::lesser, (v, o) -> v.compareTo(o) < 0);
	}

	@Override
	public boolean lesserOrEqual(Object object) {
		return withNumber(object, super::lesserOrEqual, (v, o) -> v.compareTo(o) <= 0);
	}

	@Override
	public boolean equal(Object object) {
		return withNumber(object, super::lesserOrEqual, (v, o) -> v.compareTo(o) == 0);
	}

	@Override
	public boolean notEqual(Object object) {
		return withNumber(object, super::lesserOrEqual, (v, o) -> v.compareTo(o) != 0);
	}

	private <T> T withNumber(Object operand, Function<Object, T> defaultCall,
			BiFunction<BigDecimal, BigDecimal, T> function) {
		if (operand != null && operand instanceof Number) {
			return function.apply(getValue(), new BigDecimal(((Number) operand).toString()));
		} else {
			return defaultCall.apply(operand);
		}
	}

	public static Optional<Constant> parse(String expression) {
		try {
			return Optional.of(new ConstantDecimal(new BigDecimal(expression)));
		} catch (NumberFormatException nfe) {
			return Optional.empty();
		}
	}

	public static Optional<Constant> of(Object value) {
		if (value instanceof BigDecimal) {
			return Optional.of(new ConstantDecimal((BigDecimal) value));
		} else if (value instanceof Number) {
			return Optional.of(new ConstantDecimal(new BigDecimal(((Number) value).toString())));
		} else {
			return Optional.empty();
		}
	}
}
