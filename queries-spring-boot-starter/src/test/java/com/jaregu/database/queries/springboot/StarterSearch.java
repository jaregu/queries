package com.jaregu.database.queries.springboot;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.ext.OrderableSearch;
import com.jaregu.database.queries.ext.PageableSearch;

/**
 * Hand-rolled immutable search object used by {@link QueriesAutoConfigurationTest}
 * to exercise {@code @QueryRef(toSorted = true, toPaged = true)} end-to-end.
 * Mirrors the shape the README's Lombok-built {@code JobsSearch} produces:
 * implements both {@link OrderableSearch} and {@link PageableSearch}, returns
 * itself from the various {@code with*} mutators.
 */
public final class StarterSearch implements OrderableSearch<StarterSearch>, PageableSearch<StarterSearch> {

	private final Integer offset;
	private final Integer limit;
	private final List<String> orderBy;

	private StarterSearch(Integer offset, Integer limit, List<String> orderBy) {
		this.offset = offset;
		this.limit = limit;
		this.orderBy = orderBy == null ? Collections.emptyList() : List.copyOf(orderBy);
	}

	public static StarterSearch unconstrained() {
		return new StarterSearch(null, null, Collections.emptyList());
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
		return orderBy;
	}

	@Override
	public StarterSearch withOffset(Integer offset) {
		return new StarterSearch(offset, this.limit, this.orderBy);
	}

	@Override
	public StarterSearch withLimit(Integer limit) {
		return new StarterSearch(this.offset, limit, this.orderBy);
	}

	@Override
	public StarterSearch withOrderBy(List<String> orderBy) {
		return new StarterSearch(this.offset, this.limit, orderBy);
	}
}
