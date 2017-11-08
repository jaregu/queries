package com.jaregu.database.queries.proxy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jaregu.database.queries.Queries;

/**
 * Query converter annotation marker annotation. Supply
 * {@link QueryConverterFactory} class (must have accessible zero argument
 * constructor) or use
 * {@link Queries.Builder#converter(Class, QueryConverterFactory)} method to
 * register factory instances mapped to used annotation
 * <p>
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface Converter {

	/**
	 * {@link QueryConverterFactory} class. Use this to register
	 * {@link QueryConverter} factory. Factory has to be instantiable class with
	 * accessible zero argument constructor.
	 * 
	 * @return
	 */
	Class<? extends QueryConverterFactory> value() default DEFAULT.class;

	static abstract class DEFAULT implements QueryConverterFactory {
	}
}