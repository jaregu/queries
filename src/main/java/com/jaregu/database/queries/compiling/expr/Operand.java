package com.jaregu.database.queries.compiling.expr;

import java.util.List;

public interface Operand {

	Object getValue();

	List<String> getVariableNames();

	Object multiply(Operand object);

	Object divide(Operand object);

	Object add(Operand object);

	Object subtract(Operand object);

	boolean greater(Operand object);

	boolean greaterOrEqual(Operand object);

	boolean lesser(Operand object);

	boolean lesserOrEqual(Operand object);

	boolean equal(Operand object);

	boolean notEqual(Operand object);

	boolean and(Operand object);

	boolean or(Operand object);
}
