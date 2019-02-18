package com.jaregu.database.queries.ext.dalesbred;

import org.dalesbred.query.SqlQuery;

import com.jaregu.database.queries.building.Query;

public class QueriesUtil {

	public static SqlQuery toQuery(Query q) {
		return SqlQuery.query(q.getSql(), q.getParameters());
	}
}
