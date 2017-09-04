package com.jaregu.database.queries;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

public interface QueryId {

	SourceId getSourceId();

	String getId();

	static QueryId of(SourceId sourceId, String id) {
		return new QueryIdImpl(sourceId, id);
	}

	static QueryId of(String id) {
		if (id.indexOf(".") < 0) {
			throw new IllegalArgumentException(
					"String token expected like source.query, where source is sourceId and query is query identification: "
							+ id);
		}
		int splitIndex = id.lastIndexOf(".");
		return new QueryIdImpl(SourceId.ofId(id.substring(0, splitIndex)), id.substring(splitIndex + 1));
	}

	static final class QueryIdImpl implements QueryId {

		final private SourceId sourceId;
		final private String id;

		private QueryIdImpl(SourceId sourceId, String id) {
			this.sourceId = requireNonNull(sourceId);
			this.id = requireNonNull(id).trim();
			if (this.id.length() == 0) {
				throw new IllegalArgumentException("ID is empty string!");
			}
		}

		@Override
		public SourceId getSourceId() {
			return sourceId;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String toString() {
			return "QueryId[" + sourceId.getId() + "." + id + "]";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			if (!(o instanceof QueryId)) {
				return false;
			}
			QueryIdImpl queryId = (QueryIdImpl) o;
			return Objects.equals(this.sourceId, queryId.sourceId) && Objects.equals(this.id, queryId.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(sourceId, id);
		}
	}
}
