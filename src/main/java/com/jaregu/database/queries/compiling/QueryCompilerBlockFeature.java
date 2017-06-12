package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.compiling.expr.Expression;
import com.jaregu.database.queries.parsing.SourceQueryPart;

/**
 * Nest-able SQL block feature. Will add conditionally everything between two
 * comments containing #> <#.
 * 
 * If conditional expression is omitted, then block is added when all inside
 * block used variables (inside in some other blocks or conditional parameters)
 * are supplied
 * 
 * Example SQL:
 * 
 * <pre>
 * ...
 * /* #> [<i>condition_expression</i>] *&#x2F;
 * [SQL, could contain other block and/or conditional parameters]
 * -- <# Block ends here
 * 
 * </pre>
 */
public class QueryCompilerBlockFeature implements QueryCompilerFeature {

	private static final String BLOCK_OPEN_SYMBOLS = "#>";
	private static final String BLOCK_CLOSE_SYMBOLS = "<#";

	@Override
	public boolean isCompilable(Source source) {
		List<SourceQueryPart> parts = source.getParts();
		return isAtLeastTwoParts(parts) && startsWithBlock(parts) && endsWithBlock(parts)
				&& isOpenedBlockCountEqualClosed(parts);
	}

	private boolean isAtLeastTwoParts(List<SourceQueryPart> parts) {
		return parts.size() >= 2;
	}

	private boolean startsWithBlock(List<SourceQueryPart> parts) {
		return parts.get(0).isComment() && parts.get(0).getCommentContent().startsWith(BLOCK_OPEN_SYMBOLS);
	}

	private boolean endsWithBlock(List<SourceQueryPart> parts) {
		return parts.get(parts.size() - 1).isComment()
				&& parts.get(parts.size() - 1).getCommentContent().startsWith(BLOCK_CLOSE_SYMBOLS);
	}

	private boolean isOpenedBlockCountEqualClosed(List<SourceQueryPart> parts) {
		int hierarhy = 0;
		for (SourceQueryPart part : parts) {
			if (part.isComment()) {
				if (part.getCommentContent().startsWith(BLOCK_OPEN_SYMBOLS)) {
					hierarhy++;
				} else if (part.getCommentContent().startsWith(BLOCK_CLOSE_SYMBOLS)) {
					hierarhy--;
				}

			}
		}
		return hierarhy == 0;
	}

	@Override
	public Result compile(Source source, Compiler compiler) {
		CompilingContext context = CompilingContext.getCurrent();
		List<SourceQueryPart> sourceParts = source.getParts();
		SourceQueryPart openComment = sourceParts.get(0);
		String conditionExpressionString = openComment.getCommentContent().substring(BLOCK_OPEN_SYMBOLS.length())
				.trim();

		List<CompiledQueryPart> children;
		if (sourceParts.size() > 2) {
			children = compiler.compile(Source.of(sourceParts.subList(1, sourceParts.size() - 1))).getCompiledParts();
		} else {
			children = Collections.emptyList();
		}

		List<String> variableNames = new LinkedList<>();
		for (CompiledQueryPart child : children) {
			variableNames.addAll(child.getVariableNames());
		}

		Function<ParamsResolver, Boolean> conditionFunction;
		if (conditionExpressionString.length() > 0) {
			Expression expression = context.getExpressionParser().parse(conditionExpressionString);
			variableNames.addAll(expression.getVariableNames());
			conditionFunction = (v) -> {
				Object result = expression.eval(v);
				return result != null && result instanceof Boolean && (Boolean) result;
			};
		} else {
			//condition function where block is added if all children variables is filled

			conditionFunction = (v) -> {
				boolean addBlock = true;
				for (String variableName : variableNames) {
					if (v.getValue(variableName) == null) {
						addBlock = false;
						break;
					}
				}
				return addBlock;
			};
		}

		return new Result() {
			@Override
			public List<CompiledQueryPart> getCompiledParts() {
				return Collections.singletonList(new CompiledQueryBlockPart(sourceParts.get(0).getContent(),
						sourceParts.get(sourceParts.size() - 1).getContent(), children, conditionFunction,
						variableNames));
			}
		};
	}

	protected static class CompiledQueryBlockPart implements CompiledQueryPart {

		final private String prefix;
		final private String suffix;
		final private List<CompiledQueryPart> children;
		final private Function<ParamsResolver, Boolean> conditionFunction;
		final private List<String> variableNames;

		public CompiledQueryBlockPart(String prefix, String suffix, List<CompiledQueryPart> children,
				Function<ParamsResolver, Boolean> conditionFunction, List<String> variableNames) {
			this.prefix = prefix;
			this.suffix = suffix;
			this.children = children;
			this.conditionFunction = conditionFunction;
			this.variableNames = Collections.unmodifiableList(variableNames);
		}

		@Override
		public void eval(ParamsResolver variableResolver, ResultConsumer resultConsumer) {
			if (conditionFunction.apply(variableResolver)) {
				List<Object> allParams = new LinkedList<>();
				StringBuilder sb = new StringBuilder(prefix);
				for (CompiledQueryPart part : children) {
					part.eval(variableResolver, (sql, params) -> {
						sb.append(sql);
						allParams.addAll(params);
					});
				}
				sb.append(suffix);
				resultConsumer.consume(sb.toString(), allParams);
			}
		}

		@Override
		public List<String> getVariableNames() {
			return variableNames;
		}
	}
}
