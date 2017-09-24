package com.jaregu.database.queries.ext;

public enum NullsOrder {

	FIRST("NULLS FIRST"), LAST("NULLS LAST");

	private String sql;

	private NullsOrder(String sql) {
		this.sql = sql;
	}

	public String toSql() {
		return sql;
	}
}
