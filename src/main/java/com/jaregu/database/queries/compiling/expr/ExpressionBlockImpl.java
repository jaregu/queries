package com.jaregu.database.queries.compiling.expr;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExpressionBlockImpl extends EvaluableOperand implements ExpressionBlock {

	final private Operand firstOperand;
	final private Optional<OperationType> operationType;
	final private Optional<Operand> secondOperand;
	final private List<String> variableNames;

	public ExpressionBlockImpl(Operand operand) {
		this(operand, Optional.empty(), Optional.empty());
	}

	public ExpressionBlockImpl(Operand firstOperand, OperationType operationType, Operand secondOperand) {
		this(firstOperand, Optional.of(operationType), Optional.of(secondOperand));
	}

	private ExpressionBlockImpl(Operand firstOperand, Optional<OperationType> operationType,
			Optional<Operand> secondOperand) {
		this.firstOperand = requireNonNull(firstOperand);
		this.operationType = requireNonNull(operationType);
		this.secondOperand = requireNonNull(secondOperand);

		List<String> firstOpNames = firstOperand.getVariableNames();
		Optional<List<String>> secondOpNames = secondOperand.map(Operand::getVariableNames);

		List<String> variableNames = new ArrayList<>(firstOpNames.size() + secondOpNames.map(List::size).orElse(0));
		variableNames.addAll(firstOpNames);
		variableNames.addAll(secondOpNames.orElse(Collections.emptyList()));
		this.variableNames = Collections.unmodifiableList(variableNames);

	}

	@Override
	public Object getValue() {
		if (operationType.isPresent()) {
			return operationType.get().invoke(firstOperand, secondOperand.get());
		} else {
			return firstOperand.getValue();
		}
	}

	@Override
	public String toString() {
		if (operationType.isPresent()) {
			return "(" + firstOperand.toString() + " " + operationType.get().getSequence() + " "
					+ secondOperand.get().toString() + ")";
		} else {
			return firstOperand.toString();
		}
	}

	@Override
	public List<String> getVariableNames() {
		return variableNames;
	}
}
