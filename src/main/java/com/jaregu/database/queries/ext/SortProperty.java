package com.jaregu.database.queries.ext;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class SortProperty {

	final private String name;
	final private SortOrder sortOrder;
	final private NullsOrder nullsOrder;

	SortProperty(String name, SortOrder sortOrder, NullsOrder nullsOrder) {
		if (name == null) {
			throw new NullPointerException("Property name is null!");
		}
		this.name = name;
		this.sortOrder = sortOrder;
		this.nullsOrder = nullsOrder;
	}

	public String getName() {
		return name;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public NullsOrder getNullsOrder() {
		return nullsOrder;
	}

	public String toSql() {
		StringBuilder orderBy = new StringBuilder(name);
		if (sortOrder != null)
			orderBy.append(" ").append(sortOrder.toSql());
		if (nullsOrder != null)
			orderBy.append(" ").append(nullsOrder.toSql());
		return orderBy.toString();
	}

	@Override
	public String toString() {
		return "SortProperty{" + toSql() + "}";
	}

	public SortProperty asc() {
		return new SortProperty(name, SortOrder.ASC, nullsOrder);
	}

	public SortProperty desc() {
		return new SortProperty(name, SortOrder.DESC, nullsOrder);
	}

	public SortProperty toggle() {
		return new SortProperty(name, sortOrder == SortOrder.DESC ? SortOrder.ASC : SortOrder.DESC, nullsOrder);
	}

	public SortProperty nullsFirst() {
		return new SortProperty(name, sortOrder, NullsOrder.FIRST);
	}

	public SortProperty nullsLast() {
		return new SortProperty(name, sortOrder, NullsOrder.LAST);
	}

	public static SortProperty of(String name) {
		return new SortProperty(name, null, null);
	}

	public static SortProperty of(String name, SortOrder sortOrder) {
		return new SortProperty(name, sortOrder, null);
	}

	@JsonCreator
	public static SortProperty of(@JsonProperty("name") String name, @JsonProperty("sortOrder") SortOrder sortOrder,
			@JsonProperty("nullsOrder") NullsOrder nullsOrder) {
		return new SortProperty(name, sortOrder, nullsOrder);
	}

	public static SortProperty asc(String name) {
		return new SortProperty(name, SortOrder.ASC, null);
	}

	public static SortProperty desc(String name) {
		return new SortProperty(name, SortOrder.DESC, null);
	}
}
