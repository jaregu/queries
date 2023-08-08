package com.jaregu.database.queries.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
import com.jaregu.database.queries.ext.OrderableSearch;
import com.jaregu.database.queries.ext.PageableSearch;

public final class QueriesInvocationHandler implements InvocationHandler {

	private static final QueryMapper<?> IDENTITY_MAPPER = (query, args) -> query;

	private final Class<?> classOfInterface;
	private final Queries queries;
	private final Map<Class<? extends Annotation>, QueryMapperFactory> mappers;
	private final Map<Class<? extends Annotation>, QueryConverterFactory> converters;

	private final SourceId rootSourceId;

	private Map<Method, Function<Object[], ?>> initializedMethods = new ConcurrentHashMap<>();

	public <T> QueriesInvocationHandler(Class<T> classOfInterface, Queries queries,
			Map<Class<? extends Annotation>, QueryMapperFactory> mappers,
			Map<Class<? extends Annotation>, QueryConverterFactory> converters) {
		this.classOfInterface = classOfInterface;
		this.queries = queries;
		this.mappers = mappers;
		this.converters = converters;

		SourceId rootSourceId = getSourceId(classOfInterface);
		this.rootSourceId = rootSourceId == null ? SourceId.ofClass(classOfInterface) : rootSourceId;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return initializedMethods.computeIfAbsent(method, this::createBuildReference).apply(args);
	}

	private Function<Object[], ?> createBuildReference(Method method) {
		QueryId queryId = getQueryId(method);
		Function<Object[], ParametersResolver> paramsToResolver = getResolver(method);
		QueryConverter converter = getMergedConverter(method);
		QueryMapper<?> mapper = getMergedMapper(method).orElse(IDENTITY_MAPPER);
		return args -> mapper.map(converter.convert(queries.get(queryId).build(paramsToResolver.apply(args)), args),
				args);
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
				SourceIdProducer sourceIdProducer = sourceIdProviderAnnotation.value().getDeclaredConstructor()
						.newInstance();
				sourceId = sourceIdProducer.get(element, sourceAnnotation.get());
			} catch (Exception e) {
				throw new QueryProxyException("Problem with sourceId aquiring: can't find suitable constructor " + e,
						e);
			}
		}
		return sourceId;
	}

	private QueryConverter getMergedConverter(Method method) {
		List<QueryConverter> rootConverters = getConverters(classOfInterface);
		List<QueryConverter> refConverters = getQueryRefConverters(method);
		List<QueryConverter> annoConverters = getConverters(method);

		List<QueryConverter> converters = new ArrayList<>(
				rootConverters.size() + refConverters.size() + annoConverters.size());
		converters.addAll(rootConverters);
		converters.addAll(refConverters);
		converters.addAll(annoConverters);

		return (query, args) -> {
			Query result = query;
			for (QueryConverter converter : converters) {
				result = converter.convert(result, args);
			}
			return result;
		};
	}

	private List<QueryConverter> getConverters(AnnotatedElement element) {
		List<QueryConverter> annotatedConverters = new ArrayList<>(2);
		List<Annotation> converterAnnotations = Stream.of(element.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(Converter.class)
						|| converters.containsKey(a.annotationType()))
				.collect(Collectors.toList());
		if (!converterAnnotations.isEmpty()) {
			for (Annotation converterAnnotation : converterAnnotations) {
				QueryConverterFactory factory;
				if (converters.containsKey(converterAnnotation.annotationType())) {
					factory = converters.get(converterAnnotation.annotationType());
				} else {
					Converter converter = converterAnnotation.annotationType().getAnnotation(Converter.class);
					Class<? extends QueryConverterFactory> converterFactoryClass = converter.value();
					if (Converter.DEFAULT.class.isAssignableFrom(converterFactoryClass)) {
						throw new QueryProxyException("Factory is not registered for annotation "
								+ converterAnnotation.annotationType().getName() + "! "
								+ "Set static converter using @Converter annotation value or use Queries.Builder.converter() method to register factory instance!");
					}
					try {
						factory = converterFactoryClass.getDeclaredConstructor().newInstance();
					} catch (Exception e) {
						throw new QueryProxyException(
								"Problem instantiating query converter factory class with no argument constructor " + e,
								e);
					}
				}
				annotatedConverters.add(factory.get(converterAnnotation));
			}
		}
		return annotatedConverters;
	}

	private List<QueryConverter> getQueryRefConverters(Method method) {
		QueryRef queryRef = getQueryRef(method);
		List<QueryConverter> converters = new ArrayList<>(3);
		if (queryRef.toSorted()) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1 || !OrderableSearch.class.isAssignableFrom(parameterTypes[0]))
				throw new QueryProxyException(
						"If used toSorted then " + method.getDeclaringClass().getName() + " method " + method.getName()
								+ " must have exactly one parameter and it must implement SortableSearch");

			converters.add((query, args) -> {
				return query.toOrderedQuery((OrderableSearch<?>) args[0]);
			});
		}
		if (queryRef.toPaged()) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1 || !PageableSearch.class.isAssignableFrom(parameterTypes[0]))
				throw new QueryProxyException("If used toPaged then interface " + method.getDeclaringClass().getName()
						+ " method " + method.getName()
						+ " must have exactly one parameter and it must implement PageableSearch");
			converters.add((query, args) -> {
				return query.toPagedQuery((PageableSearch<?>) args[0]);
			});
		}
		if (queryRef.toCount()) {
			converters.add((query, args) -> {
				return query.toCountQuery();
			});
		}
		return converters;
	}

	private Optional<QueryMapper<?>> getMergedMapper(Method method) {
		Optional<QueryMapper<?>> mapper = getMapper(method);
		if (mapper != null) {
			return mapper;
		} else {
			return getMapper(classOfInterface);
		}
	}

	private Optional<QueryMapper<?>> getMapper(AnnotatedElement element) {
		Optional<Annotation> optionalAnnotation = Stream.of(element.getAnnotations()).filter(
				a -> a.annotationType().isAnnotationPresent(Mapper.class) || mappers.containsKey(a.annotationType()))
				.findFirst();
		if (optionalAnnotation.isPresent()) {
			Annotation mapperAnnotation = optionalAnnotation.get();
			QueryMapperFactory factory;
			if (mappers.containsKey(mapperAnnotation.annotationType())) {
				factory = mappers.get(mapperAnnotation.annotationType());
			} else {
				Mapper mapper = mapperAnnotation.annotationType().getAnnotation(Mapper.class);
				Class<? extends QueryMapperFactory> mapperFactoryClass = mapper.value();
				if (Mapper.DEFAULT.class.isAssignableFrom(mapperFactoryClass)) {
					throw new QueryProxyException("Factory is not registered for annotation "
							+ mapperAnnotation.annotationType().getName() + "! "
							+ "Set static factory using @Mapper annotation value or use Queries.Builder.mapper() method to register factory instance!");
				}
				try {
					factory = mapperFactoryClass.getDeclaredConstructor().newInstance();
				} catch (Exception e) {
					throw new QueryProxyException(
							"Problem instantiating query mapper factory class with no argument constructor " + e, e);
				}
			}
			return Optional.of(factory.get(mapperAnnotation));
		}
		return Optional.empty();
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
					Map<String, Object> parameters = new LinkedHashMap<>(parameterTypes.length);
					for (int i = 0; i < parameterTypes.length; i++) {
						String name = parameterNames[i];
						Object value = args[i];
						parameters.put(name, value);
					}
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