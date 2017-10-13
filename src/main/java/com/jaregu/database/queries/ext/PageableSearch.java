package com.jaregu.database.queries.ext;

import static com.jaregu.database.queries.ext.OffsetLimit.empty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaregu.database.queries.dialect.Pageable;

public interface PageableSearch extends Pageable {

	@JsonIgnore
	OffsetLimit getOffsetLimit();

	void setOffsetLimit(OffsetLimit offsetLimit);

	@Override
	default Integer getOffset() {
		OffsetLimit offsetLimit = getOffsetLimit();
		return offsetLimit != null ? offsetLimit.getOffset() : null;
	}

	default void setOffset(Integer offset) {
		setOffsetLimit((getOffsetLimit() != null ? getOffsetLimit() : empty()).offset(offset));
	}

	@Override
	default Integer getLimit() {
		OffsetLimit offsetLimit = getOffsetLimit();
		return offsetLimit != null ? offsetLimit.getLimit() : null;
	}

	default void setLimit(Integer limit) {
		setOffsetLimit((getOffsetLimit() != null ? getOffsetLimit() : empty()).limit(limit));
	}

	default void nextPage() {
		OffsetLimit offsetLimit = getOffsetLimit();
		if (offsetLimit != null)
			setOffsetLimit(offsetLimit.nextPage());
	}

	default void nextPageFull(int rowCount) {
		OffsetLimit offsetLimit = getOffsetLimit();
		if (offsetLimit != null)
			setOffsetLimit(offsetLimit.nextPageFull(rowCount));
	}
}
