package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;

import com.jaregu.database.queries.Queries;

public interface Converters<T extends Converters<?>> {

	/**
	 * Registers {@link QueryConverterFactory} to be used when given annotation
	 * is encountered on proxied {@link Queries} interface. See
	 * {@link Queries#proxy(Class)}.
	 * <p>
	 */
	T converter(Class<? extends Annotation> annotatedWith, QueryConverterFactory factory);
}
