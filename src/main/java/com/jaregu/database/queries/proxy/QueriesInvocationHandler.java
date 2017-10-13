package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.ext.PageableSearch;
import com.jaregu.database.queries.ext.SortableSearch;

public final class QueriesInvocationHandler implements InvocationHandler {

	private static final QueryMapper<?> IDENTITY_MAPPER = (query, args) -> query;

	private final SourceId rootSourceId;
	private final QueryMapper<?> rootMapper;
	private final Queries queries;
	private final Map<Class<? extends Annotation>, QueryMapperFactory> factories;

	private Map<Method, Function<Object[], ?>> initializedMethods = new ConcurrentHashMap<>();

	public <T> QueriesInvocationHandler(Class<T> classOfInterface, Queries queries,
			Map<Class<? extends Annotation>, QueryMapperFactory> factories) {
		this.factories = factories;
		SourceId rootSourceId = getSourceId(classOfInterface);
		this.rootSourceId = rootSourceId == null ? SourceId.ofClass(classOfInterface) : rootSourceId;
		this.rootMapper = getMapper(classOfInterface);
		this.queries = queries;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return initializedMethods.computeIfAbsent(method, this::createBuildReference).apply(args);
	}

	private Function<Object[], ?> createBuildReference(Method method) {
		QueryId queryId = getQueryId(method);
		Function<Object[], ParametersResolver> paramsToResolver = getResolver(method);
		QueryMapper<Query> converter = getConverter(method);
		QueryMapper<?> mapper = getMergedMapper(method);
		return args -> mapper.map(converter.map(queries.get(queryId).build(paramsToResolver.apply(args)), args), args);
	}

	private QueryId getQueryId(Method method) {
		SourceId sourceId = getMergedSourceId(method);
		QueryRef queryRef = getQueryRef(method);
		return sourceId.getQueryId(queryRef.value());
	}

	private SourceId getMergedSourceId(Method method) {
		SourceId sourceId = getSourceId(method);
		if (sourceId == null)
			sourceId = rootSourceId;
		if (sourceId == null)
			throw new QueryProxyException(
					"Problem with sourceId aquiring: can't find any suitable annotation like QueriesSourceClass, QueriesSourceResource or QueriesSourceId on class "
							+ method.getDeclaringClass().getName() + " method " + method.getName());
		return sourceId;
	}

	private QueryRef getQueryRef(Method method) {
		QueryRef queryRef = method.getAnnotation(QueryRef.class);
		if (queryRef == null)
			throw new QueryProxyException("Problem with queryId aquiring: can't find QueryRef annnotation on class ("
					+ method.getDeclaringClass().getName() + ") method " + method.getName());
		return queryRef;
	}

	private SourceId getSourceId(AnnotatedElement element) {
		SourceId sourceId = null;
		Optional<Annotation> sourceAnnotation = Stream.of(element.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(RegisteredSourceIdProducer.class)).findFirst();

		if (sourceAnnotation.isPresent()) {
			RegisteredSourceIdProducer sourceIdProviderAnnotation = sourceAnnotation.get().annotationType()
					.getAnnotation(RegisteredSourceIdProducer.class);
			try {
				SourceIdProducer sourceIdProducer = sourceIdProviderAnnotation.value().newInstance();
				sourceId = sourceIdProducer.get(element, sourceAnnotation.get());
			} catch (Exception e) {
				throw new QueryProxyException("Problem with sourceId aquiring: can't find suitable constructor " + e,
						e);
			}
		}
		return sourceId;
	}

