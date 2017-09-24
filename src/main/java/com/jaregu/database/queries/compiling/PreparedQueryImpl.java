package com.jaregu.database.queries.compiling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.building.IteratorResolver;
import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryImpl;
import com.jaregu.database.queries.compiling.PreparedQueryPart.Result;
import com.jaregu.database.queries.dialect.Dialect;

final class PreparedQueryImpl implements PreparedQuery {

	final private QueryId id;
	final private List<PreparedQueryPart> parts;
	final private Dialect dialect;

	PreparedQueryImpl(QueryId id, List<PreparedQueryPart> parts, Dialect dialect) {
		this.id = id;
		this.parts = parts;
		this.dialect = dialect;
	}

	@Override
	public QueryId getQueryId() {
		return id;
	}

	@Override
	public Query build() {
		return build(ParametersResolver.empty());
	}

	@Override
	public Query build(Object params) {
		return build(ParametersResolver.ofObject(params));
	}

	@Override
	public Query build(Map<String, ?> params) {
		return build(ParametersResolver.ofMap(params));
	}

	@Override
	public Query build(List<?> params) {
		return build(ParametersResolver.ofList(params));
	}

	@Override
	public Query build(Object... params) {
		return build(Arrays.asList(params));
	}

	@Override
	public Query build(String k1, Object v1) {
		return build(Collections.singletonMap(k1, v1));
	}

	@Override
	public Query build(String k1, Object v1, String k2, Object v2) {
		Map<String, Object> params = new HashMap<>(2);
		params.put(k1, v1);
		params.put(k2, v2);
		return build(params);
	}

	@Override
	public Query build(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
		Map<String, Object> params = new HashMap<>(3);
		params.put(k1, v1);
		params.put(k2, v2);
		params.put(k3, v3);
		return build(params);
	}

	@Override
	public Query build(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
		Map<String, Object> params = new HashMap<>(4);
		params.put(k1, v1);
		params.put(k2, v2);
		params.put(k3, v3);
		params.put(k4, v4);
		return build(params);
	}

	@Override
	public Query build(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4,
			String k5, Object v5) {
		Map<String, Object> params = new HashMap<>(5);
		params.put(k1, v1);
		params.put(k2, v2);
		params.put(k3, v3);
		params.put(k4, v4);
		params.put(k5, v5);
		return build(params);
	}

	@Override
	public Query build(NamedResolver resolver) {
		return build(ParametersResolver.ofNamedParameters(resolver));
	}

	@Override
	public Query build(IteratorResolver resolver) {
		return build(ParametersResolver.ofIteratorParameters(resolver));
	}

	@Override
	public Query build(Iterable<?> resolver) {
		return build(ParametersResolver.ofIterable(resolver));
	}

	@Override
	public Query build(ParametersResolver resolver) {
		StringBuilder sql = new StringBuilder();
		List<Object> allParameters = new ArrayList<>();
		Map<String, Object> allAttributes = new HashMap<>();
		for (PreparedQueryPart part : parts) {
			Result result = part.build(resolver);
			result.getSql().ifPresent(sql::append);
			allParameters.addAll(result.getParameters());
			allAttributes.putAll(result.getAttributes());
		}
		return new QueryImpl(sql.toString(), allParameters, allAttributes, dialect);
	}

	@Override
	public String toString() {
		String idToString = id.toString();
		StringBuilder sb = new StringBuilder(idToString.length() + 20);
		sb.append("PreparedQuery{id: ").append(idToString).append("}");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof PreparedQueryImpl) {
			PreparedQueryImpl other = (PreparedQueryImpl) obj;
			return id.equals(other.id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}