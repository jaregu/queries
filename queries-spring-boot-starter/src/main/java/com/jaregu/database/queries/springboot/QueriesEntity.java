package com.jaregu.database.queries.springboot;

import java.util.Objects;

import com.jaregu.database.queries.annotation.Table;

/**
 * Spring-side wrapper for a {@code (entityClass, alias)} pair consumed by
 * {@link QueriesAutoConfiguration} when building the {@code Queries} bean.
 *
 * <p>The {@link QueriesAutoConfiguration#queries Queries} bean calls
 * {@code builder.entity(entityClass, alias)} for every {@code QueriesEntity}
 * bean in the context, mirroring the Guice
 * {@code QueriesModule.entityModule(Class[, alias])} surface.
 *
 * <p>{@link QueriesScan} discovers {@link Table}-annotated classes in the
 * scanned packages and registers a {@code QueriesEntity} bean for each
 * automatically. Users can also declare them explicitly:
 *
 * <pre>{@code
 * @Bean
 * QueriesEntity jobEntity() {
 *     return QueriesEntity.of(Job.class);
 * }
 * }</pre>
 *
 * <p>Or with a custom SQL-side alias:
 *
 * <pre>{@code
 * @Bean
 * QueriesEntity jobEntity() {
 *     return QueriesEntity.of(Job.class, "j");
 * }
 * }</pre>
 *
 * @param entityClass the class registered with the {@code Queries.Builder}
 * @param alias       the alias used inside SQL {@code entityFieldGenerator(...)}
 *                    macros (defaults to the simple class name when omitted)
 */
public record QueriesEntity(Class<?> entityClass, String alias) {

	public QueriesEntity {
		Objects.requireNonNull(entityClass, "entityClass");
		Objects.requireNonNull(alias, "alias");
	}

	/**
	 * Registers the entity with its simple class name as the alias — same
	 * default as Guice's {@code QueriesModule.entityModule(Class)}.
	 */
	public static QueriesEntity of(Class<?> entityClass) {
		return new QueriesEntity(entityClass, entityClass.getSimpleName());
	}

	/**
	 * Registers the entity with an explicit alias used in SQL macros.
	 */
	public static QueriesEntity of(Class<?> entityClass, String alias) {
		return new QueriesEntity(entityClass, alias);
	}
}
