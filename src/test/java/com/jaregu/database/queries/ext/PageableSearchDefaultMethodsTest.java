package com.jaregu.database.queries.ext;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class PageableSearchDefaultMethodsTest {

	private static class OffsetLimitSearch implements PageableSearch<OffsetLimitSearch> {

		private Integer limit;
		private Integer offset;

		public OffsetLimitSearch(Integer offset, Integer limit) {
			this.offset = offset;
			this.limit = limit;
		}

		@Override
		public Integer getOffset() {
			return offset;
		}

		@Override
		public Integer getLimit() {
			return limit;
		}

		@Override
		public OffsetLimitSearch withOffset(Integer offset) {
			return new OffsetLimitSearch(offset, limit);
		}

		public OffsetLimitSearch offset(Integer offset) {
			return withOffset(offset);
		}

		@Override
		public OffsetLimitSearch withLimit(Integer limit) {
			return new OffsetLimitSearch(offset, limit);
		}

		public OffsetLimitSearch limit(Integer limit) {
			return withLimit(limit);
		}
	}

	private OffsetLimitSearch empty() {
		return new OffsetLimitSearch(null, null);
	}

	private OffsetLimitSearch zero() {
		return new OffsetLimitSearch(0, 0);
	}

	private OffsetLimitSearch of(Integer offset, Integer limit) {
		return new OffsetLimitSearch(offset, limit);
	}

	@Test
	public void testNextPageError() {
		assertThatThrownBy(() -> empty().nextPage()).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> empty().nextPageFull(100)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void testNextPage() {
		assertThat(zero().limit(1).nextPage().getLimit()).isEqualTo(1);
		assertThat(zero().offset(2).nextPage().getOffset()).isEqualTo(2);
		assertThat(zero().limit(11).offset(10).nextPage().getOffset()).isEqualTo(21);
		assertThat(zero().limit(11).offset(10).nextPage().getLimit()).isEqualTo(11);
	}

	@Test
	public void testNextPageFull() {
		assertThat(zero().limit(1).nextPageFull(10).getLimit()).isEqualTo(1);
		assertThat(zero().offset(2).nextPageFull(10).getOffset()).isEqualTo(2);

		assertThat(of(11, 10).nextPageFull(100).getOffset()).isEqualTo(21);
		assertThat(of(11, 10).nextPageFull(100).getLimit()).isEqualTo(10);
		assertThat(of(1, 10).nextPageFull(5).getOffset()).isEqualTo(0);
		assertThat(of(11, 10).nextPageFull(5).getLimit()).isEqualTo(10);
		assertThat(of(1, 9).nextPageFull(10).getOffset()).isEqualTo(1);
		assertThat(of(1, 9).nextPageFull(10).getLimit()).isEqualTo(9);
	}
}
