package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class IteratorResolverImplTest {

	@Test
	public void testDefaultImplementation() {
		IteratorResolverImpl resolver = new IteratorResolverImpl(Arrays.asList("a", "bb", 3));
		assertThat(resolver).toIterable().containsExactly("a", "bb", 3);
	}
}
