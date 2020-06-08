package com.jaregu.database.queries.ext.dalesbred;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.inject.Singleton;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
@Singleton
public class DalesbredModuleConfiguration {

	@Singular
	@NonNull
	private List<ConversionRegistration<?, ?>> conversions = Collections.emptyList();

	@Value
	@Builder
	public static class ConversionRegistration<D, J> {

		@NonNull
		private Class<D> databaseType;

		@NonNull
		private Class<J> javaType;

		@NonNull
		private Function<D, J> fromDatabase;

		@NonNull
		private Function<J, D> toDatabase;
	}
}
