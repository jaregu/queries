package com.jaregu.database.queries.compiling;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.parsing.SourceQuery;

@FunctionalInterface
public interface QueryCompiler {

	CompiledQuery compile(SourceQuery sourceQuery);

	static QueryCompiler createDefault(QueriesConfig config) {
		return QueryCompilerImpl.createDefault(config);
	}
}
