package com.jaregu.database.queries.building;

import java.util.Iterator;

final class IteratorResolverImpl implements IteratorResolver {

	private Iterator<?> iterator;

	IteratorResolverImpl(Iterable<?> parameters) {
		iterator = parameters.iterator();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Object next() {
		return iterator.next();
	}
}
