package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jaregu.database.queries.building.ParametersResolver;

public interface PreparedQueryPart {

	public static final Result EMPTY = new Result() {

		@Override
		public Optional<String> getSql() {
			return Optional.empty();
		}

		@Override
		public List<Object> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Map<String, Object> getAttributes() {
			return Collections.emptyMap();
		}
	};

	Result build(ParametersResolver resolver);

	public static PreparedQueryPart constant(String sql) {
		return new ConstantPart(sql);
	}

	interface Result {

		Optional<String> getSql();

		List<Object> getParameters();

		Map<String, Object> getAttributes();
	}
}
