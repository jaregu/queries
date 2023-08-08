package com.jaregu.database.queries.ext.dalesbred;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.dalesbred.datatype.SqlArray;
import org.dalesbred.query.SqlQuery;

import com.jaregu.database.queries.building.Query;

public class QueriesUtil {

	public static SqlQuery toQuery(Query q) {

		List<?> parameters = q.getParameters();
		if (parameters.stream()
				.anyMatch(p -> p != null
						&& p.getClass().isArray()
						&& p.getClass().getComponentType().equals(UUID.class))) {

			parameters = parameters.stream()
					.map(p -> {
						if (p != null
								&& p.getClass().isArray()
								&& p.getClass().getComponentType().equals(UUID.class)) {
							UUID[] uuids = (UUID[]) p;
							return SqlArray.of("UUID", uuids);
						} else {
							return p;
						}
					}).collect(Collectors.toList());
		}

		return SqlQuery.query(q.getSql(), parameters);
	}
}
