package com.jaregu.database.queries.compiling;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.building.BuildtQuery;
import com.jaregu.database.queries.building.BuildtQueryImpl;
import com.jaregu.database.queries.building.IteratorResolver;
import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.compiling.PreparedQueryPart.Result;

class PreparedQueryImpl implements PreparedQuery {

	// private final static Logger LOGGER =
	// LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private QueryId id;
	private List<PreparedQueryPart> parts;

	public PreparedQueryImpl(QueryId id, List<PreparedQueryPart> parts) {
		this.id = id;
		this.parts = parts;
	}

	@Override
	public QueryId getQueryId() {
		return id;
	}

	@Override
	public BuildtQuery build() {
		return build(ParametersResolver.empty());
	}

	@Override
	public BuildtQuery build(Object params) {
		return build(ParametersResolver.ofObject(params));
	}

	@Override
	public BuildtQuery build(Map<String, Object> params) {
		return build(ParametersResolver.ofMap(params));
	}

	@Override
	public BuildtQuery build(List<Object> params) {
		return build(ParametersResolver.ofList(params));
	}

	@Override
	public BuildtQuery build(Object... params) {
		return build(Arrays.asList(params));
	}

	@Override
	public BuildtQuery build(String k1, Object v1) {
		return build(Collections.singletonMap(k1, v1));
	}

	@Override
	public BuildtQuery build(String k1, Object v1, String k2, Object v2) {
		Map<String, Object> params = new HashMap<>();
		params.put(k1, v1);
		params.put(k2, v2);
		return build(params);
	}

	@Override
	public BuildtQuery build(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
		Map<String, Object> params = new HashMap<>();
		params.put(k1, v1);
		params.put(k2, v2);
		params.put(k3, v3);
		return build(params);
	}

	@Override
	public BuildtQuery build(NamedResolver resolver) {
		return build(ParametersResolver.ofNamedParameters(resolver));
	}

	@Override
	public BuildtQuery build(IteratorResolver resolver) {
		return build(ParametersResolver.ofIteratorParameters(resolver));
	}

	@Override
	public BuildtQuery build(Iterable<Object> resolver) {
		return build(ParametersResolver.ofIterable(resolver));
	}

	@Override
	public BuildtQuery build(ParametersResolver resolver) {
		StringBuilder sql = new StringBuilder();
		List<Object> allParameters = new LinkedList<>();
		Map<String, Object> allAttributes = new HashMap<>();
		for (PreparedQueryPart part : parts) {
			Result result = part.build(resolver);
			result.getSql().ifPresent(sql::append);
			allParameters.addAll(result.getParameters());
			allAttributes.putAll(result.getAttributes());
		}
		return new BuildtQueryImpl(sql.toString(), allParameters, allAttributes);
	}
}
