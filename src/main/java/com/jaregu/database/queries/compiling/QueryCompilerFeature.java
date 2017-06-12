package com.jaregu.database.queries.compiling;

import java.util.Collections;
import java.util.List;

import com.jaregu.database.queries.parsing.SourceQueryPart;

public interface QueryCompilerFeature {

	boolean isCompilable(Source source);

	Result compile(Source source, Compiler chain);

	@FunctionalInterface
	public interface Compiler {

		Result compile(Source source);
	}

	@FunctionalInterface
	public interface Source {

		List<SourceQueryPart> getParts();

		public static Source of(List<SourceQueryPart> parts) {
			return new ImmutableSource(parts);
		}
	}

	@FunctionalInterface
	public interface Result {

		List<CompiledQueryPart> getCompiledParts();
	}

	static class ImmutableSource implements Source {

		final private List<SourceQueryPart> parts;

		public ImmutableSource(List<SourceQueryPart> parts) {
			this.parts = Collections.unmodifiableList(parts);
		}

		@Override
		public List<SourceQueryPart> getParts() {
			return parts;
		}
	}
}
