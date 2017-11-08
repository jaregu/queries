package com.jaregu.database.queries.proxy;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.Queries;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.building.ParametersResolver;
import com.jaregu.database.queries.building.Query;
import com.jaregu.database.queries.compiling.PreparedQuery;
import com.jaregu.database.queries.ext.OffsetLimit;
import com.jaregu.database.queries.ext.OrderBy;
import com.jaregu.database.queries.ext.OrderableSearch;
import com.jaregu.database.queries.ext.PageableSearch;

@RunWith(MockitoJUnitRunner.class)
public class QueriesInvocationHandlerTest {

	@Mock
	private Queries queries;

	private SourceId source1Id = SourceId.ofClass(TestBar.class);
	private SourceId source2Id = SourceId.ofId("second.source");
	private SourceId source3Id = SourceId.ofClass(QueriesInvocationHandlerTest.class);

	private QueryId queryId11 = source1Id.getQueryId("1-1");
	private QueryId queryId12 = source1Id.getQueryId("1-2");
	private QueryId queryId21 = source2Id.getQueryId("2-1");
	private QueryId queryId31 = source3Id.getQueryId("3-1");

	@Mock
	private PreparedQuery preparedQuery11;

	@Mock
	private PreparedQuery preparedQuery12;

	@Mock
	private PreparedQuery preparedQuery21;

	@Mock
	private PreparedQuery preparedQuery31;

	@Mock
	private Query query11;

	@Mock
	private Query query12;

	@Mock
	private Query query21;

	@Mock
	private Query query31;

	private Map<Class<? extends Annotation>, QueryMapperFactory> mappers = new HashMap<>();

	private Map<Class<? extends Annotation>, QueryConverterFactory> converters = new HashMap<>();

	@Before
	public void setUp() {
		when(queries.get(queryId11)).thenReturn(preparedQuery11);
		when(queries.get(queryId12)).thenReturn(preparedQuery12);
		when(queries.get(queryId21)).thenReturn(preparedQuery21);
		when(queries.get(queryId31)).thenReturn(preparedQuery31);

		when(preparedQuery11.build((ParametersResolver) any())).thenReturn(query11);
		when(preparedQuery12.build((ParametersResolver) any())).thenReturn(query12);
		when(preparedQuery21.build((ParametersResolver) any())).thenReturn(query21);
		when(preparedQuery31.build((ParametersResolver) any())).thenReturn(query31);

		when(query11.toCountQuery()).thenReturn(query11);
		when(query11.toPagedQuery((PageableSearch) any())).thenReturn(query11);
		when(query11.toOrderedQuery((OrderableSearch) any())).thenReturn(query11);

		when(query12.toPagedQuery((PageableSearch) any())).thenReturn(query12);
		when(query12.toOrderedQuery((OrderableSearch) any())).thenReturn(query12);

		// when(query12.toCountQuery()).thenReturn(query12);
		// when(query12.toPagedQuery((PageableSearch)
		// any())).thenReturn(query12);
		// when(query12.toOrderedQuery((SortableSearch)
		// any())).thenReturn(query12);

		// when(query21.toCountQuery()).thenReturn(query21);
		// when(query21.toPagedQuery((PageableSearch)
		// any())).thenReturn(query21);
		// when(query21.toOrderedQuery((SortableSearch)
		// any())).thenReturn(query21);

		mappers.put(CustomRegistered.class, new MapperFactory("REGISTERED_CustomRegistered_"));
		mappers.put(CustomWithoutMarkerRegistered.class,
				new MapperFactory("REGISTERED_CustomWithoutMarkerRegistered_"));

		converters.put(ConverterCustomRegistered.class, new ConverterFactory("REGISTERED_ConverterCustomRegistered_"));
		converters.put(ConverterCustomWithoutMarkerRegistered.class,
				new ConverterFactory("REGISTERED_ConverterCustomWithoutMarkerRegistered_"));
	}

	private <T> T createProxy(Class<T> classOfInterface) {
		InvocationHandler handler = new QueriesInvocationHandler(classOfInterface, queries, mappers, converters);

		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(classOfInterface.getClassLoader(), new Class<?>[] { classOfInterface },
				handler);
		return proxy;
	}

	@Test
	public void testClassCastException() {
		TestBar testBar = createProxy(TestBar.class);
		assertThatThrownBy(() -> testBar.getClassCastException()).isInstanceOf(ClassCastException.class);
	}

