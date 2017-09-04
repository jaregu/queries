package com.jaregu.database.queries.building;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class BuildtQueryImpl implements BuildtQuery {

	private String sql;
	private List<Object> parameters;
	private Map<String, Object> attributes;

	public BuildtQueryImpl(String sql, List<Object> parameters, Map<String, Object> attributes) {
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
	public <T> T map(Function<BuildtQuery, T> mapper) {
		return mapper.apply(this);
	}

	@Override
	public Stream<BuildtQuery> stream() {
		return Stream.of(this);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
