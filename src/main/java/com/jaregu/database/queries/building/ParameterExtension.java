package com.jaregu.database.queries.building;

import java.util.List;

public interface ParameterExtension {

	Result process(Object parameter);

	interface Result {

		String getSql();

		List<Object> getParemeters();
	}
}
