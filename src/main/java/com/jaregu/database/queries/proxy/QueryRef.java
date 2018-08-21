package com.jaregu.database.queries.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.building.Query;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Inherited
public @interface QueryRef {

	/**
	 * See {@link SourceId#getQueryId(String)}
	 * 
	 * @return
	 */
	String value();

	/**
	 * Perform {@link Query#toPagedQuery(com.jaregu.database.queries.dialect.Pageable)}
	 * @return
	 */
	boolean toPaged() default false;

	boolean toSorted() default false;

	boolean toCount() default false;
}