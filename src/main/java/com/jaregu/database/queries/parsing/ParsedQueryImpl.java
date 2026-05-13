package com.jaregu.database.queries.parsing;

import java.util.List;

import com.jaregu.database.queries.QueryId;

public class ParsedQueryImpl implements ParsedQuery {

	private QueryId id;
	private List<ParsedQueryPart> parts;
	private boolean batch;

	public ParsedQueryImpl(QueryId id, List<ParsedQueryPart> parts) {
		this(id, parts, false);
	}

	public ParsedQueryImpl(QueryId id, List<ParsedQueryPart> parts, boolean batch) {
		this.id = id;
		this.parts = parts;
		this.batch = batch;
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
	public boolean isBatch() {
		return batch;
	}

	@Override
	public String toString() {
		return "ParsedQuery" + (batch ? "(batch)" : "") + parts.toString();
	}
}
