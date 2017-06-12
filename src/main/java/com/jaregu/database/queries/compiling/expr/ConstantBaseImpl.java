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
	public Object multiply(Object object) {
		throw new ExpressionEvalException(getName() + " can not be multiplied by " + object);
	}

	@Override
	public Object divide(Object object) {
		throw new ExpressionEvalException(getName() + " can not be divided by " + object);
	}

	@Override
	public Object add(Object object) {
		throw new ExpressionEvalException(object + " can not be added to " + getName());
	}

	@Override
	public Object subtract(Object object) {
		throw new ExpressionEvalException(object + " can not be subtracted from " + getName());
	}

	@Override
	public boolean greater(Object object) {
		throw new ExpressionEvalException(getName() + " does not support greater comparision with: " + object);
	}

	@Override
	public boolean greaterOrEqual(Object object) {
		throw new ExpressionEvalException(getName() + " does not support greater or equal comparision with: " + object);
	}

	@Override
	public boolean lesser(Object object) {
		throw new ExpressionEvalException(getName() + " does not support lesser comparision with: " + object);
	}

	@Override
	public boolean lesserOrEqual(Object object) {
		throw new ExpressionEvalException(getName() + " does not support lesser or equal comparision with: " + object);
	}

	@Override
	public boolean and(Object object) {
		throw new ExpressionEvalException(getName() + " does not support logical AND with: " + object);
	}

	@Override
	public boolean or(Object object) {
		throw new ExpressionEvalException(getName() + " does not support logical OR with: " + object);
	}

	@Override
	public boolean equal(Object object) {
		return Objects.equals(getValue(), object);
	}

	@Override
	public boolean notEqual(Object object) {
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
