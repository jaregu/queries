package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class QueryBuilderTest {

	private TestBuilder builder;

	@Before
	public void setUp() {
		builder = new TestBuilder();
	}

	@Test
	public void testNoArgsBuild() {
		builder.build();
		assertThatThrownBy(() -> builder.resolver.toNamed()).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testIterableBuild() {
		builder.build((Iterable<?>) Arrays.asList("1", 2, null));
		assertThatThrownBy(() -> builder.resolver.toNamed()).isInstanceOf(QueryBuildException.class);
		testIteratorResolver(builder.resolver.toIterator(), "1", 2, null);
	}

	@Test
	public void testIteratorParametersBuild() {
		builder.build(IteratorResolver.of(Arrays.asList("1", 2, null)));
		assertThatThrownBy(() -> builder.resolver.toNamed()).isInstanceOf(QueryBuildException.class);
		testIteratorResolver(builder.resolver.toIterator(), "1", 2, null);
	}

	@Test
	public void testListBuild() {
		builder.build(Arrays.asList("1", 2, null));
		testIteratorResolver(builder.resolver.toIterator(), "1", 2, null);
		assertThatThrownBy(() -> builder.resolver.toNamed()).isInstanceOf(QueryBuildException.class);

		builder.build(Arrays.asList("1", 2, null));
		testNamedResolver(builder.resolver.toNamed(), "0", "1", "1", 2, "2", null);
		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testNamedResolverBuild() {
		Map<String, Object> params = new HashMap<>();
		params.put("one", 1);
		params.put("two", 22);
		params.put("three", null);
		builder.build(NamedResolver.ofMap(params));

		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(builder.resolver.toNamed(), "one", 1, "two", 22, "three", null);
	}

	@Test
	public void testMapBuild() {
		Map<String, Object> params = new HashMap<>();
		params.put("one", 1);
		params.put("two", 22);
		params.put("three", null);
		builder.build(params);

		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(builder.resolver.toNamed(), "one", 1, "two", 22, "three", null);
	}

	@Test
	public void testMap1KeysBuild() {
		builder.build("one", 1);

		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(builder.resolver.toNamed(), "one", 1);
	}

	@Test
	public void testMap2KeysBuild() {
		builder.build("one", 1, "two", "two");

		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(builder.resolver.toNamed(), "one", 1, "two", "two");
	}

	@Test
	public void testMap3KeysBuild() {
		builder.build("one", 1, "two", "two", "three", null);

		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(builder.resolver.toNamed(), "one", 1, "two", "two", "three", null);
	}

	@Test
	public void testMap4KeysBuild() {
		SomeBean someBean = new SomeBean();
		builder.build("one", 1, "two", "two", "three", null, "four", someBean);

		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(builder.resolver.toNamed(), "one", 1, "two", "two", "three", null, "four", someBean);
	}

	@Test
	public void testObjectBuild() {
		SomeBean someBean = new SomeBean();
		builder.build(someBean);

		testIteratorResolver(builder.resolver.toIterator(), someBean);
		assertThatThrownBy(() -> builder.resolver.toNamed()).isInstanceOf(QueryBuildException.class);

		builder.build(someBean);
		assertThat(builder.resolver.toNamed()).isExactlyInstanceOf(BeanResolver.class);
		BeanResolver beanResolver = (BeanResolver) builder.resolver.toNamed();
		assertThat(beanResolver.getBean()).isSameAs(someBean);
		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testObjectsBuild() {
		builder.build("1", 2, null);

		testIteratorResolver(builder.resolver.toIterator(), "1", 2, null);
		assertThatThrownBy(() -> builder.resolver.toNamed()).isInstanceOf(QueryBuildException.class);

		builder.build("1", 2, null);
		testNamedResolver(builder.resolver.toNamed(), "0", "1", "1", 2, "2", null);
		assertThatThrownBy(() -> builder.resolver.toIterator()).isInstanceOf(QueryBuildException.class);
	}

	private void testIteratorResolver(IteratorResolver resolver, Object... values) {
		assertThat(resolver).containsExactly(values);
	}

	private void testNamedResolver(NamedResolver resolver, Object... keyValue) {
		for (int i = 0; i < keyValue.length; i = i + 2) {
			String key = (String) keyValue[i];
			Object value = keyValue[i + 1];
			assertThat(resolver.getValue(key)).isEqualTo(value);
		}
	}

	public class SomeBean {
	}

	private static class TestBuilder implements QueryBuilder<TestBuilder> {

		private ParametersResolver resolver;

		@Override
		public TestBuilder build(ParametersResolver resolver) {
			this.resolver = resolver;
			return this;
		}
	}
}
