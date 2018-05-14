package com.jaregu.database.queries.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Column {

	/**
	 * The name of the table field. Defaults to the property name.
	 */
	String name() default "";
}