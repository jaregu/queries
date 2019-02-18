package com.jaregu.database.queries.ext.dalesbred;

import java.lang.reflect.Method;

import javax.inject.Provider;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.dalesbred.Database;
import org.dalesbred.transaction.TransactionCallback;
import org.dalesbred.transaction.TransactionContext;
import org.dalesbred.transaction.TransactionSettings;

public final class TransactionalMethodInterceptor implements MethodInterceptor {

	private final Provider<Database> databaseProvider;

	public TransactionalMethodInterceptor(Provider<Database> databaseProvider) {
		this.databaseProvider = databaseProvider;
	}

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		try {
			TransactionSettings settings = getTransactionSettings(invocation);
			return databaseProvider.get().withTransaction(settings, new TransactionCallback<Object>() {
				@Override
				public Object execute(TransactionContext tx) {
					try {
						return invocation.proceed();
					} catch (Throwable e) {
						throw new WrappedException(e);
					}
				}
			});
		} catch (WrappedException e) {
			throw e.getCause();
		}
	}

	private static TransactionSettings getTransactionSettings(MethodInvocation invocation) {
		Transactional tx = findTransactionDefinition(invocation.getMethod());
		if (tx != null)
			return fromAnnotation(tx);
		else
			return new TransactionSettings();
	}

	private static Transactional findTransactionDefinition(Method method) {
		Transactional tx = method.getAnnotation(Transactional.class);
		return (tx != null) ? tx : method.getDeclaringClass().getAnnotation(Transactional.class);
	}

	private static TransactionSettings fromAnnotation(Transactional transactional) {
		TransactionSettings settings = new TransactionSettings();
		settings.setIsolation(transactional.isolation());
		settings.setPropagation(transactional.propagation());
		return settings;
	}

	private static class WrappedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		WrappedException(Throwable e) {
			super(e);
		}
	}
}