	@Test
	public void testNoArgsCalls() {
		TestBar testBar = createProxy(TestBar.class);
		testBar.getQuery11NoArgs();
		testBar.getQuery11NoArgs();
		Query result = testBar.getQuery11NoArgs();

		verify(queries, times(3)).get(queryId11);
		// verify(preparedQuery11, times(3)).build();
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery11, times(3)).build(capturedResolver.capture());

		assertThat(result).isSameAs(query11);
		assertThatThrownBy(() -> capturedResolver.getValue().toIterator())
				.hasMessageContaining("Empty variable resolver");
		assertThatThrownBy(() -> capturedResolver.getValue().toNamed()).hasMessageContaining("Empty variable resolver");
	}

	@Test
	public void testListParam() {
		TestBar testBar = createProxy(TestBar.class);
		testBar.getQuery11ListParam(Arrays.asList("1"));
		testBar.getQuery11ListParam(Arrays.asList("1", "2"));
		Query result = testBar.getQuery11ListParam(Arrays.asList("1", "2", "3"));

		verify(queries, times(3)).get(queryId11);
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery11, times(3)).build(capturedResolver.capture());

		assertThat(result).isSameAs(this.query11);
		assertThat(capturedResolver.getAllValues().get(0).toIterator()).containsExactly("1");
		assertThat(capturedResolver.getAllValues().get(1).toIterator()).containsExactly("1", "2");
		assertThat(capturedResolver.getAllValues().get(2).toIterator()).containsExactly("1", "2", "3");
	}

	@Test
	public void testMapParam() {
		TestBar testBar = createProxy(TestBar.class);
		Map<String, Integer> map = new HashMap<>();
		map.put("1", 11);
		map.put("2", 22);
		map.put("3", 33);
		Query query11 = testBar.getQuery11MapParam(map);

		verify(queries, times(1)).get(queryId11);
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery11, times(1)).build(capturedResolver.capture());

		assertThat(query11).isSameAs(this.query11);
		assertThat(capturedResolver.getValue().toNamed().getValue("1")).isEqualTo(11);
		assertThat(capturedResolver.getValue().toNamed().getValue("2")).isEqualTo(22);
		assertThat(capturedResolver.getValue().toNamed().getValue("3")).isEqualTo(33);
	}

	@Test
	public void testBeanParam() {
		TestBar testBar = createProxy(TestBar.class);
		Params params = new Params("BBB");
		params.aaa = "AAA";
		when(preparedQuery11.build((ParametersResolver) any())).thenReturn(query11);
		Query query11 = testBar.getQuery11BeanParam(params);

		verify(queries, times(1)).get(queryId11);
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery11, times(1)).build(capturedResolver.capture());

		assertThat(query11).isSameAs(this.query11);
		assertThat(capturedResolver.getValue().toNamed().getValue("aaa")).isEqualTo("AAA");
		assertThat(capturedResolver.getValue().toNamed().getValue("bbb")).isEqualTo("BBB");
	}

	@Test
	public void testMultipleParams() {
		TestBar testBar = createProxy(TestBar.class);
		Query query11 = testBar.getQuery11MultipleParams(Arrays.asList("1", "2"), Collections.singletonMap("aaa", 222),
				"333");

		verify(queries, times(1)).get(queryId11);
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery11, times(1)).build(capturedResolver.capture());

		assertThat(query11).isSameAs(this.query11);
		assertThat(capturedResolver.getValue().toIterator()).containsExactly(Arrays.asList("1", "2"),
				Collections.singletonMap("aaa", 222), "333");
	}

	@Test
	public void testProxyMapParams() {
		TestBar testBar = createProxy(TestBar.class);

		Query query12 = testBar.getQuery12MapParams("aaa", 222);
		verify(queries, times(1)).get(queryId12);
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery12, times(1)).build(capturedResolver.capture());

		assertThat(query12).isSameAs(this.query12);
		assertThat(capturedResolver.getValue().toNamed().getValue("strKey")).isEqualTo("aaa");
		assertThat(capturedResolver.getValue().toNamed().getValue("intKey")).isEqualTo(222);
	}

	@Test
	public void testOtherSource() {
		TestBar testBar = createProxy(TestBar.class);

		Query result = testBar.getOtherSourceQuery("key1-value", null);
		verify(queries, times(1)).get(queryId21);
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery21, times(1)).build(capturedResolver.capture());

		assertThat(result).isSameAs(query21);
		assertThat(capturedResolver.getValue().toNamed().getValue("key1")).isEqualTo("key1-value");
		assertThat(capturedResolver.getValue().toNamed().getValue("key2")).isNull();
	}

	@Test
	public void testThirdSource() {
		TestBar testBar = createProxy(TestBar.class);

		Query result = testBar.getThirdSourceQuery();
		verify(queries, times(1)).get(queryId31);
		ArgumentCaptor<ParametersResolver> capturedResolver = ArgumentCaptor.forClass(ParametersResolver.class);
		verify(preparedQuery31, times(1)).build(capturedResolver.capture());

		assertThat(result).isSameAs(query31);
	}

	@Test
	public void testConversionErrors() {
		TestBar testBar = createProxy(TestBar.class);

		assertThatThrownBy(() -> testBar.getToPageableError()).isInstanceOf(QueryProxyException.class);
		assertThatThrownBy(() -> testBar.getToSortableError()).isInstanceOf(QueryProxyException.class);
		assertThatThrownBy(() -> testBar.getToPageableAndSortableError(null)).isInstanceOf(QueryProxyException.class);
		assertThatThrownBy(() -> testBar.getToAllError(null)).isInstanceOf(QueryProxyException.class);
	}

	@Test
	public void testToCount() {
		TestBar testBar = createProxy(TestBar.class);
		testBar.getToCount();
		verify(query11, times(1)).toCountQuery();
	}

	@Test
	public void testToCountWithOptionalParams() {
		TestBar testBar = createProxy(TestBar.class);
		testBar.getToCountWithOptionalParams(new Search(), "bbb");
		verify(query11, times(1)).toCountQuery();
	}

	@Test
	public void testToPaged() {
		TestBar testBar = createProxy(TestBar.class);
		Search search = new Search();
		testBar.getToPageable(search);
		verify(query11, times(1)).toPagedQuery(search);
	}

	@Test
	public void testToSortable() {
		TestBar testBar = createProxy(TestBar.class);
		Search search = new Search();
		testBar.getToSortable(search);
		verify(query11, times(1)).toOrderedQuery(search);
	}

	@Test
	public void testToSortableAndPageable() {
		TestBar testBar = createProxy(TestBar.class);
		Search search = new Search();
		testBar.getToPageableAndSortable(search);
		InOrder inOrder = inOrder(query11);
		inOrder.verify(query11).toOrderedQuery(search);
		inOrder.verify(query11).toPagedQuery(search);
	}

	@Test
	public void testToAll() {
		TestBar testBar = createProxy(TestBar.class);
		Search search = new Search();
		testBar.getToAll(search);
		InOrder inOrder = inOrder(query11);
		inOrder.verify(query11).toOrderedQuery(search);
		inOrder.verify(query11).toPagedQuery(search);
		inOrder.verify(query11).toCountQuery();
	}

	@Test
	public void testStaticMapperCastError() {
		TestBar testBar = createProxy(TestBar.class);
		assertThatThrownBy(() -> testBar.getWithMapperCastError("a", "b")).isInstanceOf(ClassCastException.class);
	}

	@Test
	public void testStaticMapper() {
		TestBar testBar = createProxy(TestBar.class);
		String result = testBar.getWithMapper("aa", "bbb");
		assertThat(result).isEqualTo("aabbb");
	}

	@Test
	public void testStaticMapperWithConversions() {
		TestBar testBar = createProxy(TestBar.class);
		Search search = new Search();
		String result = testBar.getToPageableAndSortableWithMapper(search);
		InOrder inOrder = inOrder(query12);
		inOrder.verify(query12).toOrderedQuery(search);
		inOrder.verify(query12).toPagedQuery(search);
		assertThat(result).isEqualTo("SEARCH");
	}

	@Test
	public void testMapping_CustomRegistered() {
		TestBar testBar = createProxy(TestBar.class);
		assertThat(testBar.getCustomRegistered()).isEqualTo("REGISTERED_CustomRegistered_CustomRegistered");
	}

	@Test
	public void testMapping_CustomWithoutMarkerRegistered() {
		TestBar testBar = createProxy(TestBar.class);
		assertThat(testBar.getCustomWithoutMarkerRegistered())
				.isEqualTo("REGISTERED_CustomWithoutMarkerRegistered_CustomWithoutMarkerRegistered");
	}

	@Test
	public void testMapping_CustomWithStaticFactory() {
		TestBar testBar = createProxy(TestBar.class);
		assertThat(testBar.getCustomWithStaticFactory()).isEqualTo("NOT_REGISTERED_CustomWithStaticFactory");
	}

	@Test
	public void testMapping_CustomNotRegisteredError() {
		TestBar testBar = createProxy(TestBar.class);
		assertThatThrownBy(() -> testBar.getCustomNotRegisteredError()).hasMessageContaining("not registered")
				.isInstanceOf(QueryProxyException.class);
	}

	@Test
	public void testMapping_ConverterCustomRegistered() {
		TestBar testBar = createProxy(TestBar.class);
		assertThat(testBar.getConverterCustomRegistered().getSql())
				.isEqualTo("REGISTERED_ConverterCustomRegistered_ConverterCustomRegistered");
	}

	@Test
	public void testMapping_ConverterCustomWithoutMarkerRegistered() {
		TestBar testBar = createProxy(TestBar.class);
		assertThat(testBar.getConverterCustomWithoutMarkerRegistered().getSql())
				.isEqualTo("REGISTERED_ConverterCustomWithoutMarkerRegistered_ConverterCustomWithoutMarkerRegistered");
	}

	@Test
	public void testMapping_ConverterCustomWithStaticFactory() {
		TestBar testBar = createProxy(TestBar.class);
		assertThat(testBar.getConverterCustomWithStaticFactory().getSql())
				.isEqualTo("NOT_REGISTERED_ConverterCustomWithStaticFactory");
	}

	@Test
	public void testMapping_ConverterCustomNotRegisteredError() {
		TestBar testBar = createProxy(TestBar.class);
		assertThatThrownBy(() -> testBar.getConverterCustomNotRegisteredError()).hasMessageContaining("not registered")
				.isInstanceOf(QueryProxyException.class);
	}

	public static class Search implements OrderableSearch, PageableSearch {

		private OffsetLimit offsetLimit;
		private OrderBy sortBy;

		@Override
		public OffsetLimit getOffsetLimit() {
			return offsetLimit;
		}

		@Override
		public void setOffsetLimit(OffsetLimit offsetLimit) {
			this.offsetLimit = offsetLimit;
		}

		@Override
		public OrderBy getOrderBy() {
			return sortBy;
		}

		@Override
		public void setOrderBy(OrderBy properties) {
			this.sortBy = properties;
		}

		@Override
		public String toString() {
			return "SEARCH";
		}
	}

	@QueriesSourceClass
	public interface TestBar {

		@QueryRef("1-1")
		public String getClassCastException();

		@QueryRef("1-1")
		public Query getQuery11NoArgs();

		@QueryRef("1-1")
		public Query getQuery11ListParam(List<String> someStrings);

		@QueryRef("1-1")
		public Query getQuery11MapParam(Map<String, Integer> someIntegerMap);

		@QueryRef("1-1")
		public Query getQuery11BeanParam(Params params);

		@QueryRef("1-1")
		public Query getQuery11MultipleParams(List<String> someStrings, Map<String, Integer> someIntegerMap,
				String thirdParam);

		@QueryRef("1-2")
		public Query getQuery12MapParams(@QueryParam("strKey") String strValue, @QueryParam("intKey") Integer intValue);

		@QueriesSourceId("second.source")
		@QueryRef("2-1")
		public Query getOtherSourceQuery(@QueryParam("key1") String key1Value, @QueryParam("key2") Integer key2Value);

		@QueryRef(value = "1-1", toPaged = true)
		public Query getToPageableError();

		@QueryRef(value = "1-2", toSorted = true)
		public Query getToSortableError();

		@QueriesSourceId("second.source")
		@QueryRef(value = "2-1", toPaged = true, toSorted = true)
		public Query getToPageableAndSortableError(OrderableSearch search);

		@QueryRef(value = "1-1", toSorted = true, toPaged = true, toCount = true)
		public Query getToAllError(OrderableSearch search);

		@QueryRef(value = "1-1", toPaged = true)
		public Query getToPageable(Search search);

		@QueryRef(value = "1-1", toSorted = true)
		public Query getToSortable(Search search);

		@QueryRef(value = "1-1", toCount = true)
		public Query getToCount();

		@QueryRef(value = "1-1", toCount = true)
		public Query getToCountWithOptionalParams(Search search, String aaa);

		@QueryRef(value = "1-1", toSorted = true, toPaged = true)
		public Query getToPageableAndSortable(Search search);

		@QueryRef(value = "1-1", toSorted = true, toPaged = true, toCount = true)
		public Query getToAll(Search search);

		@QueryRef("1-2")
		@ClassQueryMapper(StaticArgsToConcatenatedString.class)
		public Long getWithMapperCastError(@QueryParam("strKey1") String strValue1,
				@QueryParam("strKey2") String strValue2);

		@QueryRef(value = "1-2")
		@ClassQueryMapper(StaticArgsToConcatenatedString.class)
		public String getWithMapper(@QueryParam("strKey1") String strValue1, @QueryParam("strKey2") String strValue2);

		@QueryRef(value = "1-2", toSorted = true, toPaged = true)
		@ClassQueryMapper(StaticArgsToConcatenatedString.class)
		public String getToPageableAndSortableWithMapper(Search search);

		@QueriesSourceClass(QueriesInvocationHandlerTest.class)
		@QueryRef("3-1")
		public Query getThirdSourceQuery();

		@QueryRef("1-1")
		@CustomRegistered
		public String getCustomRegistered();

		@QueryRef("1-2")
		@CustomWithoutMarkerRegistered
		public String getCustomWithoutMarkerRegistered();

		@QueriesSourceId("second.source")
		@QueryRef("2-1")
		@CustomWithStaticFactory
		public String getCustomWithStaticFactory();

		@QueriesSourceClass(QueriesInvocationHandlerTest.class)
		@QueryRef("3-1")
		@CustomNotRegisteredError
		public String getCustomNotRegisteredError();

		@QueryRef("1-1")
		@ConverterCustomRegistered
		public Query getConverterCustomRegistered();

		@QueryRef("1-2")
		@ConverterCustomWithoutMarkerRegistered
		public Query getConverterCustomWithoutMarkerRegistered();

		@QueriesSourceId("second.source")
		@QueryRef("2-1")
		@ConverterCustomWithStaticFactory
		public Query getConverterCustomWithStaticFactory();

		@QueriesSourceClass(QueriesInvocationHandlerTest.class)
		@QueryRef("3-1")
		@ConverterCustomNotRegisteredError
		public Query getConverterCustomNotRegisteredError();
	}

	public class Params {

		public String aaa;
		private final String bbb;

		public Params(String bbb) {
			this.bbb = bbb;
		}

		public String getBbb() {
			return bbb;
		}
	}

	public static class StaticArgsToConcatenatedString implements QueryMapper<String> {

		@Override
		public String map(Query q, Object[] args) {
			StringBuilder sb = new StringBuilder();
			for (Object object : args) {
				sb.append(object);
			}
			return sb.toString();
		}
	}

	public static class MapperFactory implements QueryMapperFactory {

		private String prefix;

		public MapperFactory() {
			this("NOT_REGISTERED_");
		}

		public MapperFactory(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public QueryMapper<?> get(Annotation annotation) {
			return (query, args) -> prefix + annotation.annotationType().getSimpleName();
		}

	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	@Mapper
	public @interface CustomRegistered {
	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	public @interface CustomWithoutMarkerRegistered {
	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	@Mapper(MapperFactory.class)
	public @interface CustomWithStaticFactory {
	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	@Mapper
	public @interface CustomNotRegisteredError {
	}

	public static class ConverterFactory implements QueryConverterFactory {

		private String prefix;

		public ConverterFactory() {
			this("NOT_REGISTERED_");
		}

		public ConverterFactory(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public QueryConverter get(Annotation annotation) {
			Query queryMock = mock(Query.class);
			when(queryMock.getSql()).thenReturn(prefix + annotation.annotationType().getSimpleName());
			return (query, args) -> queryMock;
		}

	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	@Converter
	public @interface ConverterCustomRegistered {
	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	public @interface ConverterCustomWithoutMarkerRegistered {
	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	@Converter(ConverterFactory.class)
	public @interface ConverterCustomWithStaticFactory {
	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	@Converter
	public @interface ConverterCustomNotRegisteredError {
	}
}
