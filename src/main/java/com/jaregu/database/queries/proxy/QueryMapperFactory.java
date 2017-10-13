package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;

import com.jaregu.database.queries.Queries;

/**
 * {@link QueryMapper} factory class. Used once per each annotated interface
 * method proxied by {@link Queries#proxy(Class)}
 *
 */
@FunctionalInterface
public interface QueryMapperFactory {

	QueryMapper<?> get(Annotation annotation);
}
