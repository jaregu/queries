package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import com.jaregu.database.queries.SourceId;

public class QueriesSourceResourceProducer implements SourceIdProducer {

	QueriesSourceResourceProducer() {
	}

	@Override
	public SourceId get(AnnotatedElement element, Annotation annotation) {
		QueriesSourceResource sourceResource = (QueriesSourceResource) annotation;
		return SourceId.ofResource(sourceResource.value());
	}
}