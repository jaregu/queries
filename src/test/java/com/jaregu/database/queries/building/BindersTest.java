package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class BindersTest {

	private TestBinders binders;

	@Before
	public void setUp() {
		binders = new TestBinders();
	}

	@Test
	public void testBinderForCollectionsAndLastValueNull() {
		binders.binderForCollectionsAndLastValueNull(Arrays.asList(3, 1));
		assertThat(binders.binder).isExactlyInstanceOf(ParameterBinderWithCollectionSupport.class);
		assertThat(binders.binder.process(1).getParameters()).containsExactly(1);
		assertThat(binders.binder.process(1).getSql()).isEqualTo("?");
		assertThat(binders.binder.process(Arrays.asList(1, 2)).getParameters()).containsExactly(1, 2, null);
		assertThat(binders.binder.process(Arrays.asList(1, 2)).getSql()).isEqualTo("?,?,?");
	}

	@Test
	public void testBinderForCollectionsAndLastValueRepeated() {
		binders.binderForCollectionsAndLastValueRepeated(Arrays.asList(3, 1));
		assertThat(binders.binder).isExactlyInstanceOf(ParameterBinderWithCollectionSupport.class);
		assertThat(binders.binder.process(1).getParameters()).containsExactly(1);
		assertThat(binders.binder.process(1).getSql()).isEqualTo("?");
		assertThat(binders.binder.process(Arrays.asList(1, 2)).getParameters()).containsExactly(1, 2, 2);
		assertThat(binders.binder.process(Arrays.asList(1, 2)).getSql()).isEqualTo("?,?,?");
	}

	@Test
	public void testStaticDefaultBinder() {
		assertThat(Binders.defaultBinder()).isExactlyInstanceOf(ParameterBinderDefaultImpl.class);
	}

	private static class TestBinders implements Binders<TestBinders> {

		private ParameterBinder binder;

		@Override
		public TestBinders binder(ParameterBinder binder) {
			this.binder = binder;
			return this;
		}
	}
}
