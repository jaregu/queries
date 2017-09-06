package com.jaregu.database.queries.compiling.expr;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Operator {

	int getPrecedence();

	AssociativityType getAssociativity();

	List<String> getSequences();

	boolean isNullary();

	boolean isUnary();

	boolean isBinary();

	boolean isTernary();

	Function<Operand, Object> getUnary();

	BiFunction<Operand, Operand, Object> getBinary();

	TernaryFunction<Operand, Operand, Operand, Object> getTernary();

	@FunctionalInterface
	public interface TernaryFunction<TYPE, FIRST, SECOND, RESULT> {
		RESULT apply(TYPE t, FIRST first, SECOND second);
	}
}
