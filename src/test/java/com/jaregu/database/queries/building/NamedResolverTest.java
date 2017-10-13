package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class NamedResolverTest {

	@Test
	public void testStaticOfMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("one", 1);
		params.put("two", 22);
		params.put("three", null);

		NamedResolver resolver = NamedResolver.ofMap(params);
		assertThat(resolver.getValue("one")).isEqualTo(1);
		assertThat(resolver.getValue("two")).isEqualTo(22);
		assertThat(resolver.getValue("three")).isNull();
		assertThatThrownBy(() -> resolver.getValue("uknown")).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testStaticOfList() {
		NamedResolver resolver = NamedResolver.ofList(Arrays.asList(1, 22, null));
		assertThat(resolver.getValue("0")).isEqualTo(1);
		assertThat(resolver.getValue("1")).isEqualTo(22);
		assertThat(resolver.getValue("2")).isNull();
		assertThatThrownBy(() -> resolver.getValue("-1")).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> resolver.getValue("3")).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> resolver.getValue("uknown")).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testStaticOfBean() {
		SomeBean someBean = new SomeBean();
		NamedResolver resolver = NamedResolver.ofBean(someBean);
		assertThat(resolver).isExactlyInstanceOf(BeanResolver.class);
		BeanResolver beanResolver = (BeanResolver) resolver;
		assertThat(beanResolver.getBean()).isSameAs(someBean);
	}

	public class SomeBean {
	}
}
