package com.jaregu.database.queries.building;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.jaregu.database.queries.compiling.PreparedQuery;

/**
 * Implementations is used to contain parameters for successful {@link Query}
 * building using {@link PreparedQuery} build methods. Depending on SQL
 * statement {@link #toNamed()} or {@link #toIterator()} will be called.
 * <p>
 * 
 * {@link ParametersResolver} has to support only one type resolving. First call
 * <i>wins</i> and it is error to call other resolving method even when backing
 * data supports both resolvers.
 * <p>
 * 
 * Interface also contains static create methods for ParametersResolver
 * creation.
 *
 */
public interface ParametersResolver {

	NamedResolver toNamed();

	IteratorResolver toIterator();

	public static ParametersResolver empty() {
		return new ParametersResolverImpl(() -> {
			throw new QueryBuildException("Empty variable resolver! Can't create named variable resolver!");
		}, () -> {
			throw new QueryBuildException("Empty variable resolver! Can't create iterable variable resolver!");
		});
	}

	public static ParametersResolver ofObject(Object object) {
		return new ParametersResolverImpl(() -> NamedResolver.ofBean(object),
				() -> IteratorResolver.of(Collections.singletonList(object)));
	}

	public static ParametersResolver ofMap(Map<String, ?> map) {
		return new ParametersResolverImpl(() -> NamedResolver.ofMap(map), () -> {
			throw new QueryBuildException("Can't create iterable parameters resolver from map!");
		});
	}

	public static ParametersResolver ofList(List<?> parameters) {
		return new ParametersResolverImpl(() -> NamedResolver.ofList(parameters),
				() -> IteratorResolver.of(parameters));
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
		}, () -> IteratorResolver.of(parameters));
	}
}
