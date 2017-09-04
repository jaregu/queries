package com.jaregu.database.queries.parsing;

import java.util.List;

import com.jaregu.database.queries.QueryId;

public class ParsedQueryImpl implements ParsedQuery {

	private QueryId id;
	private List<ParsedQueryPart> parts;

	public ParsedQueryImpl(QueryId id, List<ParsedQueryPart> parts) {
		this.id = id;
		this.parts = parts;
	}

	@Override
	public QueryId getQueryId() {
		return id;
	}

	@Override
	public List<ParsedQueryPart> getParts() {
		return parts;
	}

	@Override
	public String toString() {
		return "ParsedQuery" + parts.toString();
	}
}
