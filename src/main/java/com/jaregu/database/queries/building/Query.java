package com.jaregu.database.queries.building;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Dialects;

/**
 * Immutable query class. Contains query SQL and its parameters - all necessary
 * info for {@link PreparedStatement}. Class also contains attributes, which
 * consists of key value map and can be used as additional info for executing
 * layer.
 * <p>
 * 
 * Class contains some utility methods like {@link #map(Function)},
 * {@link #consume(Consumer)}, {@link #stream()} for easier integration in code.
 * <p>
 * 
 * Class also contains built-in mapping methods for creating queries with some
 * additional SQL parts like sorting (ORDER BY clause), paging (LIMIT ? OFFSET ?
 * clause) or total row count (SELECT COUNT(1) ...). For these additional
 * mappings there must be correct {@link Dialect} configured.
 * <p>
 * 
 * Use {@link Queries#builder()} <code>dialect...</code> methods for configuring
 * correct dialect. See {@link Dialects} for all possible built-in values or
 * supply your own implementation.
 *
 */
public interface Query extends ToOrderedQuery, ToPagedQuery, ToCountQuery {

	/**
	 * Returns query SQL
	 * 
	 * @return
	 */
	String getSql();

	/**
	 * Returns query parameters
	 * 
	 * @return
	 */
	List<?> getParameters();

	/**
	 * Returns query attributes, which are set during query build
	 * 
	 * @return
	 */
	Map<String, ?> getAttributes();

	/**
	 * Utility method for mapping query
	 * 
	 * @param mapper
	 * @return
	 */
	default <T> T map(Function<Query, T> mapper) {
		return mapper.apply(this);
	}

	/**
	 * Utility method for using query
	 * 
	 * @param consumer
	 */
	default void consume(Consumer<Query> consumer) {
		consumer.accept(this);
	}

	/**
	 * Utility method which returns stream consisting of one item
	 * 
	 * @return
	 */
	default Stream<Query> stream() {
		return Stream.of(this);
	}
}
