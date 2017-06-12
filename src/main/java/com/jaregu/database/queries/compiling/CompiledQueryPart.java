package com.jaregu.database.queries.compiling;

import java.util.List;

import com.jaregu.database.queries.building.ParamsResolver;

public interface CompiledQueryPart {

	List<String> getVariableNames();

	void eval(ParamsResolver variableResolver, ResultConsumer resultConsumer);

	public static CompiledQueryPart constant(String sql) {
		return new CompiledQueryConstantPart(sql);
	}

	@FunctionalInterface
	public interface ResultConsumer {

		void consume(String sql, List<Object> parameters);
	}

	/*
	 * interface Result {
	 * 
	 * String getSql();
	 * 
	 * List<Object> getParameters(); }
	 */
}
