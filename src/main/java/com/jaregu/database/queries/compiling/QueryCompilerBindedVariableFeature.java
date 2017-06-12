package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.parsing.SourceQueryPart;

/**
 * Required bind parameter feature.
 * 
 * Example SQL:
 * 
 * <pre>
 * [SQL] :someName [SQL]
 * </pre>
 */
public class QueryCompilerBindedVariableFeature implements QueryCompilerFeature {

	@Override
	public boolean isCompilable(Source source) {
		List<SourceQueryPart> parts = source.getParts();
		return parts.size() == 1 && parts.get(0).isBinding();
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		List<SourceQueryPart> sourceParts = source.getParts();
		SourceQueryPart variable = sourceParts.get(0);
		return new Result() {
			@Override
			public List<CompiledQueryPart> getCompiledParts() {
				return Collections.singletonList(new CompiledVariablePart(variable.getVariableName()));
			}
		};
	}

	protected static class CompiledVariablePart implements CompiledQueryPart {

		final private String variableName;

		public CompiledVariablePart(String variableName) {
			this.variableName = variableName;
		}

		@Override
		public void eval(ParamsResolver variableResolver, ResultConsumer resultConsumer) {

			resultConsumer.consume("?", Collections.singletonList(variableResolver.getValue(variableName)));
		}

		@Override
		public List<String> getVariableNames() {
			return Collections.singletonList(variableName);
		}
	}
}
