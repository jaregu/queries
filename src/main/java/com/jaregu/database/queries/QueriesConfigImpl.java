package com.jaregu.database.queries;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.proxy.QueryConverterFactory;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

public class QueriesConfigImpl implements QueriesConfig {

	private final Dialect dialect;
	private final ParameterBinder parameterBinder;
	private final Map<Class<? extends Annotation>, QueryMapperFactory> mappers;
	private final Map<Class<? extends Annotation>, QueryConverterFactory> converters;

	QueriesConfigImpl(Dialect dialect, ParameterBinder parameterBinder,
			Map<Class<? extends Annotation>, QueryMapperFactory> mappers,
			Map<Class<? extends Annotation>, QueryConverterFactory> converters) {
		this.dialect = dialect;
		this.parameterBinder = parameterBinder;
		this.mappers = Collections.unmodifiableMap(mappers);
		this.converters = Collections.unmodifiableMap(converters);
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
		return mappers;
	}

	@Override
	public Map<Class<? extends Annotation>, QueryConverterFactory> getQueryConverterFactories() {
		return converters;
	}
}
