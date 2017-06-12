package com.jaregu.database.queries.compiling;

import java.util.List;

import com.jaregu.database.queries.QueryId;

class CompiledQueryImpl implements CompiledQuery {

	private QueryId id;
	private List<CompiledQueryPart> parts;

	public CompiledQueryImpl(QueryId id, List<CompiledQueryPart> parts) {
		this.id = id;
		this.parts = parts;
	}

	@Override
	public QueryId getQueryId() {
		return id;
	}

	@Override
	public List<CompiledQueryPart> getParts() {
		return parts;
	}
}
