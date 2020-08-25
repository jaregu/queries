package com.jaregu.database.queries.dialect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryImpl;

public class DefaultDialectImpl implements Dialect {

	@Override
	public Query toOrderedQuery(Query query, Orderable orderable) {
		List<String> items;
		if (orderable != null && (items = orderable.getOrderBy()) != null && !items.isEmpty()) {

			StringBuilder newSql = new StringBuilder("SELECT x.* FROM /* ORDER BY wrapper by default dialect */ (\n")
					.append(query.getSql()).append("\n) x ORDER BY ");
			Iterator<String> iterator = items.iterator();

			newSql.append(iterator.next());
			while (iterator.hasNext()) {
				newSql.append(", ").append(iterator.next());
			}

			return new QueryImpl(newSql.toString(), query.getParameters(), query.getAttributes(), this);
		}
		return query;
	}

	@Override
	public Query toPagedQuery(Query query, Pageable pageable) {
		if (pageable.getLimit() != null) {
			List<Object> parameters = new ArrayList<>(query.getParameters().size() + 2);
			parameters.addAll(query.getParameters());
			StringBuilder newSql = new StringBuilder(query.getSql()).append("\nLIMIT ?");
			parameters.add(pageable.getLimit());
			if (pageable.getOffset() != null) {
				newSql.append(" OFFSET ?");
				parameters.add(pageable.getOffset());
			}
			return new QueryImpl(newSql.toString(), parameters, query.getAttributes(), this);
		}
		return query;
	}

	@Override
	public Query toCountQuery(Query query) {
		StringBuilder newSql = new StringBuilder(
				"SELECT COUNT(1) as row_count FROM /* COUNT wrapper by default dialect */ (\n").append(query.getSql())
						.append("\n) x");
		return new QueryImpl(newSql.toString(), query.getParameters(), query.getAttributes(), this);
	}

	@Override
	public String getSuffix() {
		return null;
	}
}
