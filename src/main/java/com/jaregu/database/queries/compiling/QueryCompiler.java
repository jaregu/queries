package com.jaregu.database.queries.compiling;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.parsing.ParsedQuery;

@FunctionalInterface
public interface QueryCompiler {

	PreparedQuery compile(ParsedQuery sourceQuery);

	static QueryCompiler createDefault() {
		return QueryCompilerImpl.createDefault();
	}
}
