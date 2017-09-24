package com.jaregu.database.queries.ext;

import static com.jaregu.database.queries.ext.OffsetLimit.empty;
import static com.jaregu.database.queries.ext.OffsetLimit.of;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class OffsetLimitImplTest {

	@Test
	public void testEmptyCreation() {
		assertThat(empty().getLimit()).isNull();
		assertThat(empty().getOffset()).isNull();
	}

	@Test
	public void testLimitCreation() {
		OffsetLimit offsetLimit = OffsetLimit.of(123);
		assertThat(offsetLimit.getOffset()).isEqualTo(0);
		assertThat(offsetLimit.getLimit()).isEqualTo(123);
	}

	@Test
	public void testFullCreation() {
		OffsetLimit offsetLimit = of(123, 10);
		assertThat(offsetLimit.getOffset()).isEqualTo(123);
		assertThat(offsetLimit.getLimit()).isEqualTo(10);
	}

	@Test
	public void testImmutability() {
		OffsetLimit offsetLimit = of(123, 10);
		assertThat(offsetLimit.getOffset()).isEqualTo(123);
		assertThat(offsetLimit.getLimit()).isEqualTo(10);
		offsetLimit.offset(100);
		offsetLimit.limit(200);
		assertThat(offsetLimit.getOffset()).isEqualTo(123);
		assertThat(offsetLimit.getLimit()).isEqualTo(10);
	}

	@Test
	public void testOffsetChanging() {
		assertThat(empty().offset(134).getOffset()).isEqualTo(134);
		assertThat(empty().offset(134).getLimit()).isNull();
	}

	@Test
	public void testLimitChanging() {
		assertThat(empty().limit(234).getLimit()).isEqualTo(234);
		assertThat(empty().limit(234).getOffset()).isNull();
	}

	@Test
	public void testNextPage() {
		assertThat(empty().nextPage().getLimit()).isNull();
		assertThat(empty().nextPage().getOffset()).isNull();
		assertThat(empty().limit(1).nextPage().getLimit()).isEqualTo(1);
		assertThat(empty().limit(1).nextPage().getOffset()).isNull();
		assertThat(empty().offset(2).nextPage().getLimit()).isNull();
		assertThat(empty().offset(2).nextPage().getOffset()).isEqualTo(2);
		assertThat(empty().limit(11).offset(10).nextPage().getOffset()).isEqualTo(21);
		assertThat(empty().limit(11).offset(10).nextPage().getLimit()).isEqualTo(11);
	}

	@Test
	public void testNextPageFull() {
		assertThat(empty().nextPageFull(10).getLimit()).isNull();
		assertThat(empty().nextPageFull(10).getOffset()).isNull();
		assertThat(empty().limit(1).nextPageFull(10).getLimit()).isEqualTo(1);
		assertThat(empty().limit(1).nextPageFull(10).getOffset()).isNull();
		assertThat(empty().offset(2).nextPageFull(10).getLimit()).isNull();
		assertThat(empty().offset(2).nextPageFull(10).getOffset()).isEqualTo(2);

		assertThat(of(11, 10).nextPageFull(100).getOffset()).isEqualTo(21);
		assertThat(of(11, 10).nextPageFull(100).getLimit()).isEqualTo(10);
		assertThat(of(1, 10).nextPageFull(5).getOffset()).isEqualTo(0);
		assertThat(of(11, 10).nextPageFull(5).getLimit()).isEqualTo(10);
		assertThat(of(1, 9).nextPageFull(10).getOffset()).isEqualTo(1);
		assertThat(of(1, 9).nextPageFull(10).getLimit()).isEqualTo(9);
	}
}
