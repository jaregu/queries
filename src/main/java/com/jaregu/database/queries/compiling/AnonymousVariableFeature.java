package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.jaregu.database.queries.building.IteratorResolver;
import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.QueryBuildException;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

/**
 * Anonymous bind parameter feature.
 * 
 * Example SQL:
 * 
 * <pre>
 * [SQL] ? [SQL]
 * </pre>
 */
final class AnonymousVariableFeature implements QueryCompilerFeature {

	private final Result result;

	AnonymousVariableFeature(ParameterBinder parameterBinder) {
		this.result = new Result() {
			@Override
			public List<PreparedQueryPart> getParts() {
				return Collections.singletonList(new AnonymousVariablePart(parameterBinder));
			}
		};
	}

	@Override
	public boolean isCompilable(Source source) {
		List<ParsedQueryPart> parts = source.getParts();
		return parts.size() == 1 && parts.get(0).isAnonymousVariable();
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		return result;
	}

	private static final class AnonymousVariablePart implements PreparedQueryPart {

		private final ParameterBinder parameterBinder;

		private AnonymousVariablePart(ParameterBinder parameterBinder) {
			this.parameterBinder = parameterBinder;
		}

		@Override
		public Result build(ParametersResolver resolver) {

			IteratorResolver iteratorParameters = resolver.toIterator();
			if (!iteratorParameters.hasNext()) {
				throw new QueryBuildException(
						"Can't resolve parameter for for binding! Parameters iterator was empty or is exausted!");
			}
			Object value = iteratorParameters.next();
			ParameterBinder.Result result = parameterBinder.process(value);

			return new PreparedQueryPartResultImpl(Optional.of(result.getSql()), result.getParameters(),
					Collections.emptyMap());
		}

		@Override
		public String toString() {
			return "?";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(parameterBinder);
		}
	}
}
