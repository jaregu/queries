package com.jaregu.database.queries.building;

import java.util.List;

import com.jaregu.database.queries.building.ParameterBindingCollectionBuilderImpl.RestParametersType;

public interface ParameterBinders<T extends ParameterBinders<?>> {

	/**
	 * Sets parameter binder. Parameter binder is called for every binded
	 * parameter place in query.
	 * <p>
	 * 
	 * Usually parameter binder can be used to create some <code>IN</code>
	 * clause support. For example if there was a query like
	 * 
	 * <pre>
	 * SELECT * FROM dummy WHERE id IN (?)
	 * </pre>
	 * 
	 * then with correct binder implementation resulting query could be
	 * something like
	 * 
	 * <pre>
	 * SELECT * FROM dummy WHERE id IN (?,?,?)
	 * </pre>
	 * 
	 * when passed parameter is some type with three elements. See
	 * {@link #binderWithCollectionSupport(List, RestParametersType)}
	 * <p>
	 * 
	 * @param binder
	 * @return
	 */
	T binder(ParameterBinder binder);

	/**
	 * Build queries with parameter binder who processes collections as single
	 * parameters list and for each parameter there is ? created in SQL.
	 * <p>
	 * 
	 * Template sizes has to be defined, so result SQL will not create too much
	 * unique statements and parameters count will be one of defined.
	 * <p>
	 * 
	 * Binder will throw an error if collection size will exceed largest
	 * template size and usually there is some kind of limit for SQL server too.
	 * <p>
	 * 
	 * If collection is too big, use some other technique like temporary table.
	 * <p>
	 * 
	 * @param templateSizes
	 *            - list of binded variable count sizes to choose from when
	 *            building SQL, usually something like 1, 5, 50, 100, ...
	 * @param restParamType
	 *            - what value to use for empty parameters
	 * @return
	 */
	default T binderWithCollectionSupport(List<Integer> templateSizes, RestParametersType restParamType) {
		return binder(withCollectionSupport(templateSizes, restParamType));
	}

	/**
	 * Build queries with default parameter binder
	 * 
	 * @return
	 */
	default T binderDefault() {
		return binder(defaultBinder());
	}

	/**
	 * Default parameter binder, does nothing
	 * 
	 * @return
	 */
	static ParameterBinder defaultBinder() {
		return new ParameterBindingBuilderImpl();
	}

	/**
	 * Creates parameter binding where collection types are binded as collection
	 * of single parameters and for each parameter there is ? created in SQL.
	 * <p>
	 * 
	 * Template sizes has to be defined, so result SQL will not create too much
	 * unique statements and parameters count will be one of defined.
	 * <p>
	 * 
	 * Binder will throw an error if collection size will exceed largest
	 * template size and usually there is some kind of limit for SQL server too.
	 * <p>
	 * 
	 * If collection is too big, use some other technique like temporary table.
	 * <p>
	 * 
	 * @param templateSizes
	 *            - list of binded variable count sizes to choose from when
	 *            building SQL, usually something like 1, 5, 50, 100, ...
	 * @param restParamType
	 *            - what value to use for empty parameters
	 * @return
	 */
	static ParameterBinder withCollectionSupport(List<Integer> templateSizes, RestParametersType restParamType) {
		return new ParameterBindingCollectionBuilderImpl(templateSizes, restParamType);
	}
}
