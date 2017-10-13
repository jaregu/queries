package com.jaregu.database.queries.building;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Parameter binder which supports each collection element binding as stand
 * alone parameter and for each parameter there is ? created in SQL.
 * <p>
 * 
 * Template sizes has to be defined, so result SQL will not create too much
 * unique statements and binded parameter count will be one of defined.
 * <p>
 * 
 * Binder will throw an error if collection size will exceed largest template
 * size and usually there is some kind of limit for SQL server too. If
 * collection is too big, use some other technique like temporary table.
 * <p>
 * 
 * It is possible to supply <i>mapper</i> function for parameter unwrapping or
 * use default build-in "for all collections". <code>Null</code> value is
 * considered as single parameter and is processed as one, so to have some other
 * behavior (error perhaps) use custom <i>mapper</i> function, where passed
 * collection parameters can be wrapped in some special class.
 * <p>
 * 
 * Use static {@link #builder()} for creating binder instances.
 * 
 * @param templateSizes
 *            -
 * @param restParamType
 *            - what value to use for empty parameters
 * @return
 */
public class ParameterBinderWithCollectionSupport extends ParameterBinderDefaultImpl {

	private static final Function<Collection<?>, ?> LAST_VALUE = parameters -> {
		if (parameters instanceof List) {
			List<?> list = (List<?>) parameters;
			if (list.isEmpty()) {
				throw new QueryBuildException(
						"Can't get last value of collection for binder with collection support! Collection is empty. Use some conditional requirements to add this SQL clause!");
			}
			return list.get(list.size() - 1);
		} else {
			Iterator<?> iterator = parameters.iterator();
			if (!iterator.hasNext()) {
				throw new QueryBuildException(
						"Can't get last value of collection for binder with collection support! Collection is empty. Use some conditional requirements to add this SQL clause!");
			}
			while (true) {
				Object current = iterator.next();
				if (!iterator.hasNext()) {
					return current;
				}
			}
		}
	};

	private static final Function<Collection<?>, ?> NULL_VALUE = parameters -> null;

	private static final Function<Object, Collection<?>> COLLECTION_MAPPER = parameter -> {
		if (parameter instanceof Collection<?>) {
			return (Collection<?>) parameter;
		} else {
			return null;
		}
	};

	public static Builder builder() {
		return new Builder();
	}

	public static Function<Collection<?>, ?> lastValueFiller() {
		return LAST_VALUE;
	}

	public static Function<Collection<?>, ?> nullValueFiller() {
		return NULL_VALUE;
	}

	public static Function<Object, Collection<?>> collectionMapper() {
		return COLLECTION_MAPPER;
	}

	public static class Builder {

		private Set<Integer> templateSizes;
		private Function<Object, Collection<?>> collectionFunction;
		private Function<Collection<?>, ?> fillerFunction;

		/**
		 * Mandatory set of binded variable count sizes to choose from when
		 * building SQL, usually something like 1, 5, 50, 100, ...
		 * 
		 */
		public Builder templateSizes(Set<Integer> templateSizes) {
			this.templateSizes = templateSizes;
			return this;
		}

		/**
		 * Mandatory set of binded variable count sizes to choose from when
		 * building SQL, usually something like 1, 5, 50, 100, ...
		 * 
		 */
		public Builder templateSizes(List<Integer> templateSizes) {
			return templateSizes(new LinkedHashSet<>(templateSizes));
		}

		/**
		 * Function which returns collection of parameters to be used with this
		 * parameter binder or <strong>null</strong> to fall back to default
		 * implementation See {@link Binders#defaultBinder()}
		 * <p>
		 * 
		 * Use {@link #forCollections()} to use this binder for all collection
		 * types or use this method to provide custom <i>mapper</i> function for
		 * some unwrapping
		 * 
		 * @return
		 */
		public Builder collectionFunction(Function<Object, Collection<?>> collectionFunction) {
			this.collectionFunction = collectionFunction;
			return this;
		}

		/**
		 * Process all parameters when condition
		 * <code>parameter instanceOf Collection</code> is true
		 */
		public Builder forCollections() {
			this.collectionFunction = COLLECTION_MAPPER;
			return this;
		}

		/**
		 * When collection size is less then placeholder count will use this
		 * function once to obtain value for all not yet filled bind parameters.
		 * <p>
		 * 
		 * Use this method to supply custom function or use one of build-in
		 * {@link #fillRestWithLastValue()}, {@link #fillRestWithNullValue()} or
		 * {@link #fillRestWithValue(Object)}
		 * 
		 * @param fillerFunction
		 * @return
		 */
		public Builder fillRestWith(Function<Collection<?>, ?> fillerFunction) {
			this.fillerFunction = fillerFunction;
			return this;
		}

		/**
		 * Parameters will be filled with last value from collection, for
		 * example if passed in collection was [1, 2, 3] and used template size
		 * was 5, then created SQL will be ?,?,?,?,? and parameter list [1, 2,
		 * 3, 3, 3], default option
		 */
		public Builder fillRestWithLastValue() {
			this.fillerFunction = LAST_VALUE;
			return this;
		}

		/**
		 * Parameters will be filled with null value, for example if passed in
		 * collection was [1, 2, 3] and used template size was 5, then created
		 * SQL will be ?,?,?,?,? and parameter list [1, 2, 3, null, null]
		 */
		public Builder fillRestWithNullValue() {
			this.fillerFunction = NULL_VALUE;
			return this;
		}

		/**
		 * Parameters will be filled with null value, for example if passed in
		 * collection was [1, 2, 3] and used template size was 5, then created
		 * SQL will be ?,?,?,?,? and parameter list [1, 2, 3, <i>value</i>,
		 * <i>value</i>]
		 */
		public Builder fillRestWithValue(Object value) {
			this.fillerFunction = parameters -> value;
			return this;
		}

		/**
		 * Build ParameterBinderWithCollectionSupport instance. Template sizes
		 * must be supplied! By default will use binder for all collection type
		 * parameters (see {@link #forCollections()}) and rest parameters in
		 * template will be filled with last collection value (see
		 * {@link #fillRestWithLastValue()})
		 * 
		 */
		public ParameterBinderWithCollectionSupport build() {
			if (templateSizes == null) {
				throw new IllegalArgumentException("Please set mandatory templateSizes parameter!");
			}
			return new ParameterBinderWithCollectionSupport(templateSizes,
					collectionFunction == null ? COLLECTION_MAPPER : collectionFunction,
					fillerFunction == null ? LAST_VALUE : fillerFunction);
		}
	}

	private final NavigableSet<Integer> templateSizes;
	private final Function<Object, Collection<?>> collectionFunction;
	private final Function<Collection<?>, ?> fillerFunction;

	ParameterBinderWithCollectionSupport(Set<Integer> templateSizes, Function<Object, Collection<?>> collectionFunction,
			Function<Collection<?>, ?> fillerFunction) {

		this.templateSizes = Collections.unmodifiableNavigableSet(requireNonNull(templateSizes, "templateSizes is null")
				.stream().filter(s -> s != null && s > 0).collect(Collectors.toCollection(TreeSet::new)));
		this.collectionFunction = requireNonNull(collectionFunction);
		this.fillerFunction = requireNonNull(fillerFunction);

		if (this.templateSizes.isEmpty()) {
			throw new IllegalArgumentException("Template sizes has to contain at least one positive integer!");
		}
	}

	protected Set<Integer> getTemplateSizes() {
		return templateSizes;
	}

	protected Function<Object, Collection<?>> getCollectionFunction() {
		return collectionFunction;
	}

	protected Function<Collection<?>, ?> getFillerFunction() {
		return fillerFunction;
	}

	@Override
	public Result process(Object parameter) {
		Collection<?> collection = collectionFunction.apply(parameter);
		if (collection != null) {
			Integer places = templateSizes.ceiling(collection.size());
			if (places == null) {
				throw new QueryBuildException("Can't bind collection as repeated ?,?,?... parameters!"
						+ "\nPassed collecton has " + collection.size()
						+ " element(s) when maximum configured parameter count is: " + templateSizes.last());
			}
			return new ResultImpl(collection, places, fillerFunction);
		} else {
			return super.process(parameter);
		}
	}

	private final static class ResultImpl implements Result {

		private static final String FIRST = "?";
		private static final String REST = ",?";

		private final Collection<?> values;
		private final int places;
		private final Function<Collection<?>, ?> fillerFunction;

		public ResultImpl(Collection<?> values, int places, Function<Collection<?>, ?> fillerFunction) {
			this.values = values;
			this.places = places;
			this.fillerFunction = fillerFunction;
		}

		@Override
		public String getSql() {
			StringBuilder sql = new StringBuilder(FIRST);
			if (places > 1) {
				for (int i = 1; i < places; i++) {
					sql.append(REST);
				}
			}
			return sql.toString();
		}

		@Override
		public List<Object> getParameters() {
			List<Object> parameters = new ArrayList<>(places);
			parameters.addAll(values);
			if (places > parameters.size()) {
				Object fillerValue = fillerFunction.apply(values);
				for (int i = parameters.size(); i < places; i++) {
					parameters.add(fillerValue);
				}
			}
			return parameters;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "ParameterBinderWithCollectionSupport.Result{" + places + "}" + values + "";
		}
	}
}
