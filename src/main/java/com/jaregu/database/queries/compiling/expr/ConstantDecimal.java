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
	public Object multiply(Operand operand) {
		return withNumber(operand, super::multiply, (v, o) -> v.multiply(o));
	}

	@Override
	public Object divide(Operand operand) {
		return withNumber(operand, super::divide, (v, o) -> v.divide(o));
	}

	@Override
	public Object add(Operand operand) {
		return withNumber(operand, super::add, (v, o) -> v.add(o));
	}

	@Override
	public Object subtract(Operand operand) {
		return withNumber(operand, super::subtract, (v, o) -> v.subtract(o));
	}

	@Override
	public boolean greater(Operand operand) {
		return withNumber(operand, super::greater, (v, o) -> v.compareTo(o) > 0);
	}

	@Override
	public boolean greaterOrEqual(Operand operand) {
		return withNumber(operand, super::greaterOrEqual, (v, o) -> v.compareTo(o) >= 0);
	}

	@Override
	public boolean lesser(Operand operand) {
		return withNumber(operand, super::lesser, (v, o) -> v.compareTo(o) < 0);
	}

	@Override
	public boolean lesserOrEqual(Operand operand) {
		return withNumber(operand, super::lesserOrEqual, (v, o) -> v.compareTo(o) <= 0);
	}

	@Override
	public boolean equal(Operand operand) {
		return withNumber(operand, super::equal, (v, o) -> v.compareTo(o) == 0);
	}

	@Override
	public boolean notEqual(Operand operand) {
		return withNumber(operand, super::notEqual, (v, o) -> v.compareTo(o) != 0);
	}

	private <T> T withNumber(Operand operand, Function<Operand, T> defaultCall,
			BiFunction<BigDecimal, BigDecimal, T> function) {
		Object otherValue = operand.getValue();
		if (otherValue != null && otherValue instanceof Number) {
			return function.apply(getValue(), new BigDecimal(((Number) otherValue).toString()));
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
