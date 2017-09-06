package com.jaregu.database.queries.compiling.expr;

public abstract class EvaluableOperand implements Operand {

	private Constant getValueConstant() {
		return Constant.of(getValue());
	}

	@Override
	public Object multiply(Operand object) {
		return getValueConstant().multiply(object);
	}

	@Override
	public Object divide(Operand object) {
		return getValueConstant().divide(object);
	}

	@Override
	public Object add(Operand object) {
		return getValueConstant().add(object);
	}

	@Override
	public Object subtract(Operand object) {
		return getValueConstant().subtract(object);
	}

	@Override
	public boolean greater(Operand object) {
		return getValueConstant().greater(object);
	}

	@Override
	public boolean greaterOrEqual(Operand object) {
		return getValueConstant().greaterOrEqual(object);
	}

	@Override
	public boolean lesser(Operand object) {
		return getValueConstant().lesser(object);
	}

	@Override
	public boolean lesserOrEqual(Operand object) {
		return getValueConstant().lesserOrEqual(object);
	}

	@Override
	public boolean equal(Operand object) {
		return getValueConstant().equal(object);
	}

	@Override
	public boolean notEqual(Operand object) {
		return getValueConstant().notEqual(object);
	}

	@Override
	public boolean and(Operand object) {
		return getValueConstant().and(object);
	}

	@Override
	public boolean or(Operand object) {
		return getValueConstant().or(object);
	}

	@Override
	public boolean not() {
		return getValueConstant().not();
	}

	@Override
	public Object ternary(Operand first, Operand second) {
		return getValueConstant().ternary(first, second);
	}

	@Override
	public Object assign(Operand object) {
		return getValueConstant().assign(object);
	}
}
