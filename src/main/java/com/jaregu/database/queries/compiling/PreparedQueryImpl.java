package com.jaregu.database.queries.compiling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jaregu.database.queries.QueryId;
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
