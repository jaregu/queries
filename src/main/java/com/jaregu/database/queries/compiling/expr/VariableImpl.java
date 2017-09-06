package com.jaregu.database.queries.compiling.expr;

import java.util.Collections;
import java.util.List;

public final class VariableImpl extends EvaluableOperand implements Variable {

	final private String name;
	private List<String> variableNames;

	public VariableImpl(String name) {
		this.name = name;
		this.variableNames = Collections.singletonList(name);
	}

	@Override
	public Object getValue() {
		EvaluationContext context = EvaluationContext.getCurrent();
		return context.getVariableResolver().getNamedResolver().getValue(name);
	}

	@Override
	public String toString() {
		return "${" + name + "}";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getVariableNames() {
		return variableNames;
	}
}
