package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ParametersResolverTest {

	@Test
	public void testStaticEmpty() {
		ParametersResolver empty = ParametersResolver.empty();
		assertThatThrownBy(() -> empty.toNamed()).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> empty.toIterator()).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testStaticOfIterable() {
		ParametersResolver iterableAsIterator = ParametersResolver.ofIterable(Arrays.asList("1", 2, null));
		assertThatThrownBy(() -> iterableAsIterator.toNamed()).isInstanceOf(QueryBuildException.class);
		testIteratorResolver(iterableAsIterator.toIterator(), "1", 2, null);
	}

	@Test
	public void testStaticOfIteratorParameters() {
		ParametersResolver iterableAsIterator = ParametersResolver
				.ofIteratorParameters(IteratorResolver.of(Arrays.asList("1", 2, null)));
		assertThatThrownBy(() -> iterableAsIterator.toNamed()).isInstanceOf(QueryBuildException.class);
		testIteratorResolver(iterableAsIterator.toIterator(), "1", 2, null);
	}

	@Test
	public void testStaticOfList() {
		ParametersResolver iterableAsIterator = ParametersResolver.ofList(Arrays.asList("1", 2, null));
		testIteratorResolver(iterableAsIterator.toIterator(), "1", 2, null);
		assertThatThrownBy(() -> iterableAsIterator.toNamed()).isInstanceOf(QueryBuildException.class);

		ParametersResolver iterableAsNamed = ParametersResolver.ofList(Arrays.asList("1", 2, null));
		testNamedResolver(iterableAsNamed.toNamed(), "0", "1", "1", 2, "2", null);
		assertThatThrownBy(() -> iterableAsNamed.toIterator()).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testStaticOfMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("one", 1);
		params.put("two", 22);
		params.put("three", null);

		ParametersResolver mapResolver = ParametersResolver.ofMap(params);

		assertThatThrownBy(() -> mapResolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(mapResolver.toNamed(), "one", 1, "two", 22, "three", null);
	}

	@Test
	public void testStaticOfNamedResolver() {
		Map<String, Object> params = new HashMap<>();
		params.put("one", 1);
		params.put("two", 22);
		params.put("three", null);

		ParametersResolver namedResolver = ParametersResolver.ofNamedParameters(NamedResolver.ofMap(params));

		assertThatThrownBy(() -> namedResolver.toIterator()).isInstanceOf(QueryBuildException.class);
		testNamedResolver(namedResolver.toNamed(), "one", 1, "two", 22, "three", null);
	}

	@Test
	public void testStaticOfObject() {
		SomeBean someBean = new SomeBean();
		ParametersResolver beanAsIterable = ParametersResolver.ofObject(someBean);
		testIteratorResolver(beanAsIterable.toIterator(), someBean);
		assertThatThrownBy(() -> beanAsIterable.toNamed()).isInstanceOf(QueryBuildException.class);

		ParametersResolver beanAsNamed = ParametersResolver.ofObject(someBean);
		assertThat(beanAsNamed.toNamed()).isExactlyInstanceOf(BeanResolver.class);
		BeanResolver beanResolver = (BeanResolver) beanAsNamed.toNamed();
		assertThat(beanResolver.getBean()).isSameAs(someBean);
		assertThatThrownBy(() -> beanAsNamed.toIterator()).isInstanceOf(QueryBuildException.class);
	}

	private void testIteratorResolver(IteratorResolver resolver, Object... values) {
		assertThat(resolver).toIterable().containsExactly(values);
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
}
