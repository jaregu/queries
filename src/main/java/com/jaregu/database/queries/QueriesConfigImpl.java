package com.jaregu.database.queries;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.cache.QueriesCache;
import com.jaregu.database.queries.dialect.Dialect;

public class QueriesConfigImpl implements QueriesConfig {

	private final Dialect dialect;

	private final QueriesCache cache;

	private final ParameterBinder parameterBinder;

	QueriesConfigImpl(Dialect dialect, QueriesCache cache, ParameterBinder parameterBinder) {
		this.dialect = dialect;
		this.cache = cache;
		this.parameterBinder = parameterBinder;
	}
	
	@Override
	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public QueriesCache getCache() {
		return cache;
	}

	@Override
	public ParameterBinder getParameterBinder() {
		return parameterBinder;
	}
}
