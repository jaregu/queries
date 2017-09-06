package com.jaregu.database.queries.compiling.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BinaryOperand extends EvaluableOperand implements Operand {

	private Operator operator;
	private Operand first;
	private Operand second;
	private List<String> variableNames;

	public BinaryOperand(Operator operator, Operand first, Operand second) {
		if (!operator.isBinary()) {
			throw new IllegalArgumentException("Operator: " + operator + " is no binary!");
		}
		this.operator = operator;
		this.first = first;
		this.second = second;
	}

	@Override
	public Object getValue() {
		return operator.getBinary().apply(first, second);
	}

	@Override
	public List<String> getVariableNames() {
		if (variableNames == null) {
			List<String> variableNames;
			if (first.getVariableNames().isEmpty() && second.getVariableNames().isEmpty()) {
				variableNames = Collections.emptyList();
			} else {
				variableNames = new ArrayList<>(first.getVariableNames().size() + second.getVariableNames().size());
				variableNames.addAll(first.getVariableNames());
				variableNames.addAll(second.getVariableNames());
			}
			this.variableNames = Collections.unmodifiableList(variableNames);
		}
		return this.variableNames;
	}
}
