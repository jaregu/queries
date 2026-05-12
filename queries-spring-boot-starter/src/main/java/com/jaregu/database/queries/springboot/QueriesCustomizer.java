package com.jaregu.database.queries.springboot;

import com.jaregu.database.queries.Queries;

/**
 * Bean-based hook for tuning the {@link Queries.Builder} that the
 * {@link QueriesAutoConfiguration} uses to build the {@code Queries} bean.
 *
 * <p>Define one or more {@code QueriesCustomizer} beans in your Spring context
 * to:
 * <ul>
 *   <li>set a {@code Dialect} ({@code builder.dialect(...)})</li>
 *   <li>plug in a {@code QueriesCache}</li>
 *   <li>register additional {@code QueryMapperFactory} or
 *       {@code QueryConverterFactory}</li>
 *   <li>swap the {@code ParameterBinder}</li>
 *   <li>register entity classes</li>
 * </ul>
 *
 * <p>Customizers are applied <i>after</i> the auto-configuration installs the
 * default Spring-backed mappers, so user customizers can override them.
 * Multiple customizers run in {@code @Order} sequence.
 */
@FunctionalInterface
public interface QueriesCustomizer {

	void customize(Queries.Builder builder);
}
