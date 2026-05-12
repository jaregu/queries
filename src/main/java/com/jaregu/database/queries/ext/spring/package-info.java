/**
 * Spring-backed execution layer for the Jaregu Queries library.
 *
 * <p>Mirrors the contract of {@code com.jaregu.database.queries.ext.dalesbred}
 * but routes execution through Spring's {@code JdbcClient} instead of
 * Dalesbred. The four {@link com.jaregu.database.queries.proxy.QueryMapperFactory}
 * implementations register against the same {@code @ExecuteUpdate},
 * {@code @FindAll}, {@code @FindOptional} and {@code @FindUnique} annotations
 * defined in the dalesbred package, so user interface declarations are
 * portable between the two extensions.
 *
 * <p>This package depends on {@code spring-jdbc} at compile time but ships as
 * {@code compileOnly} — users only pay for Spring on their classpath if they
 * opt into this extension.
 *
 * <p>Use {@link com.jaregu.database.queries.ext.spring.SpringQueriesConfigurer}
 * to register all four factories on a {@code Queries.Builder} in plain Spring
 * setups. Spring Boot users should pull in the
 * {@code queries-spring-boot-starter} artifact which auto-wires this for them.
 */
package com.jaregu.database.queries.ext.spring;
