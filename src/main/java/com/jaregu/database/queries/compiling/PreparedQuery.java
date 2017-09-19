package com.jaregu.database.queries.compiling;

import java.util.List;
import java.util.Map;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.IteratorResolver;
import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParametersResolver;

public interface PreparedQuery {

	QueryId getQueryId();

	Query build();

	Query build(Object params);

	Query build(Map<String, Object> params);

	Query build(List<Object> params);

	Query build(Object... params);

	Query build(String k1, Object v1);

	Query build(String k1, Object v1, String k2, Object v2);

	Query build(String k1, Object v1, String k2, Object v2, String k3, Object v3);

	Query build(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4);

	Query build(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5,
			Object v5);

	Query build(NamedResolver resolver);

	Query build(IteratorResolver resolver);

	Query build(Iterable<Object> resolver);

	Query build(ParametersResolver resolver);
}
