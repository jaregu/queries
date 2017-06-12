package com.jaregu.database.queries.building;

import com.jaregu.database.queries.compiling.CompiledQuery;

@FunctionalInterface
public interface QueryBuilder {

	Query build(CompiledQuery compiledQuery, ParamsResolver resolver);

	public static QueryBuilder createDefault() {
		return new QueryBuilderImpl();
	}
}
