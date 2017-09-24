package com.jaregu.database.queries.ext;

public enum SortOrder {

	ASC("ASC"), DESC("DESC");

	private String sql;

	private SortOrder(String sql) {
		this.sql = sql;
	}

	public String toSql() {
		return sql;
	}
}