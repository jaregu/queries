package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.parsing.ParsedQueryPart;

public interface QueryCompilerFeature {

	boolean isCompilable(Source source);

	Result compile(Source source, Compiler chain);

	@FunctionalInterface
	public interface Compiler {

		Result compile(Source source);
	}

	@FunctionalInterface
	public interface Source {

		List<ParsedQueryPart> getParts();

		public static Source of(List<ParsedQueryPart> parts) {
			return new ImmutableSource(parts);
		}
	}

	@FunctionalInterface
	public interface Result {

		List<PreparedQueryPart> getParts();
	}

	static class ImmutableSource implements Source {

		final private List<ParsedQueryPart> parts;

		public ImmutableSource(List<ParsedQueryPart> parts) {
			this.parts = Collections.unmodifiableList(parts);
		}

		@Override
		public List<ParsedQueryPart> getParts() {
			return parts;
		}
	}
}
