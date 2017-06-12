package com.jaregu.database.queries.building;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.QueryId;

public class QueryImpl implements Query {

	private QueryId queryId;
	private String sql;
	private List<Object> parameters;

	public QueryImpl(QueryId queryId, String sql, List<Object> parameters) {
		this.queryId = queryId;
		this.sql = sql;
		this.parameters = Collections.unmodifiableList(parameters);
	}

	@Override
	public QueryId getQueryId() {
		return queryId;
	}

	@Override
	public String getSql() {
		return sql;
	}

	@Override
	public List<Object> getParameters() {
		return parameters;
	}
}
