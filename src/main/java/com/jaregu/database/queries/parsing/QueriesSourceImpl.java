package com.jaregu.database.queries.parsing;

import java.util.function.Supplier;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;

public class QueriesSourceImpl implements QueriesSource {

	private SourceId sourceId;
	private Supplier<String> contentSupplier;

	public QueriesSourceImpl(SourceId sourceId, Supplier<String> contentSupplier) {
		this.sourceId = sourceId;
		this.contentSupplier = contentSupplier;
	}

	public SourceId getId() {
		return sourceId;
	}

	@Override
	public String getContent() {
		return contentSupplier.get();
	}

	@Override
	public QueryId getQueryId(String id) {
		return sourceId.getQueryId(id);
	}
}
