package com.jaregu.database.queries.building;

import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Pageable;
import com.jaregu.database.queries.ext.OffsetLimit;

public interface ToPagedQuery {

	/**
	 * Returns new query which has added <code>LIMIT, OFFSET</code>
	 * functionality using configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toPagedQuery(Query, OffsetLimit)}
	 * 
	 * @return
	 */
	Query toPagedQuery(Pageable pageable);

	/**
	 * Returns new query which has added <code>LIMIT, OFFSET</code>
	 * functionality using configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toPagedQuery(Query, OffsetLimit)}
	 * 
	 * @return
	 */
	default Query toPagedQuery(Integer offset, Integer limit) {
		return toPagedQuery(OffsetLimit.of(offset, limit));
	}
}
