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

public final class SortBy implements Iterable<SortProperty>, Orderable, Serializable {

	private static final long serialVersionUID = 9139131057961457410L;

	private static final SortBy EMPTY = new SortBy(Collections.emptyList());

	private final List<SortProperty> properties;

	SortBy(List<SortProperty> properties) {
		this.properties = properties;
	}

	@Override
	public Iterator<SortProperty> iterator() {
		return properties.iterator();
	}

	@JsonIgnore
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public SortBy add(SortProperty property) {
		return new SortBy(Stream.concat(properties.stream(), Stream.of(property)).collect(Collectors.toList()));
	}

	public SortBy add(String field) {
		return add(SortProperty.of(field));
	}

	public SortBy addAsc(String field) {
		return add(SortProperty.asc(field));
	}

	public SortBy addDesc(String field) {
		return add(SortProperty.desc(field));
	}

	@Override
	public List<String> getOrderByItems() {
		return properties.stream().map(SortProperty::toSql)
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

		if (obj instanceof SortBy) {
			SortBy other = (SortBy) obj;
			return Objects.equals(properties, other.properties);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(properties);
	}

	public static SortBy empty() {
		return EMPTY;
	}

	public static SortBy of(SortProperty property) {
		return EMPTY.add(property);
	}

	public static SortBy asc(String property) {
		return EMPTY.addAsc(property);
	}

	public static SortBy desc(String property) {
		return EMPTY.addDesc(property);
	}

	@JsonCreator
	public static SortBy of(@JsonProperty Collection<SortProperty> properties) {
		return new SortBy(new ArrayList<>(properties));
	}
}
