package com.jaregu.database.queries.ext.dalesbred;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;

import com.jaregu.database.queries.proxy.Mapper;

@Target({ FIELD, METHOD })
@Retention(RUNTIME)
@Mapper
public @interface FindOptional {

	/**
	 * Row class
	 * 
	 * @return
	 */
	Class<?> value();

	/**
	 * By default uses {@link Optional} class. Set to false to get value or null
	 * of value is not found
	 */
	boolean useOptional() default true;
}
