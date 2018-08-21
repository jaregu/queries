package com.jaregu.database.queries.building;

import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Pageable;

public interface ToPagedQuery {

	/**
	 * Returns new query which has added <code>LIMIT, OFFSET</code>
	 * functionality using configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toPagedQuery(Query, Pageable)}
	 * 
	 * @return
	 */
	Query toPagedQuery(Pageable pageable);

	/**
	 * Returns new query which has added <code>LIMIT, OFFSET</code>
	 * functionality using configured ({@link Dialect}) implementation
	 * 
	 * See {@link Dialect#toPagedQuery(Query, Pageable)}
	 * 
	 * @return
	 */
	default Query toPagedQuery(Integer offset, Integer limit) {
		return toPagedQuery(new Pageable() {

			@Override
			public Integer getOffset() {
				return offset;
			}

			@Override
			public Integer getLimit() {
				return limit;
			}
		});
	}
}
