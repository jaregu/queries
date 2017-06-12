package com.jaregu.database.queries.compiling.expr;

import java.util.function.BiFunction;

public enum OperationType implements Separator {

	//Precedence http://introcs.cs.princeton.edu/java/11precedence/

	MULTIPLY("*", Operand::multiply, 4),

	DIVIDE("/", Operand::divide, 4),

	PLUS("+", Operand::add, 5),

	MINUS("-", Operand::subtract, 5),

	GREATER(">", Operand::greater, 7),

	GREATER_OR_EQUAL(">=", Operand::greaterOrEqual, 7),

	LESSER("<", Operand::lesser, 7),

	LESSER_OR_EQUAL("<=", Operand::lesserOrEqual, 7),

	EQUAL("==", Operand::equal, 8),

	NOT_EQUAL("!=", Operand::notEqual, 8),

	AND("&&", Operand::and, 12),

	OR("||", Operand::or, 13),

	;

	final private String sequence;
	final private BiFunction<Operand, Object, Object> function;
	final private int precedence;

	private OperationType(String sequence, BiFunction<Operand, Object, Object> function, int precedence) {
		this.sequence = sequence;
		this.function = function;
		this.precedence = precedence;
	}

	public Object invoke(Operand operand, Object value) {
		return function.apply(operand, value);
	}

	public int getPrecedence() {
		return precedence;
	}

	@Override
	public String getSequence() {
		return sequence;
	}

	@Override
	public String toString() {
		return sequence;
	}
}