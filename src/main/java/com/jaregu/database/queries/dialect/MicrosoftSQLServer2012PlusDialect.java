package com.jaregu.database.queries.dialect;

import java.util.ArrayList;
import java.util.List;

import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.QueryImpl;

public class MicrosoftSQLServer2012PlusDialect extends DefaultDialectImpl {

	@Override
	public Query toPagedQuery(Query query, Pageable pageable) {
		if (pageable.getLimit() != null) {
			List<Object> parameters = new ArrayList<>(query.getParameters().size() + 2);
			parameters.addAll(query.getParameters());
			StringBuilder newSql = new StringBuilder(query.getSql()).append("\n");
			if (pageable.getOffset() != null) {
				newSql.append("OFFSET ? ");
				parameters.add(pageable.getOffset());
			}
			newSql.append("ROWS FETCH NEXT ? ROWS ONLY");
			parameters.add(pageable.getLimit());

			return new QueryImpl(newSql.toString(), parameters, query.getAttributes(), this);
		}
		return query;
	}

	@Override
	public String getSuffix() {
		return "mssql";
	}
}
