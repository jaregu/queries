package com.jaregu.database.queries;

import com.jaregu.database.queries.building.ParameterBindingBuilder;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.compiling.QueryCompiler;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.parsing.QueriesParser;

public interface QueriesConfig {

	QueriesCache getCache();

	QueriesParser getParser();

	QueryCompiler getCompiler();

	ExpressionParser getExpressionParser();

	ParameterBindingBuilder getParameterBindingBuilder();

	static QueriesConfigImpl.Builder builder() {
		return QueriesConfigImpl.builder();
	}

	static QueriesConfig createDefault() {
		return QueriesConfigImpl.builder().build();
	}
}
