package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.jaregu.database.queries.QueriesContext;
import com.jaregu.database.queries.building.IteratorResolver;
import com.jaregu.database.queries.building.ParameterBindingBuilder;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.QueryBuildException;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Required bind parameter feature.
 * 
 * Example SQL:
 * 
 * <pre>
 * [SQL] ? [SQL]
 * </pre>
 */
public class AnonymousVariableFeature implements QueryCompilerFeature {

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		return parts.size() == 1 && parts.get(0).isAnonymousVariable();
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		ParameterBindingBuilder bindingBuilder = QueriesContext.getCurrent().getConfig().getParameterBindingBuilder();
		return new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(new AnonymousVariablePart(bindingBuilder));
			}
		};
	}

	protected static class AnonymousVariablePart implements PreparedQueryPart {

		private ParameterBindingBuilder bindingBuilder;

		public AnonymousVariablePart(ParameterBindingBuilder bindingBuilder) {
			this.bindingBuilder = bindingBuilder;
		}

		@Override
		public Result build(ParametersResolver resolver) {

			IteratorResolver iteratorParameters = resolver.getIteratorResolver();
			if (!iteratorParameters.hasNext()) {
				throw new QueryBuildException(
						"Can't resolve parameter for for binding! Parameters iterator was empty or is exausted!");
			}
			Object value = iteratorParameters.next();
			ParameterBindingBuilder.Result result = bindingBuilder.process(value);

			return new PreparedQueryPartResultImpl(Optional.of(result.getSql()), result.getParemeters(),
					Collections.emptyMap());
		}
	}
}
