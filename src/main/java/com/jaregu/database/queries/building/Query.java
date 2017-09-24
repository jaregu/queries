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
import com.jaregu.database.queries.ext.OffsetLimit;
import com.jaregu.database.queries.ext.PageableSearch;
import com.jaregu.database.queries.ext.SortProperties;
import com.jaregu.database.queries.ext.SortableSearch;

/**
 * Immutable query class. Contains query SQL and its parameters - all necessary
 * info for {@link PreparedStatement}. Attributes, which consists of key value
 * map can be used as additional info for executing layer.
 * <p>
 * 
 * Class contains some utility methods like {@link #map(Function)},
 * {@link #consume(Consumer)}, {@link #stream()} for easier integration in code.
 * <p>
 * 
 * Class also contains built-in convert methods for creating queries with some
 * additional features like sorting (ORDER BY clause), paging support (LIMIT ?
 * OFFSET ? clause) or total row count query (SELECT COUNT() ...). For these
 * additional conversations there must be correct {@link Dialect} configured.
 * <p>
 * 
 * Use {@link Queries#builder()} <code>dialect...</code> methods for configuring
 * correct dialect. See {@link Dialects} for all possible built-in values or
 * supply your own implementation.
 *
 */
public interface Query {

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
	<T> T map(Function<Query, T> mapper);

	/**
	 * Utility method for using query
	 * 
	 * @param consumer
	 */
	void consume(Consumer<Query> consumer);

	/**
	 * Utility method which returns stream consisting of one item
	 * 
	 * @return
	 */
	Stream<Query> stream();

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * configured ({@link Dialect}) implementation.
	 * 
	 * See {@link Dialect#toSortedQuery(Query, SortProperties)}
	 * 
	 * @return
	 */
	Query toSortedQuery(String... sortPorperties);

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toSortedQuery(Query, SortProperties)}
	 * 
	 * @return
	 */
	Query toSortedQuery(Iterable<String> sortPorperties);

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toSortedQuery(Query, SortProperties)}
	 * 
	 * @return
	 */
	Query toSortedQuery(SortProperties sortProperties);

	/**
	 * Returns new query which has added <code>ORDER BY</code> clause using
	 * configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toSortedQuery(Query, SortProperties)}
	 * 
	 * @return
	 */
	Query toSortedQuery(SortableSearch sortableSearch);

	/**
	 * Returns new query which has added <code>LIMIT, OFFSET</code>
	 * functionality using configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toPagedQuery(Query, OffsetLimit)}
	 * 
	 * @return
	 */
	Query toPagedQuery(PageableSearch pageableSearch);

	/**
	 * Returns new query which has added <code>LIMIT, OFFSET</code>
	 * functionality using configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toPagedQuery(Query, OffsetLimit)}
	 * 
	 * @return
	 */
	Query toPagedQuery(OffsetLimit offsetLimit);

	/**
	 * Returns new query which has added <code>LIMIT, OFFSET</code>
	 * functionality using configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toPagedQuery(Query, OffsetLimit)}
	 * 
	 * @return
	 */
	Query toPagedQuery(int offset, int limit);

	/**
	 * Returns new query which has original query wrapped as sub-query inside
	 * <code>COUNT</code> query using configured ({@link Dialect})
	 * implementation
	 * 
	 * See {@link Dialect#toCountQuery(Query)}
	 * 
	 * @return
	 */
	Query toCountQuery();
}
