package com.jaregu.database.queries.compiling.expr;

import java.util.List;
import java.util.Map;

import com.jaregu.database.queries.building.ParametersResolver;

public interface Expression {

	ExpressionResult eval(ParametersResolver variableResolver);

	List<String> getVariableNames();

	public interface ExpressionResult {

		Object getReturnValue();

		Map<String, Object> getOutputVariables();
	}
}
