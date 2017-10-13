package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import com.jaregu.database.queries.SourceId;

public class QueriesSourceClassProducer implements SourceIdProducer {

	QueriesSourceClassProducer() {
	}

	@Override
	public SourceId get(AnnotatedElement element, Annotation annotation) {
		QueriesSourceClass sourceClass = (QueriesSourceClass) annotation;
		SourceId sourceId;
		if (QueriesSourceClass.DEFAULT.class.isAssignableFrom(sourceClass.value())) {
			if (element instanceof Class<?>) {
				sourceId = SourceId.ofClass((Class<?>) element);
			} else {
				throw new QueryProxyException(
						"Annotation QueriesSourceClass value can be ommited if used on source class itself!");
			}
		} else {
			sourceId = SourceId.ofClass(sourceClass.value());
		}
		return sourceId;
	}
}