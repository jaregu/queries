package com.jaregu.database.queries.compiling.expr;

import java.util.List;

public interface Operand {

	Object getValue();

	List<String> getVariableNames();

	Object multiply(Object object);

	Object divide(Object object);

	Object add(Object object);

	Object subtract(Object object);

	boolean greater(Object object);

	boolean greaterOrEqual(Object object);

	boolean lesser(Object object);

	boolean lesserOrEqual(Object object);

	boolean equal(Object object);

	boolean notEqual(Object object);

	boolean and(Object object);

	boolean or(Object object);
}
