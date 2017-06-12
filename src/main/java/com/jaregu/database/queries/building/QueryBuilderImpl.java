package com.jaregu.database.queries.building;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaregu.database.queries.compiling.CompiledQuery;
import com.jaregu.database.queries.compiling.CompiledQueryPart;

public class QueryBuilderImpl implements QueryBuilder {

	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Query build(CompiledQuery compiledQuery, ParamsResolver resolver) {
		Builder builder = new Builder(compiledQuery, resolver);
		builder.build();
		return new QueryImpl(compiledQuery.getQueryId(), builder.getQuery(), builder.getParameters());
	}

	private class Builder {

		private CompiledQuery compiledQuery;
		private ParamsResolver variableResolver;
		private StringBuilder query;
		private List<Object> parameters;

		public Builder(CompiledQuery compiledQuery, ParamsResolver variableResolver) {
			this.compiledQuery = compiledQuery;
			this.variableResolver = variableResolver;
		}

		public void build() {
			query = new StringBuilder();
			parameters = new LinkedList<>();
			for (CompiledQueryPart part : compiledQuery.getParts()) {
				part.eval(variableResolver, (sql, parameters) -> {
					query.append(sql);
					this.parameters.addAll(parameters);
				});
			}
			log.debug("Query: {} parameters: {}", query.toString(), parameters);
		}

		public String getQuery() {
			return query.toString();
		}

		public List<Object> getParameters() {
			return parameters;
		}
	}
}
