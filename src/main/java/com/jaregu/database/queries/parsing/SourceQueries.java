package com.jaregu.database.queries.parsing;

import java.util.List;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;

public interface SourceQueries extends Iterable<SourceQuery> {

	SourceId getSourceId();

	SourceQuery get(QueryId id);

	List<SourceQuery> getQueries();
}
