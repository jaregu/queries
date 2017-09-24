package com.jaregu.database.queries;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.dialect.Dialect;

public interface QueriesConfig {

	QueriesCache getCache();

	Dialect getDialect();

	ParameterBinder getParameterBinder();

	static QueriesConfig of(Dialect dialect, QueriesCache cache, ParameterBinder parameterBinder) {
		return new QueriesConfigImpl(dialect, cache, parameterBinder);
	}
}
