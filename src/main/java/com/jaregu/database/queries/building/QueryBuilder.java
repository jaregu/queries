package com.jaregu.database.queries.building;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface QueryBuilder<T> {

	/**
	 * Builds {@link Query} using passed resolver. See other build methods for
	 * alternative ways of passing parameters. Or use {@link ParametersResolver}
	 * static methods for explicit resolver creation.
	 * 
	 * @param resolver
	 */
	T build(ParametersResolver resolver);

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver} static methods
	 * 
	 */
	default T build() {
		return build(ParametersResolver.empty());
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofObject(Object)} static method
	 * 
	 */
	default T build(Object params) {
		return build(ParametersResolver.ofObject(params));
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofMap(Map)} static method
	 * 
	 */
	default T build(Map<String, ?> params) {
		return build(ParametersResolver.ofMap(params));
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofList(List)} static methods
	 * 
	 */
	default T build(List<?> params) {
		return build(ParametersResolver.ofList(params));
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofList(List)} static method
	 * 
	 */
	default T build(Object... params) {
		return build(Arrays.asList(params));
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofMap(Map)} static method
	 * 
	 */
	default T build(String k1, Object v1) {
		return build(Collections.singletonMap(k1, v1));
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofMap(Map)} static method
	 * 
	 */
	default T build(String k1, Object v1, String k2, Object v2) {
		Map<String, Object> params = new HashMap<>(2);
		params.put(k1, v1);
		params.put(k2, v2);
		return build(params);
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofMap(Map)} static method
	 * 
	 */
	default T build(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
		Map<String, Object> params = new HashMap<>(3);
		params.put(k1, v1);
		params.put(k2, v2);
		params.put(k3, v3);
		return build(params);
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofMap(Map)} static method
	 * 
	 */
	default T build(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
		Map<String, Object> params = new HashMap<>(4);
		params.put(k1, v1);
		params.put(k2, v2);
		params.put(k3, v3);
		params.put(k4, v4);
		return build(params);
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofMap(Map)} static method
	 * 
	 */
	default T build(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5,
			Object v5) {
		Map<String, Object> params = new HashMap<>(5);
		params.put(k1, v1);
		params.put(k2, v2);
		params.put(k3, v3);
		params.put(k4, v4);
		params.put(k5, v5);
		return build(params);
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofNamedParameters(NamedResolver)} static method
	 * 
	 */
	default T build(NamedResolver resolver) {
		return build(ParametersResolver.ofNamedParameters(resolver));
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofIteratorParameters(IteratorResolver)} static
	 * method
	 * 
	 */
	default T build(IteratorResolver resolver) {
		return build(ParametersResolver.ofIteratorParameters(resolver));
	}

	/**
	 * See {@link #build(ParametersResolver)}, this is shorthand method for
	 * {@link ParametersResolver#ofIterable(Iterable)} static method
	 * 
	 */
	default T build(Iterable<?> resolver) {
		return build(ParametersResolver.ofIterable(resolver));
	}
}
