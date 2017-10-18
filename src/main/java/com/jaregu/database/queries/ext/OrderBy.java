package com.jaregu.database.queries.ext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaregu.database.queries.dialect.Orderable;

public final class OrderBy implements Iterable<OrderByProperty>, Orderable, Serializable {

	private static final long serialVersionUID = 9139131057961457410L;

	private static final OrderBy EMPTY = new OrderBy(Collections.emptyList());

	private final List<OrderByProperty> properties;

	OrderBy(List<OrderByProperty> properties) {
		this.properties = properties;
	}

	@Override
	public Iterator<OrderByProperty> iterator() {
		return properties.iterator();
	}

	@JsonIgnore
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public OrderBy add(OrderByProperty property) {
		return new OrderBy(Stream.concat(properties.stream(), Stream.of(property)).collect(Collectors.toList()));
	}

	public OrderBy add(String field) {
		return add(OrderByProperty.of(field));
	}

	public OrderBy addAsc(String field) {
		return add(OrderByProperty.asc(field));
	}

	public OrderBy addDesc(String field) {
		return add(OrderByProperty.desc(field));
	}

	@Override
	public List<String> getOrderByItems() {
		return properties.stream().map(OrderByProperty::toSql)
				.collect(Collectors.toCollection(() -> new ArrayList<String>(properties.size())));
	}

	@Override
	public String toString() {
		return "SortBy" + properties;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof OrderBy) {
			OrderBy other = (OrderBy) obj;
			return Objects.equals(properties, other.properties);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(properties);
	}

	public static OrderBy empty() {
		return EMPTY;
	}

	public static OrderBy of(OrderByProperty property) {
		return EMPTY.add(property);
	}

	public static OrderBy asc(String property) {
		return EMPTY.addAsc(property);
	}

	public static OrderBy desc(String property) {
		return EMPTY.addDesc(property);
	}

	@JsonCreator
	public static OrderBy of(@JsonProperty Collection<OrderByProperty> properties) {
		return new OrderBy(new ArrayList<>(properties));
	}
}
