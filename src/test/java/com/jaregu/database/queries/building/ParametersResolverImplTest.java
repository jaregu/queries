package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParametersResolverImplTest {

	@Mock
	private Supplier<NamedResolver> namedSupplier;

	@Mock
	private NamedResolver namedResolver;

	@Mock
	private Supplier<IteratorResolver> iteratorSupplier;

	@Mock
	private IteratorResolver iteratorResolver;

	private ParametersResolverImpl resolver;

	@Before
	public void setUp() {
		when(namedSupplier.get()).thenReturn(namedResolver);
		when(iteratorSupplier.get()).thenReturn(iteratorResolver);

		resolver = new ParametersResolverImpl(namedSupplier, iteratorSupplier);
	}

	@Test
	public void testNamedParameter() {
		assertThat(resolver.getNamedResolver()).isSameAs(namedResolver);
		assertThat(resolver.getNamedResolver()).isSameAs(namedResolver);
		verify(namedSupplier, times(1)).get();
		assertThatThrownBy(() -> resolver.getIteratorResolver()).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> resolver.getIteratorResolver()).isInstanceOf(QueryBuildException.class);
		verifyZeroInteractions(iteratorSupplier);
	}

	@Test
	public void testIteratorParameter() {
		assertThat(resolver.getIteratorResolver()).isSameAs(iteratorResolver);
		assertThat(resolver.getIteratorResolver()).isSameAs(iteratorResolver);
		verify(iteratorSupplier, times(1)).get();
		assertThatThrownBy(() -> resolver.getNamedResolver()).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> resolver.getNamedResolver()).isInstanceOf(QueryBuildException.class);
		verifyZeroInteractions(namedSupplier);
	}
}
