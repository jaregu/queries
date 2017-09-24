package com.jaregu.database.queries.building;

import java.util.Collections;
import java.util.List;

public class ParameterBindingBuilderImpl implements ParameterBinder {

	@Override
	public Result process(Object parameter) {
		return new ResultImpl(parameter);
	}

	private static class ResultImpl implements Result {

		private static final String VARIABLE_SQL = "?";

		private Object value;

		public ResultImpl(Object value) {
			this.value = value;
		}

		@Override
		public String getSql() {
			return VARIABLE_SQL;
		}

		@Override
		public List<Object> getParemeters() {
			return Collections.singletonList(value);
		}
	}
}
