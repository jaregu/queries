package com.jaregu.database.queries.building;

import static com.jaregu.database.queries.building.ParameterBinderWithCollectionSupport.builder;
import static com.jaregu.database.queries.building.ParameterBinderWithCollectionSupport.collectionMapper;
import static com.jaregu.database.queries.building.ParameterBinderWithCollectionSupport.lastValueFiller;
import static com.jaregu.database.queries.building.ParameterBinderWithCollectionSupport.nullValueFiller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import org.assertj.core.util.Sets;
import org.junit.Test;

import com.jaregu.database.queries.building.ParameterBinder.Result;
import com.jaregu.database.queries.building.ParameterBinderWithCollectionSupport.Builder;

public class ParameterBinderWithCollectionSupportTest {

	@Test
	public void testErroneousConstructor() {
		assertThatThrownBy(() -> new ParameterBinderWithCollectionSupport(null, null, null))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new ParameterBinderWithCollectionSupport(Collections.emptySet(), null, null))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new ParameterBinderWithCollectionSupport(Collections.emptySet(), x -> null, null))
				.isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> new ParameterBinderWithCollectionSupport(Collections.emptySet(), x -> null, x -> null))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(
				() -> new ParameterBinderWithCollectionSupport(Sets.newLinkedHashSet(0), x -> null, x -> null))
						.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ParameterBinderWithCollectionSupport(Sets.newLinkedHashSet((Integer) null),
				x -> null, x -> null)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(
				() -> new ParameterBinderWithCollectionSupport(Sets.newLinkedHashSet(-1), x -> null, x -> null))
						.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testErroneousBuilder() {
		assertThatThrownBy(() -> builder().build()).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> builder().templateSizes(Collections.emptyList()).build())
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> builder().templateSizes(Collections.emptySet()).build())
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> builder().templateSizes(Arrays.asList((Integer) null)).build())
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> builder().templateSizes(Arrays.asList((Integer) null, 0)).build())
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public void testBuilder() {
		Function<Object, Collection<?>> collectionFunction = x -> null;
		Function<Collection<?>, ?> fillerFunction = x -> null;
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(10, 1, 1, 3))
				.collectionFunction(collectionFunction).fillRestWith(fillerFunction).build();

		assertThat(binder.getTemplateSizes()).containsExactly(1, 3, 10);
		assertThat(binder.getCollectionFunction()).isSameAs(collectionFunction);
		assertThat(binder.getFillerFunction()).isSameAs(fillerFunction);
	}

	@Test
	public void testBuilderDefaults() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1)).build();

		assertThat(binder.getTemplateSizes()).containsExactly(1);
		assertThat(binder.getCollectionFunction()).isSameAs(collectionMapper());
		assertThat(binder.getFillerFunction()).isSameAs(lastValueFiller());
	}

	@Test
	public void testBuiltinFunctions() {
		Builder builder = builder().templateSizes(Arrays.asList(1)).forCollections().fillRestWithNullValue();
		assertThat(builder.build().getTemplateSizes()).containsExactly(1);
		assertThat(builder.build().getCollectionFunction()).isSameAs(collectionMapper());
		assertThat(builder.build().getFillerFunction()).isSameAs(nullValueFiller());
		builder.fillRestWithLastValue();
		assertThat(builder.build().getFillerFunction()).isSameAs(lastValueFiller());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCollectionMapper() {
		assertThat(collectionMapper().apply(Collections.emptyList())).isEmpty();
		assertThat((Collection<Object>) collectionMapper().apply(Arrays.asList(1, 2, "3"))).containsExactly(1, 2, "3");
		assertThat(collectionMapper().apply(Collections.emptySet())).isEmpty();
		assertThat((Collection<Object>) collectionMapper().apply(Sets.newLinkedHashSet(1, 2, "3"))).containsExactly(1,
				2, "3");
		assertThat(collectionMapper().apply("aaa")).isNull();
		assertThat(collectionMapper().apply(new Object())).isNull();
		assertThat(collectionMapper().apply(null)).isNull();
	}

	@Test
	public void testLastValueFiller() {
		assertThatThrownBy(() -> lastValueFiller().apply(Collections.emptyList()))
				.isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> lastValueFiller().apply(null)).isInstanceOf(NullPointerException.class);

		assertThat(lastValueFiller().apply(Arrays.asList(1, 2, "3"))).isEqualTo("3");
		assertThat(lastValueFiller().apply(Arrays.asList(1, 2, null))).isEqualTo(null);
		assertThat(lastValueFiller().apply(Arrays.asList(null, 1, 2))).isEqualTo(2);
		assertThat(lastValueFiller().apply(Sets.newLinkedHashSet(1, 2))).isEqualTo(2);
	}

	@Test
	public void testNullValueFiller() {
		assertThat(nullValueFiller().apply(null)).isNull();
		assertThat(nullValueFiller().apply(Collections.emptyList())).isNull();
		assertThat(nullValueFiller().apply(Arrays.asList(1, 2, "3"))).isNull();
		assertThat(nullValueFiller().apply(Arrays.asList(1, 2, null))).isNull();
		assertThat(nullValueFiller().apply(Arrays.asList(null, 1, 2))).isNull();
		assertThat(nullValueFiller().apply(Sets.newLinkedHashSet(1, 2))).isNull();
	}

	@Test
	public void testSinglePlacementNullValueLastValue() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1)).fillRestWithNullValue()
				.build();

		Result result = binder.process(null);
		assertThat(result.getSql()).isEqualTo("?");
		assertThat(result.getParameters()).containsExactly((Object) null);
	}

	@Test
	public void testSinglePlacementEmptyListLastValue() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1)).fillRestWithLastValue()
				.build();

		assertThatThrownBy(() -> binder.process(Collections.emptyList()).getParameters())
				.isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testSinglePlacementEmptyListNullValue() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1)).fillRestWithNullValue()
				.build();

		Result result = binder.process(Collections.emptyList());
		assertThat(result.getSql()).isEqualTo("?");
		assertThat(result.getParameters()).containsExactly((Object) null);
	}

	@Test
	public void testSinglePlacementWithOneValue() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1)).build();

		Result result = binder.process(Collections.singletonList("abc"));
		assertThat(result.getSql()).isEqualTo("?");
		assertThat(result.getParameters()).containsExactly("abc");
	}

	@Test
	public void testmaxSizeExceeded() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1, 3))
				.fillRestWithLastValue().build();

		assertThatThrownBy(() -> binder.process(Arrays.asList(1, 2, 3, 4))).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testMultipleWithLastRepeated() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1, 3))
				.fillRestWithLastValue().build();

		assertThat(binder.process(1).getParameters()).containsExactly(1);
		assertThat(binder.process(1).getSql()).isEqualTo("?");
		assertThat(binder.process(Arrays.asList(1, 2)).getParameters()).containsExactly(1, 2, 2);
		assertThat(binder.process(Arrays.asList(1, 2)).getSql()).isEqualTo("?,?,?");
	}

	@Test
	public void testMultipleWithNullRepeated() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1, 3))
				.fillRestWithNullValue().build();

		assertThat(binder.process(1).getParameters()).containsExactly(1);
		assertThat(binder.process(1).getSql()).isEqualTo("?");
		assertThat(binder.process(Arrays.asList(1, 2)).getParameters()).containsExactly(1, 2, null);
		assertThat(binder.process(Arrays.asList(1, 2)).getSql()).isEqualTo("?,?,?");
	}

	@Test
	public void testMultipleWithValueRepeated() {
		ParameterBinderWithCollectionSupport binder = builder().templateSizes(Arrays.asList(1, 3)).fillRestWithValue(3)
				.build();

		assertThat(binder.process(1).getParameters()).containsExactly(1);
		assertThat(binder.process(1).getSql()).isEqualTo("?");
		assertThat(binder.process(Arrays.asList(1, 2)).getParameters()).containsExactly(1, 2, 3);
		assertThat(binder.process(Arrays.asList(1, 2)).getSql()).isEqualTo("?,?,?");
	}
}
