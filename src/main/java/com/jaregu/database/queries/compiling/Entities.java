package com.jaregu.database.queries.compiling;

public interface Entities<T extends Entities<?>> {

	/**
	 * Registers DAO entity to be used with {@link EntityFieldsFeature} as
	 * entity class name. Register entity and use inside SQL entities simple
	 * class name, so changing package of entity will be easier.
	 * <p>
	 * 
	 * @param entity
	 *            - entity class
	 */
	T entity(Class<?> entity);

	/**
	 * Registers DAO entity to be used with {@link EntityFieldsFeature} as
	 * entity class name. Register entity and use inside SQL alias name, so
	 * changing package of entity will be easier.
	 * <p>
	 * 
	 * @param entity
	 *            - entity class
	 * @param alias
	 *            - alias to be used inside SQL use name without whitespace
	 */
	T entity(Class<?> entity, String alias);
}
