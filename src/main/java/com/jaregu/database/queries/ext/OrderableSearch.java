package com.jaregu.database.queries.ext;

import static com.jaregu.database.queries.ext.OrderBy.empty;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.dialect.Orderable;

public interface OrderableSearch extends Orderable {

	OrderBy getOrderBy();

	void setOrderBy(OrderBy properties);

	default boolean hasSortProperties() {
		return getOrderBy() != null && !getOrderBy().isEmpty();
	}

	@Override
	default List<String> getOrderByItems() {
		return getOrderBy() != null ? getOrderBy().getOrderByItems() : Collections.emptyList();
	}

	default void clearSortProperties() {
		setOrderBy(empty());
	}

	default void addSortProperty(OrderByProperty property) {
		setOrderBy((getOrderBy() != null ? getOrderBy() : empty()).add(property));
	}

	default void addSort(String property) {
		setOrderBy((getOrderBy() != null ? getOrderBy() : empty()).add(property));
	}

	default void addSortAsc(String property) {
		setOrderBy((getOrderBy() != null ? getOrderBy() : empty()).addAsc(property));
	}

	default void addSortDesc(String property) {
		setOrderBy((getOrderBy() != null ? getOrderBy() : empty()).addDesc(property));
	}
}
