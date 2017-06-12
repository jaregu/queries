package com.jaregu.database.queries.compiling;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Compiler;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Result;
import com.jaregu.database.queries.compiling.QueryCompilerFeature.Source;
import com.jaregu.database.queries.compiling.expr.ExpressionParser;
import com.jaregu.database.queries.compiling.expr.ExpressionParserImpl;
import com.jaregu.database.queries.parsing.SourceQuery;
import com.jaregu.database.queries.parsing.SourceQueryPart;

public class QueryCompilerImpl implements QueryCompiler, Compiler {

	private ExpressionParser expressionParser;
	private List<QueryCompilerFeature> features;
	private QueriesConfig config;

	public static QueryCompiler createDefault(QueriesConfig config) {
		return new Builder(config).addDefaultFeatures().build();
	}

	public QueryCompilerImpl(QueriesConfig config, ExpressionParser expressionParser,
			List<QueryCompilerFeature> features) {
		this.expressionParser = expressionParser;
		this.features = features;
		this.config = config;
	}

	@Override
	public CompiledQuery compile(SourceQuery sourceQuery) {
		return CompilingContext.forExpressionParser(expressionParser).withConfig(config).build().withContext(() -> {
			PartsCompiler compiler = new PartsCompiler(sourceQuery.getParts());
			compiler.compile();
			return new CompiledQueryImpl(sourceQuery.getQueryId(), compiler.getCompiledParts());
		});
	}

	@Override
	public Result compile(Source source) {
		PartsCompiler compiler = new PartsCompiler(source.getParts());
		compiler.compile();
		return compiler::getCompiledParts;
	}

	private class PartsCompiler {

		private List<SourceQueryPart> sourceParts;
		private List<CompiledQueryPart> compiledParts;

		public PartsCompiler(List<SourceQueryPart> sourceParts) {
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
					for (QueryCompilerFeature feature : features) {
						if (feature.isCompilable(source)) {
							Result result = feature.compile(source, QueryCompilerImpl.this);
							compiledParts.addAll(result.getCompiledParts());
							compiled = true;
							break;
						}
					}
					if (compiled) {
						break;
					}
				}

				if (compiled) {
					start = end - 1;
				} else {
					compiledParts.add(CompiledQueryPart.constant(sourceParts.get(start).getContent()));
				}
			}
		}

		public List<CompiledQueryPart> getCompiledParts() {
			return compiledParts;
		}
	}

	public static class Builder {

		private QueriesConfig config;
		private ExpressionParser parser = new ExpressionParserImpl();
		private List<QueryCompilerFeature> features = new LinkedList<>();

		public Builder(QueriesConfig config) {
			this.config = config;
		}

		public Builder addFeature(QueryCompilerFeature feature) {
			features.add(feature);
			return this;
		}

		public Builder addDefaultFeatures() {
			features.add(new QueryCompilerIgnoredCommentsFeature());
			features.add(new QueryCompilerHyphenCommentParameterFeature());
			features.add(new QueryCompilerSlashCommentParameterFeature());
			features.add(new QueryCompilerBlockFeature());
			features.add(new QueryCompilerBindedVariableFeature());
			return this;
		}

		public Builder withExpressionParser(ExpressionParser parser) {
			this.parser = Objects.requireNonNull(parser);
			return this;
		}

		public QueryCompiler build() {
			return new QueryCompilerImpl(config, parser, features);
		}
	}
}
