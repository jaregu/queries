package com.jaregu.database.queries.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jaregu.database.queries.SourceId;

/**
 * Identifies SQL source, see {@link SourceId#ofResource(String)}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@RegisteredSourceIdProducer(QueriesSourceResourceProducer.class)
public @interface QueriesSourceResource {

	/**
	 * Value for {@link SourceId#ofResource(String)}
	 * 
	 * @return
	 */
	String value();

}