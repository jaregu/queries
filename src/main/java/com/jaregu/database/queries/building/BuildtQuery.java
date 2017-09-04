package com.jaregu.database.queries.building;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface BuildtQuery {

	String getSql();

	List<Object> getParameters();

	Map<String, Object> getAttributes();

	<T> T map(Function<BuildtQuery, T> mapper);

	Stream<BuildtQuery> stream();
}
