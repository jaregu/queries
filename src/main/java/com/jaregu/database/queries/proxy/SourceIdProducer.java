package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import com.jaregu.database.queries.SourceId;

@FunctionalInterface
public interface SourceIdProducer {

	SourceId get(AnnotatedElement element, Annotation annotation);
}