	private QueryMapper<Query> getConverter(Method method) {
		QueryMapper<Query> converter = (query, args) -> query;
		QueryRef queryRef = getQueryRef(method);
		if (queryRef.toSorted() || queryRef.toPaged() || queryRef.toCount()) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (queryRef.toSorted()) {
				if (parameterTypes.length != 1 || !SortableSearch.class.isAssignableFrom(parameterTypes[0]))
					throw new QueryProxyException("If used toSorted then " + method.getDeclaringClass().getName()
							+ " method " + method.getName()
							+ " must have exactly one parameter and it must implement SortableSearch");

				QueryMapper<Query> before = converter;
				converter = (query, args) -> {
					Query result = before.map(query, args);
					return result.toOrderedQuery((SortableSearch) args[0]);
				};
			}

			if (queryRef.toPaged()) {
				if (parameterTypes.length != 1 || !PageableSearch.class.isAssignableFrom(parameterTypes[0]))
					throw new QueryProxyException("If used toPaged then interface "
							+ method.getDeclaringClass().getName() + " method " + method.getName()
							+ " must have exactly one parameter and it must implement PageableSearch");

				QueryMapper<Query> before = converter;
				converter = (query, args) -> {
					Query result = before.map(query, args);
					return result.toPagedQuery((PageableSearch) args[0]);
				};
			}

			if (queryRef.toCount()) {
				QueryMapper<Query> before = converter;
				converter = (query, args) -> {
					Query result = before.map(query, args);
					return result.toCountQuery();
				};
			}
		}
		return converter;
	}

	private QueryMapper<?> getMergedMapper(Method method) {
		QueryMapper<?> mapper = getMapper(method);
		if (mapper != null) {
			return mapper;
		} else {
			return rootMapper;
		}
	}

	private QueryMapper<?> getMapper(AnnotatedElement element) {
		Optional<Annotation> optionalAnnotation = Stream.of(element.getAnnotations()).filter(
				a -> a.annotationType().isAnnotationPresent(Mapper.class) || factories.containsKey(a.annotationType()))
				.findFirst();
		if (optionalAnnotation.isPresent()) {
			Annotation mapperAnnotation = optionalAnnotation.get();
			QueryMapperFactory factory;
			if (factories.containsKey(mapperAnnotation.annotationType())) {
				factory = factories.get(mapperAnnotation.annotationType());
			} else {
				Mapper mapper = mapperAnnotation.annotationType().getAnnotation(Mapper.class);
				Class<? extends QueryMapperFactory> mapperFactoryClass = mapper.value();
				if (Mapper.DEFAULT.class.isAssignableFrom(mapperFactoryClass)) {
					throw new QueryProxyException("Factory is not registered for annotation "
							+ mapperAnnotation.annotationType().getName() + "! "
							+ "Set static factory using @Mapper annotation value or use Queries.Builder.factory() method to register factory instance!");
				}
				try {
					factory = mapperFactoryClass.newInstance();
				} catch (Exception e) {
					throw new QueryProxyException(
							"Problem instantiating query mapper factory class with no argument constructor " + e, e);
				}
			}
			return factory.get(mapperAnnotation);
		}
		return IDENTITY_MAPPER;
	}

	@SuppressWarnings("unchecked")
	private Function<Object[], ParametersResolver> getResolver(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();

		if (parameterTypes.length == 0) {
			return (args) -> ParametersResolver.empty();
		} else {
			boolean isAnyMapParam = Stream.of(parameterAnnotations).anyMatch(at -> Stream.of(at).anyMatch(a -> {
				return a instanceof QueryParam;
			}));
			if (isAnyMapParam) {
				boolean allIsMapParams = Stream.of(parameterAnnotations).allMatch(at -> Stream.of(at).anyMatch(a -> {
					return a instanceof QueryParam;
				}));
				if (!allIsMapParams) {
					throw new QueryProxyException("QueryParam annotation if used is required on all arguments, check "
							+ method.getDeclaringClass().getName() + " class " + method.getName() + " method");
				}

				String[] parameterNames = IntStream.range(0, parameterTypes.length)
						.mapToObj(i -> Stream.of(parameterAnnotations[i]).filter(a -> {
							return a instanceof QueryParam;
						}).findFirst().map(a -> (QueryParam) a).get().value()).toArray(String[]::new);

				return args -> {
					Map<String, Object> parameters = IntStream.range(0, parameterTypes.length)
							.mapToObj(i -> new SimpleEntry<String, Object>(parameterNames[i], args[i]))
							.collect(Collectors.toMap(se -> se.getKey(), se -> se.getValue()));
					return ParametersResolver.ofMap(parameters);
				};
			} else if (parameterTypes.length == 1) {
				Class<?> parameterType = parameterTypes[0];
				if (List.class.isAssignableFrom(parameterType)) {
					return args -> ParametersResolver.ofList((List<?>) args[0]);
				} else if (Map.class.isAssignableFrom(parameterType)) {
					return args -> ParametersResolver.ofMap((Map<String, ?>) args[0]);
				} else {
					return args -> ParametersResolver.ofObject(args[0]);
				}

			} else {
				return args -> ParametersResolver.ofList(Arrays.asList(args));
			}
		}
	}
}