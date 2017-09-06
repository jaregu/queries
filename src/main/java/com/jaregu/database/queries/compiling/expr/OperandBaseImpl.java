package com.jaregu.database.queries.compiling.expr;

import java.util.Collections;
import java.util.List;

public abstract class OperandBaseImpl implements Operand {

	@Override
	public Object getValue() {
		throw new ExpressionEvalException(getName() + " has no value!");
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
	public boolean not() {
		throw new ExpressionEvalException(getName() + " does not support logical NOT!");
	}

	@Override
	public Object ternary(Operand first, Operand second) {
		throw new ExpressionEvalException(
				getName() + " does not support logical ternary (cond ? val_if_true : val_if_false) with: "
						+ first.getValue() + " : " + second.getValue());
	}

	@Override
	public Object assign(Operand object) {
		throw new ExpressionEvalException(getName() + " does not support assignment with: " + object.getValue());
	}

	@Override
	public boolean equal(Operand object) {
		throw new ExpressionEvalException(getName() + " does not support equality with: " + object.getValue());
	}

	@Override
	public boolean notEqual(Operand object) {
		throw new ExpressionEvalException(getName() + " does not support not equality with: " + object.getValue());
	}

	private String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public List<String> getVariableNames() {
		return Collections.emptyList();
	}
}
