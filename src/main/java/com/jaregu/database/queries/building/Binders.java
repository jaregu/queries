package com.jaregu.database.queries.building;

import java.util.List;

/**
 * Collection of build-in parameter binders. See
 *
 * @param <T>
 */
public interface Binders<T extends Binders<?>> {

	/**
	 * Sets parameter binder. Parameter binder is called for every binded
	 * parameter place in query. If not supplied will use
	 * {@link #defaultBinder()} when building queries.
	 * <p>
	 * 
	 * Usually parameter binder can be used to create some <code>IN</code>
	 * clause support. For example if there was a query like
	 * 
	 * <pre>
	 * SELECT * FROM dummy WHERE id IN (?)
	 * </pre>
	 * 
	 * then with correct binder implementation resulting query could be
	 * something like this:
	 * 
	 * <pre>
	 * SELECT * FROM dummy WHERE id IN (?,?,?)
	 * </pre>
	 * 
	 * By default will use {@link #binderDefault()}, for more options see
	 * {@link ParameterBinderWithCollectionSupport} or use one of shorthand
	 * methods: {@link #binderForCollectionsAndLastValueNull(List)} or
	 * {@link #binderForCollectionsAndLastValueRepeated(List)}
	 * <p>
	 * 
	 * @param binder
	 * @return
	 */
	T binder(ParameterBinder binder);

	/**
	 * See {@link ParameterBinderWithCollectionSupport}
	 * <p>
	 * 
	 * Shorthand for
	 * 
	 * <pre>
	 * binder(ParameterBinderWithCollectionSupport.builder().forCollections().fillRestWithLastValue()
	 * 		.teplateSizes(teplateSizes).build())
	 * </pre>
	 * 
	 * @param templateSizes
	 *            - list of binded variable count sizes to choose from when
	 *            building SQL, usually something like 1, 5, 50, 100, ...
	 * @return
	 */
	default T binderForCollectionsAndLastValueRepeated(List<Integer> templateSizes) {
		return binder(ParameterBinderWithCollectionSupport.builder().forCollections().fillRestWithLastValue()
				.templateSizes(templateSizes).build());
	}

	/**
	 * See {@link ParameterBinderWithCollectionSupport}
	 * <p>
	 * 
	 * Shorthand for
	 * 
	 * <pre>
	 * binder(ParameterBinderWithCollectionSupport.builder().forCollections().fillRestWithNullValue()
	 * 		.teplateSizes(teplateSizes).build())
	 * </pre>
	 * 
	 * @param templateSizes
	 *            - list of binded variable count sizes to choose from when
	 *            building SQL, usually something like 1, 5, 50, 100, ...
	 * @return
	 */
	default T binderForCollectionsAndLastValueNull(List<Integer> templateSizes) {
		return binder(ParameterBinderWithCollectionSupport.builder().forCollections().fillRestWithNullValue()
				.templateSizes(templateSizes).build());
	}

	/**
	 * Default parameter binder, returns single question mark, no additional
	 * logic.
	 * 
	 * @return
	 */
	static ParameterBinder defaultBinder() {
		return new ParameterBinderDefaultImpl();
	}
}
