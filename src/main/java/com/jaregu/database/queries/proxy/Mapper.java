package com.jaregu.database.queries.proxy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jaregu.database.queries.Queries;

/**
 * Query mapping annotation marker annotation. Supply {@link QueryMapperFactory}
 * class (must have accessible zero argument constructor) or use
 * {@link Queries.Builder#factory(Class, QueryMapperFactory)} method to register
 * factory instances mapped to used annotation
 * <p>
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface Mapper {

	/**
	 * {@link QueryMapperFactory} class. Use this to register
	 * {@link QueryMapper} factory. Factory has to be instantiable class with
	 * accessible zero argument constructor.
	 * 
	 * @return
	 */
	Class<? extends QueryMapperFactory> value() default DEFAULT.class;

	static abstract class DEFAULT implements QueryMapperFactory {
	}
}