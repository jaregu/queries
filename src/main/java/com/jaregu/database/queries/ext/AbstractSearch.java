package com.jaregu.database.queries.ext;

import java.util.List;

public class AbstractSearch<T extends AbstractSearch<T>> implements OrderableSearch<T>, PageableSearch<T> {

	protected final Integer limit;
	protected final Integer offset;
	protected final List<String> orderByItems;

	public AbstractSearch(Integer offset, Integer limit, List<String> orderByItems) {
		this.offset = offset;
		this.limit = limit;
		this.orderByItems = orderByItems;
	}

	@Override
	public Integer getOffset() {
		return offset;
	}

	@Override
	public Integer getLimit() {
		return limit;
	}

	@Override
	public List<String> getOrderBy() {
		return orderByItems;
	}

	@Override
	public T withOffset(Integer offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T withLimit(Integer limit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T withOrderBy(List<String> orderBy) {
		throw new UnsupportedOperationException();
	}
}