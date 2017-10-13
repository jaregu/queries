package com.jaregu.database.queries.building;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Orderable;
import com.jaregu.database.queries.dialect.Pageable;

public final class QueryImpl implements Query {

	private final String sql;
	private final List<?> parameters;
	private final Map<String, ?> attributes;
	private transient final Dialect dialect;

	public QueryImpl(String sql, List<?> parameters, Map<String, ?> attributes, Dialect dialect) {
		this.sql = sql;
		this.dialect = dialect;
		this.parameters = Collections.unmodifiableList(parameters);
		this.attributes = Collections.unmodifiableMap(attributes);
	}

	@Override
	public String getSql() {
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

	@Override
	public Query toOrderedQuery(Orderable orderable) {
		return dialect.toOrderedQuery(this, orderable);
	}

	@Override
	public Query toPagedQuery(Pageable offsetLimit) {
		return dialect.toPagedQuery(this, offsetLimit);
	}

	@Override
	public Query toCountQuery() {
		return dialect.toCountQuery(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(10 + sql.length() + 10 * parameters.size() + 10 * attributes.size());
		sb.append(sql);
		sb.append("\n[");
		Iterator<?> it = parameters.iterator();
		if (it.hasNext()) {
			sb.append(it.next());
			while (it.hasNext()) {
				sb.append(", ").append(it.next());
			}
		}
		sb.append("]");
		if (!attributes.isEmpty()) {
			sb.append("\nattributes: ").append(attributes);
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof QueryImpl) {
			QueryImpl other = (QueryImpl) obj;
			return Objects.equals(sql, other.sql) && Objects.equals(parameters, other.parameters)
					&& Objects.equals(attributes, other.attributes);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sql, parameters, attributes);
	}
}
