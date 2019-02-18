package com.jaregu.database.queries.ext.dalesbred;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jaregu.database.queries.proxy.Mapper;

@Target({ FIELD, METHOD })
@Retention(RUNTIME)
@Mapper
public @interface FindAll {

	/**
	 * Row class
	 * 
	 * @return
	 */
	Class<?> value();
}
