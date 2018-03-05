package com.jaregu.database.queries.ext;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OrderBy {

	public static OrderByList empty() {
		return new OrderByList(Collections.emptyList());
	}

	public static OrderByList of(List<String> orderByItems) {
		return new OrderByList(orderByItems);
	}

	public static OrderByList asc(String property) {
		return new OrderByList(Collections.singletonList(property));
	}

	public static OrderByList desc(String property) {
		return new OrderByList(Collections.singletonList(property));
	}

	public static class OrderByList extends AbstractList<String> implements List<String> {

		private List<String> orderByItems;

		public OrderByList(List<String> orderByItems) {
			this.orderByItems = Collections.unmodifiableList(orderByItems);
		}

		public OrderByList asc(String property) {
			return with(property);
		}

		public OrderByList desc(String property) {
			return with(property + " " + SortOrder.DESC);
		}

		private OrderByList with(String property) {
			return new OrderByList(
					Stream.concat(orderByItems.stream(), Stream.of(property)).collect(Collectors.toList()));
		}

		@Override
		public String get(int index) {
			return orderByItems.get(index);
		}

		@Override
		public int size() {
			return orderByItems.size();
		}
	}
}
