package com.jaregu.database.queries.compiling.expr;

import java.util.List;

import com.jaregu.database.queries.building.ParamsResolver;

public interface Expression {

	Object eval(ParamsResolver variableResolver);

	List<String> getVariableNames();
}
