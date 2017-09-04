package com.jaregu.database.queries.compiling.expr;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConstantBaseImpl<T> implements Constant {

	final private T value;

	protected ConstantBaseImpl(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public Object multiply(Operand object) {
		throw new ExpressionEvalException(getName() + " can not be multiplied by " + object.getValue());
	}

	@Override
	public Object divide(Operand object) {
		throw new ExpressionEvalException(getName() + " can not be divided by " + object.getValue());
	}

	@Override
	public Object add(Operand object) {
		throw new ExpressionEvalException(object.getValue() + " can not be added to " + getName());
	}

	@Override
	public Object subtract(Operand object) {
		throw new ExpressionEvalException(object.getValue() + " can not be subtracted from " + getName());
	}

	@Override
	public boolean greater(Operand object) {
		throw new ExpressionEvalException(
				getName() + " does not support greater comparision with: " + object.getValue());
	}

	@Override
	public boolean greaterOrEqual(Operand object) {
		throw new ExpressionEvalException(
				getName() + " does not support greater or equal comparision with: " + object.getValue());
	}

	@Override
	public boolean lesser(Operand object) {
		throw new ExpressionEvalException(
				getName() + " does not support lesser comparision with: " + object.getValue());
	}

	@Override
	public boolean lesserOrEqual(Operand object) {
		throw new ExpressionEvalException(
				getName() + " does not support lesser or equal comparision with: " + object.getValue());
	}

	@Override
	public boolean and(Operand object) {
		throw new ExpressionEvalException(getName() + " does not support logical AND with: " + object.getValue());
	}

	@Override
	public boolean or(Operand object) {
		throw new ExpressionEvalException(getName() + " does not support logical OR with: " + object.getValue());
	}

	@Override
	public boolean equal(Operand object) {
		return Objects.equals(getValue(), object.getValue());
	}

	@Override
	public boolean notEqual(Operand object) {
		return !equal(object);
	}

	private String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return value == null ? "null" : value.toString();
	}

	@Override
	public List<String> getVariableNames() {
		return Collections.emptyList();
	}
}
