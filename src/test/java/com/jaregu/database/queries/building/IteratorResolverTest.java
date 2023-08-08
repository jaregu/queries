package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class IteratorResolverTest {

	@Test
	public void testStaticOf() {
		IteratorResolver resolver = IteratorResolver.of(Arrays.asList("a", "bb", 3));
		assertThat(resolver).toIterable().containsExactly("a", "bb", 3);
	}
}
