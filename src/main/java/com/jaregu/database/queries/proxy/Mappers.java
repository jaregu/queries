package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;

import com.jaregu.database.queries.Queries;

public interface Mappers<T extends Mappers<?>> {

	/**
	 * Registers {@link QueryMapperFactory} to be used when given annotation is
	 * encountered on proxied {@link Queries} interface. See
	 * {@link Queries#proxy(Class)}.
	 * <p>
	 */
	T factory(Class<? extends Annotation> annotatedWith, QueryMapperFactory factory);
}
