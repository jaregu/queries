package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.jaregu.database.queries.QueriesContext;
import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParameterBindingBuilder;
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
public class NamedVariableFeature implements QueryCompilerFeature {

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		return parts.size() == 1 && parts.get(0).isNamedVariable();
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		List<ParsedQueryPart> sourceParts = source.getParts();
		ParsedQueryPart variable = sourceParts.get(0);
		ParameterBindingBuilder bindingBuilder = QueriesContext.getCurrent().getConfig().getParameterBindingBuilder();
		return new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(new NamedVariablePart(variable.getVariableName(), bindingBuilder));
			}
		};
	}

	private static class NamedVariablePart implements PreparedQueryPart {

		private String variableName;
		private ParameterBindingBuilder bindingBuilder;

		public NamedVariablePart(String variableName, ParameterBindingBuilder bindingBuilder) {
			this.variableName = variableName;
			this.bindingBuilder = bindingBuilder;
		}

		@Override
		public Result build(ParametersResolver resolver) {

			NamedResolver namedParameters = resolver.getNamedResolver();
			Object value = namedParameters.getValue(variableName);
			ParameterBindingBuilder.Result result = bindingBuilder.process(value);

			return new PreparedQueryPartResultImpl(Optional.of(result.getSql()), result.getParemeters(),
					Collections.emptyMap());
		}
	}
}
