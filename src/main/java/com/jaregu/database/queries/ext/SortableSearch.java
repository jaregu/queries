package com.jaregu.database.queries.ext;

import static com.jaregu.database.queries.ext.SortProperties.empty;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SortableSearch {

	SortProperties getSortProperties();

	void setSortProperties(SortProperties properties);

	default boolean hasSortProperties() {
		return getSortProperties() != null && !getSortProperties().isEmpty();
	}

	default String sortPropertiesToSql() {
		return getSortProperties().toSql();
	}

	default void clearSortProperties() {
		setSortProperties(empty());
	}

	default void addSortProperty(SortProperty property) {
		setSortProperties((getSortProperties() != null ? getSortProperties() : empty()).add(property));
	}

	default void addSort(String property) {
		setSortProperties((getSortProperties() != null ? getSortProperties() : empty()).add(property));
	}

	default void addSortAsc(String property) {
		setSortProperties((getSortProperties() != null ? getSortProperties() : empty()).addAsc(property));
	}

	default void addSortDesc(String property) {
		setSortProperties((getSortProperties() != null ? getSortProperties() : empty()).addDesc(property));
	}
}
