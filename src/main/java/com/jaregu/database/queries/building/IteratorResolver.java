package com.jaregu.database.queries.building;

import java.util.Iterator;

public interface IteratorResolver extends Iterator<Object> {

	static IteratorResolver of(Iterable<?> parameters) {
		return new IteratorResolverImpl(parameters);
	}
}
