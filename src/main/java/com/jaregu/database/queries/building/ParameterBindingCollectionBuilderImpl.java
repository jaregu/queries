package com.jaregu.database.queries.building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ParameterBindingCollectionBuilderImpl extends ParameterBindingBuilderImpl {

	public static enum RestParametersType {
		NULL, LAST_VALUE;
	}

	private final List<Integer> templateSizes;
	private RestParametersType restParamType;

	public ParameterBindingCollectionBuilderImpl(List<Integer> templateSizes, RestParametersType restParamType) {
		this.restParamType = restParamType;
		this.templateSizes = templateSizes.stream().filter(s -> s != null && s > 0).sorted()
				.collect(Collectors.toList());
		if (templateSizes.isEmpty()) {
			throw new IllegalArgumentException("Template sizes has to contain at least one positive integer!");
		}
	}

	@Override
	public Result process(Object parameter) {
		if (parameter instanceof Collection<?>) {
			int size = ((Collection<?>) parameter).size();
			if (size == 0) {
				throw new QueryBuildException("Can't bind collection as repeated ?,?,?... parameters?"
						+ " Empty parameter collection, use some conditional requirements to add this SQL clause!");
			}
			int lastSize = 0;
			for (Integer templateSize : templateSizes) {
				if (size <= templateSize) {
					return new ResultImpl((Collection<?>) parameter, templateSize);
				}
				lastSize = templateSize;
			}
			throw new QueryBuildException("Can't bind collection as repeated ?,?,?... parameters?"
					+ " Maximum configured parameter count is: " + lastSize + " collections has " + size
					+ " elements!");
		} else {
			return super.process(parameter);
		}
	}

	private class ResultImpl implements Result {

		private static final String FIRST = "?";
		private static final String REST = ",?";

		private Collection<?> values;
		private int places;

		public ResultImpl(Collection<?> values, int places) {
			this.values = values;
			this.places = places;
		}

		@Override
		public String getSql() {
			StringBuilder sql = new StringBuilder(FIRST);
			if (places > 1) {
				for (int i = 1; i < places; i++) {
					sql.append(REST);
				}
			}
			return sql.toString();
		}

		@Override
		public List<Object> getParemeters() {
			List<Object> parameters = new ArrayList<>(places);
			parameters.addAll(values);
			Object lastValue = parameters.get(parameters.size() - 1);
			int additionalCount = places - parameters.size();
			for (int i = 0; i < additionalCount; i++) {
				if (restParamType == RestParametersType.LAST_VALUE) {
					parameters.add(lastValue);
				} else {
					parameters.add(null);
				}
			}
			return parameters;
		}
	}
}
