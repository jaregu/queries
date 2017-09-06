package com.jaregu.database.queries.compiling.expr;

public final class OutputVariableImpl extends OperandBaseImpl implements OutputVariable {

	final private String name;

	public OutputVariableImpl(String name) {
		this.name = name;
	}

	@Override
	public Object assign(Operand object) {
		Object value = object.getValue();
		EvaluationContext.getCurrent().setOutputVariable(name, value);
		return value;
	}

	@Override
	public String toString() {
		return "Output{" + name + "}";
	}

	@Override
	public String getName() {
		return name;
	}
}
