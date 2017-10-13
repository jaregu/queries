package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;

import org.junit.Test;

import com.jaregu.database.queries.dialect.Pageable;

public class ToPagedQueryTest {

	@Test
	public void testByStrings() {
		testByOffsetLimitInts(null, null);
		testByOffsetLimitInts(123, null);
		testByOffsetLimitInts(null, 345);
		testByOffsetLimitInts(123, 345);
	}

	private void testByOffsetLimitInts(Integer offset, Integer limit) {
		ToPagedQuery toPaged = createInstance(page -> {
			assertThat(page.getOffset()).isEqualTo(offset);
			assertThat(page.getLimit()).isEqualTo(limit);
		});
		toPaged.toPagedQuery(offset, limit);
	}

	private ToPagedQuery createInstance(Consumer<Pageable> tester) {
		return (o) -> {
			tester.accept(o);
			return null;
		};
	}
}
