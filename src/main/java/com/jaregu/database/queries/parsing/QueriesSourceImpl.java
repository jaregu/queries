package com.jaregu.database.queries.parsing;

import java.util.function.Function;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;

public class QueriesSourceImpl implements QueriesSource {

	private SourceId sourceId;
	private Function<QueriesConfig, String> contentSupplier;

	public QueriesSourceImpl(SourceId sourceId, Function<QueriesConfig, String> contentSupplier) {
		this.sourceId = sourceId;
		this.contentSupplier = contentSupplier;
	}

	public SourceId getId() {
		return sourceId;
	}

	@Override
	public String readContent(QueriesConfig config) {
		return contentSupplier.apply(config);
	}

	@Override
	public QueryId getQueryId(String id) {
		return sourceId.getQueryId(id);
	}
}
