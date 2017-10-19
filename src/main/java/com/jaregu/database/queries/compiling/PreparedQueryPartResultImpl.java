package com.jaregu.database.queries.compiling;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class PreparedQueryPartResultImpl implements PreparedQueryPart.Result {

	private final Optional<String> sql;
	private final List<?> parameters;
	private final Map<String, ?> attributes;

	PreparedQueryPartResultImpl(Optional<String> sql, List<?> parameters, Map<String, ?> attributes) {
		this.sql = sql;
		this.parameters = parameters;
		this.attributes = attributes;
	}

	@Override
	public Optional<String> getSql() {
		return sql;
	}

	@Override
	public List<?> getParameters() {
		return parameters;
	}

	@Override
	public Map<String, ?> getAttributes() {
		return attributes;
	}
}
