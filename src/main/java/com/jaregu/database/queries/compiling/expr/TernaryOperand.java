package com.jaregu.database.queries.compiling.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TernaryOperand extends EvaluableOperand implements Operand {

	private Operator operator;
	private Operand first;
	private Operand second;
	private Operand third;
	private List<String> variableNames;

	public TernaryOperand(Operator operator, Operand first, Operand second, Operand third) {
		if (!operator.isTernary()) {
			throw new IllegalArgumentException("Operator: " + operator + " is not ternary operator!");
		}
		this.operator = operator;
		this.first = first;
		this.second = second;
		this.third = third;
	}

	@Override
	public Object getValue() {
		return operator.getTernary().apply(first, second, third);
	}

	@Override
	public List<String> getVariableNames() {
		if (variableNames == null) {
			List<String> variableNames;
			if (first.getVariableNames().isEmpty() && second.getVariableNames().isEmpty()
					&& third.getVariableNames().isEmpty()) {
				variableNames = Collections.emptyList();
			} else {
				variableNames = new ArrayList<>(first.getVariableNames().size() + second.getVariableNames().size());
				variableNames.addAll(first.getVariableNames());
				variableNames.addAll(second.getVariableNames());
				variableNames.addAll(third.getVariableNames());
			}
			this.variableNames = Collections.unmodifiableList(variableNames);
		}
		return this.variableNames;
	}
}
