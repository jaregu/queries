package com.jaregu.database.queries.parsing;

import java.util.List;

import com.jaregu.database.queries.QueryId;

public interface SourceQuery {

	QueryId getQueryId();

	List<SourceQueryPart> getParts();
}
