package com.jaregu.database.queries.parsing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;

public class SourceQueriesImpl implements SourceQueries {

	private SourceId sourceId;
	private List<SourceQuery> sources;
	private Map<QueryId, SourceQuery> queries;

	public SourceQueriesImpl(SourceId sourceId, List<SourceQuery> sources) {
		this.sourceId = sourceId;
		this.sources = Collections.unmodifiableList(sources);
		this.queries = sources.stream().collect(Collectors.toMap(SourceQuery::getQueryId, Function.identity()));
	}

	@Override
	public SourceId getSourceId() {
		return sourceId;
	}

	@Override
	public Iterator<SourceQuery> iterator() {
		return sources.iterator();
	}

	@Override
	public SourceQuery get(QueryId id) {
		SourceQuery query;
		if ((query = queries.get(id)) == null) {
			throw new QueriesParseException("Uknown query Id: " + id);
		}
		return query;
	}

	@Override
	public List<SourceQuery> getQueries() {
		return sources;
	}

	@Override
	public String toString() {
		return "SourceQueries" + sources;
	}
}
