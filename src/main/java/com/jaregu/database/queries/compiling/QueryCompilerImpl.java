package com.jaregu.database.queries.compiling;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.building.ParameterBinder;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Source;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.parsing.ParsedQuery;
import com.jaregu.database.queries.parsing.ParsedQueryPart;

class QueryCompilerImpl implements QueryCompiler, Compiler {

	private List<QueryCompilerFeature> features;

	private Dialect dialect;

	public static QueryCompiler of(QueriesConfig config) {

		ExpressionParser expressionParser = ExpressionParser.defaultParser();
		ParameterBinder parameterBinder = config.getParameterBinder();

		List<QueryCompilerFeature> features = Arrays.asList(
				new IgnoredCommentFeature(),
				new BlockFeature(expressionParser),
				new OptionalHyphenNamedParameterFeature(expressionParser, parameterBinder),
				new OptionalSlashNamedParameterFeature(expressionParser, parameterBinder),
				new NamedVariableFeature(parameterBinder),
				new AnonymousVariableFeature(parameterBinder),
				new AssignmentFeature(expressionParser),
				new EntityFieldsFeature(expressionParser, config.getEntities()));

		return new QueryCompilerImpl(features, config.getDialect());
	}

	QueryCompilerImpl(List<QueryCompilerFeature> features, Dialect dialect) {
		this.features = features;
		this.dialect = dialect;
	}

	@Override
	public PreparedQuery compile(ParsedQuery sourceQuery) {
		return CompileContext.of(sourceQuery).withContext(() -> {
			PartsCompiler compiler = new PartsCompiler(sourceQuery.getParts());
			compiler.compile();
			return new PreparedQueryImpl(sourceQuery.getQueryId(), compiler.getCompiledParts(), dialect);
		});
	}

	@Override
	public Result compile(Source source) {
		PartsCompiler compiler = new PartsCompiler(source.getParts());
		compiler.compile();
		return compiler::getCompiledParts;
	}

	private class PartsCompiler {

		private List<ParsedQueryPart> sourceParts;
		private List<PreparedQueryPart> compiledParts;

		public PartsCompiler(List<ParsedQueryPart> sourceParts) {
			this.sourceParts = sourceParts;
		}

		public void compile() {
			compiledParts = new LinkedList<>();

			boolean compiled = false;
			int start;
			int end;
			for (start = 0; start < sourceParts.size(); start++) {
				for (end = start + 1; end <= sourceParts.size(); end++) {
					compiled = false;
					Source source = Source.of(sourceParts.subList(start, end));
					try {
						for (QueryCompilerFeature feature : features) {
							if (feature.isCompilable(source)) {
								Result result = feature.compile(source, QueryCompilerImpl.this);
								compiledParts.addAll(result.getParts());
								compiled = true;
								break;
							}
						}
					} catch (Throwable e) {
						String query;
						ParsedQuery sourceQuery = CompileContext.getCurrent().getSourceQuery();
						ParsedQueryPart problemStartPart = sourceParts.get(start);
						StringBuilder problemQuery = new StringBuilder(" ");
						for (ParsedQueryPart sourcePart : sourceQuery.getParts()) {
							if (problemStartPart == sourcePart) {
								problemQuery.append("[ERROR COMPILING --->]\n");
								for (ParsedQueryPart problemPart : source.getParts()) {
									problemQuery.append(problemPart.getContent());
								}
								problemQuery.append("\n[<--- ERROR COMPILING][...]");
								break;
							} else {
								problemQuery.append(sourcePart.getContent());
							}
						}
						query = problemQuery.toString();
						throw new QueryCompileException("Exception (" + e.getMessage() + "): " + query, e);
					}
					if (compiled) {
						break;
					}
				}

				if (compiled) {
					start = end - 1;
				} else {
					compiledParts.add(PreparedQueryPart.constant(sourceParts.get(start).getContent()));
				}
			}
		}

		public List<PreparedQueryPart> getCompiledParts() {
			return compiledParts;
		}
	}
}
