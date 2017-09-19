package com.jaregu.database.queries.building;

import java.util.List;

import com.jaregu.database.queries.building.ParameterBindingCollectionBuilderImpl.RestParametersType;

/**
 * SQL query parameter binding builder class, default output for this is just
 * one question mark ? and same object. Can be used to build some IN clause
 * support or some other parameter value unwrapping/converting
 *
 */
public interface ParameterBindingBuilder {

	Result process(Object parameter);

	interface Result {

		String getSql();

		List<Object> getParemeters();
	}

	static ParameterBindingBuilder createDefault() {
		return new ParameterBindingBuilderImpl();
	}

	/**
	 * Creates parameter binding where collection types are binded as collection
	 * of single parameters and for each parameter there is ? created in SQL.
	 * <p>
	 * 
	 * Template sizes has to be defined, so result SQL will not create too much
	 * unique statements, but will be in size of one of defined.
	 * <p>
	 * 
	 * Builder will throw an error if collection size will exceed maximum
	 * template size and usually there is some limit for SQL server side too.
	 * <p>
	 * 
	 * If collection is too big, use some other technique like temporary table.
	 * <p>
	 * 
	 * @param templateSizes
	 *            - list of predefined question mark counts, usually something
	 *            like 1, 5, 50, 100, ...
	 * @param restParamType
	 *            - what value to use for empty parameters
	 * @return
	 */
	static ParameterBindingBuilder createWithInClauseSupport(List<Integer> templateSizes,
			RestParametersType restParamType) {
		return new ParameterBindingCollectionBuilderImpl(templateSizes, restParamType);
	}
}
