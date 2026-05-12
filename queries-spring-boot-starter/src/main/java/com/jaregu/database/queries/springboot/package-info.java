/**
 * Spring Boot auto-configuration for the Jaregu Queries library.
 *
 * <p>Pulling in {@code com.jaregu:queries-spring-boot-starter} on a Spring
 * Boot application gives you, out of the box:
 *
 * <ul>
 *   <li>{@link com.jaregu.database.queries.springboot.QueriesAutoConfiguration}
 *       — wires a {@code JdbcClient} (on top of the auto-configured
 *       {@code DataSource}) and a {@code Queries} bean with the four
 *       Spring-backed {@code QueryMapperFactory} implementations
 *       installed.</li>
 *   <li>{@link com.jaregu.database.queries.springboot.QueriesScan} — class-level
 *       annotation that scans configured packages for
 *       {@code @QueriesSourceClass} interfaces and registers each one as an
 *       injectable proxy bean plus its underlying
 *       {@code QueriesSource}.</li>
 *   <li>{@link com.jaregu.database.queries.springboot.QueriesCustomizer} —
 *       functional bean hook for tuning the {@code Queries.Builder} (dialect,
 *       cache, custom mappers, etc.).</li>
 * </ul>
 */
package com.jaregu.database.queries.springboot;
