package com.jaregu.database.queries.ext;

import java.util.List;

public interface SortableSearch {

	List<SortProperty> getSortProperties();

	interface SortProperty {

		String getProperty();

		SortType getType();
	}

	enum SortType {

		ASC(""), DESC(" DESC");

		private String sql;

		private SortType(String sql) {
			this.sql = sql;
		}

		public String getSql() {
			return sql;
		}
	}
}
