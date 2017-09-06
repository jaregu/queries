package com.jaregu.database.queries.building;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class QueryImpl implements Query {

	private String sql;
	private List<Object> parameters;
	private Map<String, Object> attributes;

	public QueryImpl(String sql, List<Object> parameters, Map<String, Object> attributes) {
		this.sql = sql;
		this.parameters = Collections.unmodifiableList(parameters);
		this.attributes = Collections.unmodifiableMap(attributes);
	}

	@Override
	public String getSql() {
		return sql;
	}

	@Override
	public List<Object> getParameters() {
		return parameters;
	}

	@Override
	public <T> T map(Function<Query, T> mapper) {
		return mapper.apply(this);
	}

	@Override
	public Stream<Query> stream() {
		return Stream.of(this);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
