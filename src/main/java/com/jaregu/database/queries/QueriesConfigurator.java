package com.jaregu.database.queries;

/**
 * Hook for customising a {@link Queries.Builder} before it is built — used by
 * both the Guice integration ({@code QueriesModule.queriesConfiguratorModule})
 * and the Spring Boot starter (any {@code QueriesConfigurator} bean is applied
 * to the builder).
 *
 * <p>Common uses:
 * <ul>
 *   <li>set a {@code Dialect}</li>
 *   <li>plug in a {@code QueriesCache}</li>
 *   <li>register additional {@code QueryMapperFactory} /
 *       {@code QueryConverterFactory}</li>
 *   <li>swap the {@code ParameterBinder}</li>
 *   <li>register entity classes</li>
 * </ul>
 */
@FunctionalInterface
public interface QueriesConfigurator {

	void configure(Queries.Builder builder);
}
