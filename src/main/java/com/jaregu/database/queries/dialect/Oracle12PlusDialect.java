package com.jaregu.database.queries.dialect;

import java.util.ArrayList;
import java.util.List;

import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryImpl;
import com.jaregu.database.queries.ext.OffsetLimit;

public class Oracle12PlusDialect extends DefaultDialectImpl {

	@Override
	public Query toPagedQuery(Query query, OffsetLimit offsetLimit) {
		if (offsetLimit.getLimit() != null) {
			List<Object> parameters = new ArrayList<>(query.getParameters().size() + 2);
			parameters.addAll(query.getParameters());
			StringBuilder newSql = new StringBuilder(query.getSql()).append("\n");
			if (offsetLimit.getOffset() != null) {
				newSql.append("OFFSET ? ");
				parameters.add(offsetLimit.getOffset());
			}
			newSql.append("ROWS FETCH NEXT ? ROWS ONLY");
			parameters.add(offsetLimit.getLimit());

			return new QueryImpl(newSql.toString(), parameters, query.getAttributes(), this);
		}
		return query;
	}
}
