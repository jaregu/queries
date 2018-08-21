package com.jaregu.database.queries.cache;

public interface Caches<T extends Caches<?>> {

	/**
	 * Sets cache implementation
	 * 
	 * @param cache
	 * @return
	 */
	public T cache(QueriesCache cache);

	default T cacheSimpleMap() {
		return cache(simpleMapCache());
	}

	static QueriesCache noCache() {
		return QueriesCacheNoCache.getInstance();
	}

	static QueriesCache simpleMapCache() {
		return new QueriesMapCache();
	}
}
