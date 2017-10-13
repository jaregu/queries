package com.jaregu.database.queries.ext;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaregu.database.queries.dialect.Pageable;

final public class OffsetLimit implements Pageable, Serializable {

	private static final long serialVersionUID = 7553329792612426998L;

	private static final OffsetLimit EMPTY = new OffsetLimit(null, null);

	final private Integer offset;
	final private Integer limit;

	OffsetLimit(Integer offset, Integer limit) {
		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public Integer getOffset() {
		return offset;
	}

	@Override
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

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof OffsetLimit) {
			OffsetLimit other = (OffsetLimit) obj;
			return Objects.equals(limit, other.limit) && Objects.equals(offset, other.offset);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(limit, offset);
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
