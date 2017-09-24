package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Required bind parameter feature.
 * 
 * Example SQL:
 * 
 * <pre>
 * [SQL] :someName [SQL]
 * </pre>
 */
final class NamedVariableFeature implements QueryCompilerFeature {

	final private ParameterBinder parameterBinder;

	NamedVariableFeature(ParameterBinder parameterBinder) {
		this.parameterBinder = parameterBinder;
	}

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		return parts.size() == 1 && parts.get(0).isNamedVariable();
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		List<ParsedQueryPart> sourceParts = source.getParts();
		ParsedQueryPart variable = sourceParts.get(0);

		return new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(new NamedVariablePart(variable.getVariableName(), parameterBinder));
			}
		};
	}

	private static final class NamedVariablePart implements PreparedQueryPart {

		private final String variableName;
		private final ParameterBinder parameterBinder;

		public NamedVariablePart(String variableName, ParameterBinder parameterBinder) {
			this.variableName = variableName;
			this.parameterBinder = parameterBinder;
		}

		@Override
		public Result build(ParametersResolver resolver) {

			NamedResolver namedParameters = resolver.getNamedResolver();
			Object value = namedParameters.getValue(variableName);
			ParameterBinder.Result result = parameterBinder.process(value);

			return new PreparedQueryPartResultImpl(Optional.of(result.getSql()), result.getParemeters(),
					Collections.emptyMap());
		}

		@Override
		public String toString() {
			return ":" + variableName;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(variableName);
		}
	}
}
