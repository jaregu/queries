package com.jaregu.database.queries.ext.dalesbred;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.dalesbred.transaction.Isolation;
import org.dalesbred.transaction.Propagation;

/**
 * Marks method which is executed in transaction. By default it has
 * {@link Propagation#REQUIRED}, which means - join existing transaction if
 * there is one, otherwise create a new one.
 * <p>
 * Newly created connection after method execution is committed, joined
 * connection is expected to be committed elsewhere.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
public @interface Transactional {

	Propagation propagation() default Propagation.REQUIRED;

	Isolation isolation() default Isolation.DEFAULT;
}