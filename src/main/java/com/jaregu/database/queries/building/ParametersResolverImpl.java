package com.jaregu.database.queries.building;

import java.util.function.Supplier;

final class ParametersResolverImpl implements ParametersResolver {

	private final Object lock = new Object();
	private final Supplier<NamedResolver> namedSupplier;
	private final Supplier<IteratorResolver> iteratorSupplier;

	private boolean resolved = false;
	private NamedResolver namedParameters;
	private IteratorResolver iteratorParameters;

	public ParametersResolverImpl(Supplier<NamedResolver> namedSupplier, Supplier<IteratorResolver> iteratorSupplier) {
		this.namedSupplier = namedSupplier;
		this.iteratorSupplier = iteratorSupplier;
	}

	@Override
	public NamedResolver toNamed() {
		resolveIfNotResolved(() -> namedParameters = namedSupplier.get());
		if (namedParameters == null) {
			throw new QueryBuildException("Parameter resolver was already resolved as iterator resolver!"
					+ " Probably query contains mixed named (:name) and anonymous (?) parameters!");
		}
		return namedParameters;
	}

	@Override
	public IteratorResolver toIterator() {
		resolveIfNotResolved(() -> iteratorParameters = iteratorSupplier.get());
		if (iteratorParameters == null) {
			throw new QueryBuildException("Parameter resolver was already resolved as named resolver!"
					+ " Probably query contains mixed named (:name) and anonymous (?) parameters!");
		}
		return iteratorParameters;
	}

	private void resolveIfNotResolved(Runnable runnable) {
		if (!resolved) {
			synchronized (lock) {
				if (!resolved) {
					runnable.run();
					resolved = true;
				}
			}
		}
	}
}
