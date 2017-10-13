package com.jaregu.database.queries.proxy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark proxiable interface or its methods to perform
 * Query conversion to some other value. {@link QueryMapper} must be
 * instantiable class with accessible zero argument constructor
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RUNTIME)
@Mapper(ClassQueryMapperFactory.class)
public @interface ClassQueryMapper {

	/**
	 * {@link QueryMapper} must be instantiable class with accessible zero
	 * argument constructor
	 */
	Class<? extends QueryMapper<?>> value();
}