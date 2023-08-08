package com.jaregu.database.queries.ext.dalesbred;

import java.util.List;
import java.util.function.Function;

import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
@Singleton
public class DalesbredModuleConfiguration {

	@NonNull
	@Singular
	private List<ConversionRegistration<?, ?>> conversions;

	private String aaa;

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
