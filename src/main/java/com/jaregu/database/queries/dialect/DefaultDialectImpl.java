package com.jaregu.database.queries.dialect;

import java.util.ArrayList;
import java.util.List;

import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryImpl;
import com.jaregu.database.queries.ext.OffsetLimit;
import com.jaregu.database.queries.ext.SortProperties;

public class DefaultDialectImpl implements Dialect {

	@Override
	public Query toSortedQuery(Query query, SortProperties sortProperties) {
		if (!sortProperties.isEmpty()) {
			StringBuilder newSql = new StringBuilder("SELECT x.* FROM /* ORDER BY wrapper by default dialect */ (\n")
					.append(query.getSql()).append("\n) x").append(" ").append(sortProperties.toSql());
			return new QueryImpl(newSql.toString(), query.getParameters(), query.getAttributes(), this);
		}
		return query;
	}

	@Override
	public Query toPagedQuery(Query query, OffsetLimit offsetLimit) {
		if (offsetLimit.getLimit() != null) {
			List<Object> parameters = new ArrayList<>(query.getParameters().size() + 2);
			parameters.addAll(query.getParameters());
			StringBuilder newSql = new StringBuilder(query.getSql()).append("\nLIMIT ?");
			parameters.add(offsetLimit.getLimit());
			if (offsetLimit.getOffset() != null) {
				newSql.append(" OFFSET ?");
				parameters.add(offsetLimit.getOffset());
			}
			return new QueryImpl(newSql.toString(), parameters, query.getAttributes(), this);
		}
		return query;
	}

	@Override
	public Query toCountQuery(Query query) {
		StringBuilder newSql = new StringBuilder(
				"SELECT COUNT(1) as 'row_count' FROM /* COUNT wrapper by default dialect */ (\n").append(query.getSql())
						.append("\n) x");
		return new QueryImpl(newSql.toString(), query.getParameters(), query.getAttributes(), this);
	}
}
