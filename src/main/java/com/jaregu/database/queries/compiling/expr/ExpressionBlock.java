package com.jaregu.database.queries.compiling.expr;

import java.util.List;

interface ExpressionBlock extends Operand {

	Object getValue();

	List<String> getVariableNames();
}
