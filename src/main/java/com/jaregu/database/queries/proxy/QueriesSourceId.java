package com.jaregu.database.queries.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jaregu.database.queries.SourceId;

/**
 * Identifies SQL source, see {@link SourceId#ofId(String)}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@RegisteredSourceIdProducer(QueriesSourceIdProducer.class)
public @interface QueriesSourceId {

	/**
	 * Value for {@link SourceId#ofId(String)}
	 * 
	 * @return
	 */
	String value();
}