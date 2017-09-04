package com.jaregu.database.queries;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

public interface SourceId {

	String getId();

	QueryId getQueryId(String id);

	static SourceId ofClass(Class<?> clazz) {
		return new SourceIdImpl(clazz.getName());
	}

	static SourceId ofId(String id) {
		return new SourceIdImpl(id);
	}

	static SourceId ofResource(String resource) {
		String id = resource.trim();
		id = (id.indexOf('.') > 0 ? id.substring(0, id.lastIndexOf('.')) : id).replace('\\', '.').replace('/', '.');
		while (id.startsWith(".")) {
			id = id.substring(1);
		}
		while (id.endsWith(".")) {
			id = id.substring(0, id.length() - 1);
		}
		return new SourceIdImpl(id);
	}

	static final class SourceIdImpl implements SourceId {

		private final String id;

		public SourceIdImpl(String id) {
			this.id = requireNonNull(id).trim();
			if (this.id.length() == 0) {
				throw new IllegalArgumentException("ID is empty!");
			}
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String toString() {
			return "QueriesSourceId{" + id + "}";
		}

		@Override
		public QueryId getQueryId(String id) {
			return QueryId.of(this, id);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			if (!(o instanceof SourceId)) {
				return false;
			}
			SourceIdImpl sourceId = (SourceIdImpl) o;
			return Objects.equals(this.id, sourceId.getId());
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}
