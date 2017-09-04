package com.jaregu.database.queries.compiling;

import java.util.List;
import java.util.Map;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.building.BuildtQuery;
import com.jaregu.database.queries.building.IteratorResolver;
import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParametersResolver;

public interface PreparedQuery {

	QueryId getQueryId();

	BuildtQuery build();

	BuildtQuery build(Object params);

	BuildtQuery build(Map<String, Object> params);

	BuildtQuery build(List<Object> params);

	BuildtQuery build(Object... params);

	BuildtQuery build(String k1, Object v1);

	BuildtQuery build(String k1, Object v1, String k2, Object v2);

	BuildtQuery build(String k1, Object v1, String k2, Object v2, String k3, Object v3);

	BuildtQuery build(NamedResolver resolver);

	BuildtQuery build(IteratorResolver resolver);

	BuildtQuery build(Iterable<Object> resolver);

	BuildtQuery build(ParametersResolver resolver);
}
