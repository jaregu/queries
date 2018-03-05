package com.jaregu.database.queries.ext;

import java.util.List;

import com.jaregu.database.queries.dialect.Orderable;

public interface OrderableSearch<T extends OrderableSearch<T>> extends Orderable {

	T withOrderBy(List<String> orderBy);

	default boolean hasSortProperties() {
		return getOrderBy() != null && !getOrderBy().isEmpty();
	}

	default T withAsc(String property) {
		return withOrderBy(OrderBy.of(getOrderBy()).asc(property));
	}

	default T withDesc(String property) {
		return withOrderBy(OrderBy.of(getOrderBy()).desc(property));
	}

	default T asc(String property) {
		return withOrderBy(OrderBy.asc(property));
	}

	default T desc(String property) {
		return withOrderBy(OrderBy.desc(property));
	}
}
