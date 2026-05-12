package com.jaregu.database.queries.springboot;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

import com.jaregu.database.queries.Queries;

/**
 * {@link FactoryBean} that produces a {@code Queries.proxy(daoInterface)}
 * instance for a given interface — used by {@link QueriesScanRegistrar} to
 * register each discovered DAO as an injectable Spring bean.
 *
 * <p>The {@code Queries} dependency is resolved lazily via {@link BeanFactory}
 * to avoid cycles during context bootstrap.
 *
 * @param <T> the proxied DAO interface type
 */
public class QueriesProxyFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

	private final Class<T> proxyInterface;
	private BeanFactory beanFactory;

	public QueriesProxyFactoryBean(Class<T> proxyInterface) {
		this.proxyInterface = proxyInterface;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public T getObject() {
		return beanFactory.getBean(Queries.class).proxy(proxyInterface);
	}

	@Override
	public Class<?> getObjectType() {
		return proxyInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
