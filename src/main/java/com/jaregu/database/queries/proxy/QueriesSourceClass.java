package com.jaregu.database.queries.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jaregu.database.queries.SourceId;

/**
 * Identifies SQL queries source, see {@link SourceId#ofClass(Class)}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@RegisteredSourceIdProducer(QueriesSourceClassProducer.class)
public @interface QueriesSourceClass {

	/**
	 * Value for {@link SourceId#ofClass(Class)}. Can be omitted if decorated
	 * class to be used as parameter.
	 * 
	 * @return
	 */
	Class<?> value() default DEFAULT.class;

	static final class DEFAULT {
	}
}