package com.jaregu.database.queries.compiling.expr;

import java.util.Objects;

public class ConstantBaseImpl<T> extends OperandBaseImpl implements Constant {

	final private T value;

	protected ConstantBaseImpl(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public boolean equal(Operand object) {
		return Objects.equals(getValue(), object.getValue());
	}

	@Override
	public boolean notEqual(Operand object) {
		return !equal(object);
	}

	@Override
	public String toString() {
		return value == null ? "null" : value.toString();
	}
}
