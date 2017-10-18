package com.jaregu.database.queries.ext;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class OrderByProperty implements Serializable {

	private static final long serialVersionUID = -109082551693301890L;

	final private String name;
	final private SortOrder sortOrder;
	final private NullsOrder nullsOrder;

	OrderByProperty(String name, SortOrder sortOrder, NullsOrder nullsOrder) {
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

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof OrderByProperty) {
			OrderByProperty other = (OrderByProperty) obj;
			return Objects.equals(name, other.name) && Objects.equals(sortOrder, other.sortOrder)
					&& Objects.equals(nullsOrder, other.nullsOrder);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, sortOrder, nullsOrder);
	}

	public OrderByProperty asc() {
		return new OrderByProperty(name, SortOrder.ASC, nullsOrder);
	}

	public OrderByProperty desc() {
		return new OrderByProperty(name, SortOrder.DESC, nullsOrder);
	}

	public OrderByProperty toggle() {
		return new OrderByProperty(name, sortOrder == SortOrder.DESC ? SortOrder.ASC : SortOrder.DESC, nullsOrder);
	}

	public OrderByProperty nullsFirst() {
		return new OrderByProperty(name, sortOrder, NullsOrder.FIRST);
	}

	public OrderByProperty nullsLast() {
		return new OrderByProperty(name, sortOrder, NullsOrder.LAST);
	}

	public static OrderByProperty of(String name) {
		return new OrderByProperty(name, null, null);
	}

	public static OrderByProperty of(String name, SortOrder sortOrder) {
		return new OrderByProperty(name, sortOrder, null);
	}

	@JsonCreator
	public static OrderByProperty of(@JsonProperty("name") String name, @JsonProperty("sortOrder") SortOrder sortOrder,
			@JsonProperty("nullsOrder") NullsOrder nullsOrder) {
		return new OrderByProperty(name, sortOrder, nullsOrder);
	}

	public static OrderByProperty asc(String name) {
		return new OrderByProperty(name, SortOrder.ASC, null);
	}

	public static OrderByProperty desc(String name) {
		return new OrderByProperty(name, SortOrder.DESC, null);
	}
}
