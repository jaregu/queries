package com.jaregu.database.queries.ext.dalesbred;

import static java.util.Collections.unmodifiableList;

import java.util.Iterator;
import java.util.List;

public class DataTable implements Iterable<List<Object>> {

	private List<String> columns;
	private List<List<Object>> rows;

	public DataTable(List<String> columns, List<List<Object>> rows) {
		this.columns = unmodifiableList(columns);
		this.rows = unmodifiableList(rows);
	}

	public static DataTable create(List<String> columns, List<List<Object>> rows) {
		return new DataTable(columns, rows);
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return rows.iterator();
	}

	public List<String> getColumns() {
		return columns;
	}

	public List<List<Object>> getRows() {
		return rows;
	}
}
