package com.jaregu.database.queries.ext;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

final public class OffsetLimit {

	private static final OffsetLimit EMPTY = new OffsetLimit(null, null);

	final private Integer offset;
	final private Integer limit;

	OffsetLimit(Integer offset, Integer limit) {
		this.offset = offset;
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public Integer getLimit() {
		return limit;
	}

	private boolean isFull() {
		return limit != null && offset != null;
	}

	public OffsetLimit nextPage() {
		if (isFull()) {
			return new OffsetLimit(offset + limit, limit);
		} else {
			return this;
		}
	}

	public OffsetLimit nextPageFull(int rowCount) {
		if (isFull()) {
			OffsetLimit nextPage = nextPage();
			if (nextPage.getOffset() + nextPage.getLimit() > rowCount) {
				return new OffsetLimit(rowCount > nextPage.getLimit() ? rowCount - nextPage.getLimit() : 0, limit);
			} else {
				return nextPage;
			}
		}
		return this;
	}

	public OffsetLimit offset(Integer offset) {
		return new OffsetLimit(offset, limit);
	}

	public OffsetLimit limit(Integer limit) {
		return new OffsetLimit(offset, limit);
	}

	public String toString() {
		return "Paging{offset=" + offset + ", limit=" + limit + "}";
	}

	public static OffsetLimit empty() {
		return EMPTY;
	}

	public static OffsetLimit of(Integer limit) {
		return new OffsetLimit(0, limit);
	}

	@JsonCreator
	public static OffsetLimit of(@JsonProperty("offset") Integer offset, @JsonProperty("limit") Integer limit) {
		return new OffsetLimit(offset, limit);
	}
}
