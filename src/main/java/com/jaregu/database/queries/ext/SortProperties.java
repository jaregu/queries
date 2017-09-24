package com.jaregu.database.queries.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class SortProperties implements Iterable<SortProperty> {

	private static final SortProperties EMPTY = new SortProperties(Collections.emptyList());

	private final List<SortProperty> properties;

	SortProperties(List<SortProperty> properties) {
		this.properties = properties;
	}

	/*
	 * public List<SortProperty> getProperties() { return
	 * Collections.unmodifiableList(properties); }
	 */
	@Override
	public Iterator<SortProperty> iterator() {
		return properties.iterator();
	}

	@JsonIgnore
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public String toSql() {
		return toSql(properties.stream().map(SortProperty::toSql).iterator());
	}

	public SortProperties add(SortProperty property) {
		return new SortProperties(Stream.concat(properties.stream(), Stream.of(property)).collect(Collectors.toList()));
	}

	public SortProperties add(String field) {
		return add(SortProperty.of(field));
	}

	public SortProperties addAsc(String field) {
		return add(SortProperty.asc(field));
	}

	public SortProperties addDesc(String field) {
		return add(SortProperty.desc(field));
	}

	@Override
	public String toString() {
		return "OrderBy" + properties;
	}

	public static String toSql(Iterator<String> properties) {
		Iterator<String> iterator = properties;
		if (iterator.hasNext()) {
			StringBuilder sql = new StringBuilder("ORDER BY ");
			sql.append(iterator.next());
			while (iterator.hasNext()) {
				sql.append(", ").append(iterator.next());
			}
			return sql.toString();
		} else {
			return "";
		}
	}

	public static SortProperties empty() {
		return EMPTY;
	}

	public static SortProperties of(SortProperty property) {
		return EMPTY.add(property);
	}

	public static SortProperties asc(String property) {
		return EMPTY.addAsc(property);
	}

	public static SortProperties desc(String property) {
		return EMPTY.addDesc(property);
	}

	@JsonCreator
	public static SortProperties of(@JsonProperty Collection<SortProperty> properties) {
		return new SortProperties(new ArrayList<>(properties));
	}
}
