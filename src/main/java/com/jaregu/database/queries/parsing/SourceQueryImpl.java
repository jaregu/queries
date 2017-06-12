package com.jaregu.database.queries.parsing;

import java.util.List;

import com.jaregu.database.queries.QueryId;

public class SourceQueryImpl implements SourceQuery {

	private QueryId id;
	private List<SourceQueryPart> parts;

	public SourceQueryImpl(QueryId id, List<SourceQueryPart> parts) {
		this.id = id;
		this.parts = parts;
	}

	@Override
	public QueryId getQueryId() {
		return id;
	}

	@Override
	public List<SourceQueryPart> getParts() {
		return parts;
	}

	@Override
	public String toString() {
		return "SourceQuery" + parts.toString();
	}
}
