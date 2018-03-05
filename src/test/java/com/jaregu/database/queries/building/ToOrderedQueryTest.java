package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.Test;

import com.jaregu.database.queries.dialect.Orderable;

public class ToOrderedQueryTest {

	@Test
	public void testByStrings() {
		testByStrings((String[]) null);
		testByStrings(new String[] {});
		testByStrings("");
		testByStrings("aaa", "bbb");
	}

	private void testByStrings(String... values) {
		ToOrderedQuery toOrdered = createInstance(o -> {
			if (values == null) {
				assertThat(o.getOrderBy()).isEmpty();
			} else {
				assertThat(o.getOrderBy()).containsExactly(values);
			}
		});
		toOrdered.toOrderedQuery(values);
	}

	@Test
	public void testByIterable() {
		testByIterable((String[]) null);
		testByIterable(new String[] {});
		testByIterable("");
		testByIterable("aaa", "bbb");
	}

	private void testByIterable(String... values) {
		ToOrderedQuery toOrdered = createInstance(o -> {
			if (values == null) {
				assertThat(o.getOrderBy()).isEmpty();
			} else {
				assertThat(o.getOrderBy()).containsExactly(values);
			}
		});
		if (values == null) {
			toOrdered.toOrderedQuery((Iterable<String>) null);
		} else {
			toOrdered.toOrderedQuery(Arrays.asList(values));
		}
	}

	private ToOrderedQuery createInstance(Consumer<Orderable> tester) {
		return (o) -> {
			tester.accept(o);
			return null;
		};
	}
}
