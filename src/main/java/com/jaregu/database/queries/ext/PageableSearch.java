package com.jaregu.database.queries.ext;

import com.jaregu.database.queries.dialect.Pageable;

public interface PageableSearch<T extends PageableSearch<T>> extends Pageable {

	T withOffset(Integer offset);

	T withLimit(Integer limit);

	default T nextPage() {
		if (getLimit() != null && getOffset() != null) {
			return withOffset(getOffset() + getLimit());
		}
		throw new IllegalStateException("Limit or offset is null, can't go to next page!");
	}

	default T nextPageFull(int rowCount) {
		if (getLimit() != null && getOffset() != null) {
			if (getOffset() + getLimit() + getLimit() > rowCount) {
				return withOffset(rowCount > getLimit() ? rowCount - getLimit() : 0);
			} else {
				return nextPage();
			}
		}
		throw new IllegalStateException("Limit or offset is null, can't go to next page!");
	}
}
