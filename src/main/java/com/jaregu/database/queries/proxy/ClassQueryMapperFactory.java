package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;

class ClassQueryMapperFactory implements QueryMapperFactory {

	public ClassQueryMapperFactory() {
	}

	@Override
	public QueryMapper<?> get(Annotation annotation) {
		ClassQueryMapper classMapper = (ClassQueryMapper) annotation;
		QueryMapper<?> queryMapper;
		try {
			queryMapper = classMapper.value().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new QueryProxyException("Problem instantiating query mapper class with no argument constructor " + e,
					e);
		}
		return (query, args) -> queryMapper.map(query, args);
	}
}
