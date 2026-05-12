package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

	@BeforeEach
	public void setUp() {
		when(namedSupplier.get()).thenReturn(namedResolver);
		when(iteratorSupplier.get()).thenReturn(iteratorResolver);

		resolver = new ParametersResolverImpl(namedSupplier, iteratorSupplier);
	}

	@Test
	public void testNamedParameter() {
		assertThat(resolver.toNamed()).isSameAs(namedResolver);
		assertThat(resolver.toNamed()).isSameAs(namedResolver);
		verify(namedSupplier, times(1)).get();
		assertThatThrownBy(() -> resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> resolver.toIterator()).isInstanceOf(QueryBuildException.class);
		verifyNoInteractions(iteratorSupplier);
	}

	@Test
	public void testIteratorParameter() {
		assertThat(resolver.toIterator()).isSameAs(iteratorResolver);
		assertThat(resolver.toIterator()).isSameAs(iteratorResolver);
		verify(iteratorSupplier, times(1)).get();
		assertThatThrownBy(() -> resolver.toNamed()).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> resolver.toNamed()).isInstanceOf(QueryBuildException.class);
		verifyNoInteractions(namedSupplier);
	}
}
