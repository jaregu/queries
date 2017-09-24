package com.jaregu.database.queries.compiling;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class PreparedQueryPartResultImpl implements PreparedQueryPart.Result {

	private final Optional<String> sql;
	private final List<Object> parameters;
	private final Map<String, Object> attributes;

	PreparedQueryPartResultImpl(Optional<String> sql, List<Object> parameters, Map<String, Object> attributes) {
		this.sql = sql;
		this.parameters = parameters;
		this.attributes = attributes;
	}

	@Override
	public Optional<String> getSql() {
		return sql;
	}

	@Override
	public List<Object> getParameters() {
		return parameters;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
