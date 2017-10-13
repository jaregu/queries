package com.jaregu.database.queries.building;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface NamedResolver {

	/**
	 * Returns the value of given variable, which could be null.
	 * 
	 * @throws QueryBuildException
	 *             if resolution fails
	 * @param variable
	 * @return
	 */
	Object getValue(String variable);

	/**
	 * Returns a {@link NamedResolver} that is backed by given map.
	 * 
	 * @param map
	 * @return
	 */
	public static NamedResolver ofMap(Map<String, ?> map) {
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
	 * Returns a {@link NamedResolver} that is backed by given bean. When
	 * variables are looked up, tries to find a matching getter or accessible
	 * field for the variable and returns its value. Supports nested beans.
	 * 
	 * @param object
	 * @return
	 */
	public static NamedResolver ofBean(Object object) {
		return new BeanResolver(object);
	}

	/**
	 * Returns a {@link NamedResolver} that is backed by given list. Variable
	 * names must be valid indexes.
	 * 
	 * @param list
	 * @return
	 */
	public static NamedResolver ofList(List<?> list) {
		return variable -> {
			int index;
			try {
				index = Integer.parseInt(variable);
			} catch (NumberFormatException e) {
				throw new QueryBuildException("Variable name is not number: '" + variable + "'");
			}
			if (index >= 0 && index < list.size()) {
				return list.get(index);
			} else {
				throw new QueryBuildException("Invalid index: " + index + ", list size: " + list.size() + "");
			}
		};
	}
}