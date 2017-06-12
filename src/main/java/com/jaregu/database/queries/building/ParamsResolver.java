package com.jaregu.database.queries.building;

import java.util.Map;

@FunctionalInterface
public interface ParamsResolver {

	/**
	 * Returns the value of given variable, which could be null.
	 *
	 * @throws QueryBuildException
	 *             if resolution fails
	 */
	Object getValue(String variable);

	/**
	 * Returns a {@link ParamsResolver} that is backed by given map.
	 */
	public static ParamsResolver forMap(Map<String, ?> map) {
		return variable -> {
			Object value = map.get(variable);
			if (value != null || map.containsKey(variable)) {
				return value;
			} else {
				throw new QueryBuildException("No value registered for key: '" + variable + "'");
			}
		};
	}

	/**
	 * Returns a {@link ParamsResolver} that is backed by given bean. When
	 * variables are looked up, tries to find a matching getter or accessible
	 * field for the variable and returns its value. Supports nested beans.
	 */
	public static ParamsResolver forBean(Object object) {
		return new BeanParams(object);
	}

	public static ParamsResolver empty() {
		return variable -> {
			throw new QueryBuildException("Empty variable resolver!");
		};
	}
}