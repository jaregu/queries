package com.jaregu.database.queries;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

public class QueriesConfigImpl implements QueriesConfig {

	private final Dialect dialect;
	private final ParameterBinder parameterBinder;
	private final Map<Class<? extends Annotation>, QueryMapperFactory> factories;

	QueriesConfigImpl(Dialect dialect, ParameterBinder parameterBinder,
			Map<Class<? extends Annotation>, QueryMapperFactory> factories) {
		this.dialect = dialect;
		this.parameterBinder = parameterBinder;
		this.factories = Collections.unmodifiableMap(factories);
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public ParameterBinder getParameterBinder() {
		return parameterBinder;
	}

	@Override
	public Map<Class<? extends Annotation>, QueryMapperFactory> getQueryMapperFactories() {
		return factories;
	}
}
