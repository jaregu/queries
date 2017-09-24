package com.jaregu.database.queries.building;

import java.util.List;

/**
 * Parameter binder is called for every binded parameter place while building
 * query.
 * <p>
 * 
 * Usually parameter binder can be used to create some <code>IN</code> clause
 * support.
 * 
 * See {@link ParameterBinders#binder(ParameterBinder)}
 */
public interface ParameterBinder {

	Result process(Object parameter);

	interface Result {

		String getSql();

		List<Object> getParemeters();
	}
}
