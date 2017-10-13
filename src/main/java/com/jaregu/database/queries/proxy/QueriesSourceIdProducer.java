package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import com.jaregu.database.queries.SourceId;

public class QueriesSourceIdProducer implements SourceIdProducer {

	QueriesSourceIdProducer() {
	}

	@Override
	public SourceId get(AnnotatedElement element, Annotation annotation) {
		QueriesSourceId queriesSourceId = (QueriesSourceId) annotation;
		return SourceId.ofId(queriesSourceId.value());
	}
}