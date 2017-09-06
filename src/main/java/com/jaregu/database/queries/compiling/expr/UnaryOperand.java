package com.jaregu.database.queries.compiling.expr;

import java.util.List;

public final class UnaryOperand extends EvaluableOperand implements Operand {

	private Operator operator;
	private Operand operand;

	public UnaryOperand(Operator operator, Operand operand) {
		if (!operator.isUnary()) {
			throw new IllegalArgumentException("Operator: " + operator + " is no unary!");
		}
		this.operator = operator;
		this.operand = operand;
	}

	@Override
	public Object getValue() {
		return operator.getUnary().apply(operand);
	}

	@Override
	public List<String> getVariableNames() {
		return operand.getVariableNames();
	}
}
