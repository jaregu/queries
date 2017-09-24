package com.jaregu.database.queries.building;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ParametersResolver {

	NamedResolver getNamedResolver();

	IteratorResolver getIteratorResolver();

	public static ParametersResolver empty() {
		return new ParametersResolverImpl(() -> {
			throw new QueryBuildException("Empty variable resolver! Can't create named variable resolver!");
		}, () -> {
			throw new QueryBuildException("Empty variable resolver! Can't create iterable variable resolver!");
		});
	}

	public static ParametersResolver ofObject(Object object) {
		return new ParametersResolverImpl(() -> new BeanResolver(object),
				() -> new IteratorResolverImpl(Collections.singletonList(object)));
	}

	public static ParametersResolver ofMap(Map<String, ?> map) {
		return new ParametersResolverImpl(() -> NamedResolver.forMap(map), () -> {
			throw new QueryBuildException("Can't create iterable parameters resolver from map!");
		});
	}

	public static ParametersResolver ofList(List<?> parameters) {
		return new ParametersResolverImpl(() -> NamedResolver.forList(parameters),
				() -> new IteratorResolverImpl(parameters));
	}

	public static ParametersResolver ofNamedParameters(NamedResolver parameters) {
		return new ParametersResolverImpl(() -> parameters, () -> {
			throw new QueryBuildException("Can't create iterable parameters resolver from named parameters!");
		});
	}

	public static ParametersResolver ofIteratorParameters(IteratorResolver parameters) {
		return new ParametersResolverImpl(() -> {
			throw new QueryBuildException("Can't create named parameters resolver from iterator parameters!");
		}, () -> parameters);
	}

	public static ParametersResolver ofIterable(Iterable<?> parameters) {
		return new ParametersResolverImpl(() -> {
			throw new QueryBuildException("Can't create named parameters resolver from iterable parameters!");
		}, () -> new IteratorResolverImpl(parameters));
	}
}
