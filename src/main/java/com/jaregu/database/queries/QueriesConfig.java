package com.jaregu.database.queries;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.proxy.QueryConverterFactory;
import com.jaregu.database.queries.proxy.QueryMapperFactory;

public interface QueriesConfig {

	Dialect getDialect();

	ParameterBinder getParameterBinder();

	Map<Class<? extends Annotation>, QueryMapperFactory> getQueryMapperFactories();

	Map<Class<? extends Annotation>, QueryConverterFactory> getQueryConverterFactories();

	Map<String, Class<?>> getEntities();
}
