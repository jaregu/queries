package com.jaregu.database.queries.building;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.ext.OffsetLimit;
import com.jaregu.database.queries.ext.PageableSearch;
import com.jaregu.database.queries.ext.SortProperties;
import com.jaregu.database.queries.ext.SortProperty;
import com.jaregu.database.queries.ext.SortableSearch;

public final class QueryImpl implements Query {

	private final String sql;
	private final List<?> parameters;
	private final Map<String, ?> attributes;
	private final Dialect dialect;

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
	public <T> T map(Function<Query, T> mapper) {
		return mapper.apply(this);
	}

	@Override
	public Stream<Query> stream() {
		return Stream.of(this);
	}

	@Override
	public void consume(Consumer<Query> consumer) {
		consumer.accept(this);
	}

	@Override
	public Query toSortedQuery(String... sortPorperties) {
		return toSortedQuery(Arrays.asList(sortPorperties));
	}

	@Override
	public Query toSortedQuery(Iterable<String> sortPorperties) {
		return toSortedQuery(SortProperties.of(StreamSupport.stream(sortPorperties.spliterator(), false)
				.map(SortProperty::of).collect(Collectors.toList())));
	}

	@Override
	public Query toSortedQuery(SortableSearch sortableSearch) {
		return toSortedQuery(sortableSearch.getSortProperties());
	}

	@Override
	public Query toSortedQuery(SortProperties sortProperties) {
		return dialect.toSortedQuery(this, sortProperties);
	}

	@Override
	public Query toPagedQuery(PageableSearch pageableSearch) {
		return toPagedQuery(pageableSearch.getOffsetLimit());
	}

	@Override
	public Query toPagedQuery(int offset, int limit) {
		return toPagedQuery(OffsetLimit.of(offset, limit));
	}

	@Override
	public Query toPagedQuery(OffsetLimit offsetLimit) {
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
			return sql.equals(other.sql) && parameters.equals(other.parameters) && attributes.equals(other.attributes);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sql, parameters, attributes);
	}
}
