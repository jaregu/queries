package com.jaregu.database.queries.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
@Inherited
public @interface QueryParam {

	/**
	 * Map key name of parameter
	 * 
	 * @return
	 */
	String value();
}