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
 *       {@code @QueriesSourceClass} interfaces (registered as proxy beans +
 *       their {@code QueriesSource}) and {@code @Table} entities (registered
 *       as {@link com.jaregu.database.queries.springboot.QueriesEntity}
 *       beans).</li>
 *   <li>{@link com.jaregu.database.queries.QueriesConfigurator} — the same
 *       functional interface used by the Guice integration; any
 *       {@code QueriesConfigurator} bean is applied to the builder before
 *       {@code build()}.</li>
 * </ul>
 */
package com.jaregu.database.queries.springboot;
