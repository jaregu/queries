package com.jaregu.database.queries.building;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Query {

	String getSql();

	List<Object> getParameters();

	Map<String, Object> getAttributes();

	<T> T map(Function<Query, T> mapper);

	Stream<Query> stream();
}
