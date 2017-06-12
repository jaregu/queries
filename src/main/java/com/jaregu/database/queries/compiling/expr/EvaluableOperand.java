package com.jaregu.database.queries.compiling.expr;

public abstract class EvaluableOperand implements Operand {

	private Constant getValueConstant() {
		return Constant.of(getValue());
	}

	@Override
	public Object multiply(Object object) {
		return getValueConstant().multiply(object);
	}

	@Override
	public Object divide(Object object) {
		return getValueConstant().divide(object);
	}

	@Override
	public Object add(Object object) {
		return getValueConstant().add(object);
	}

	@Override
	public Object subtract(Object object) {
		return getValueConstant().subtract(object);
	}

	@Override
	public boolean greater(Object object) {
		return getValueConstant().greater(object);
	}

	@Override
	public boolean greaterOrEqual(Object object) {
		return getValueConstant().greaterOrEqual(object);
	}

	@Override
	public boolean lesser(Object object) {
		return getValueConstant().lesser(object);
	}

	@Override
	public boolean lesserOrEqual(Object object) {
		return getValueConstant().lesserOrEqual(object);
	}

	@Override
	public boolean equal(Object object) {
		return getValueConstant().equal(object);
	}

	@Override
	public boolean notEqual(Object object) {
		return getValueConstant().notEqual(object);
	}

	@Override
	public boolean and(Object object) {
		return getValueConstant().and(object);
	}

	@Override
	public boolean or(Object object) {
		return getValueConstant().or(object);
	}
}
