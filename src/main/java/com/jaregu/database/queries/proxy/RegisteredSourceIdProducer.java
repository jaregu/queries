package com.jaregu.database.queries.proxy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface RegisteredSourceIdProducer {

	Class<? extends SourceIdProducer> value();
}