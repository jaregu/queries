package com.jaregu.database.queries;

import java.util.Map;

import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.building.ParamsResolver;
import com.jaregu.database.queries.parsing.QueriesSources;

public interface Queries extends QueriesParams {

	Query get(QueryId queryId);

	Query get(QueryId queryId, Object params);

	Query get(QueryId queryId, Map<String, Object> params);

	Query get(QueryId queryId, ParamsResolver resolver);

	static QueriesImpl.Builder newBuilder() {
		return new QueriesImpl.Builder();
	}

	static Queries forSources(QueriesSources sources) {
		return new QueriesImpl.Builder().setSources(sources).build();
	}
}