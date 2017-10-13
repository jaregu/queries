package com.jaregu.database.queries.ext;

import static com.jaregu.database.queries.ext.SortBy.empty;

import java.util.List;

import com.jaregu.database.queries.dialect.Orderable;

public interface SortableSearch extends Orderable {

	SortBy getSortBy();

	void setSortBy(SortBy properties);

	default boolean hasSortProperties() {
		return getSortBy() != null && !getSortBy().isEmpty();
	}

	@Override
	default List<String> getOrderByItems() {
		return getSortBy().getOrderByItems();
	}

	default void clearSortProperties() {
		setSortBy(empty());
	}

	default void addSortProperty(SortProperty property) {
		setSortBy((getSortBy() != null ? getSortBy() : empty()).add(property));
	}

	default void addSort(String property) {
		setSortBy((getSortBy() != null ? getSortBy() : empty()).add(property));
	}

	default void addSortAsc(String property) {
		setSortBy((getSortBy() != null ? getSortBy() : empty()).addAsc(property));
	}

	default void addSortDesc(String property) {
		setSortBy((getSortBy() != null ? getSortBy() : empty()).addDesc(property));
	}
}
