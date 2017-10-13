package com.jaregu.database.queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class QueryIdTest {

	@Test
	public void testIdCreation() throws Exception {
		assertThatThrownBy(() -> QueryId.of("aaa"))
				.as("Query Id must be in form sourceIdPart1.[sourceIdPart2.[... sourceIdPartN]]queryIdPart")
				.isInstanceOf(IllegalArgumentException.class);

		QueryId queryId = QueryId.of("aaa.bbb.ccc");
		assertThat(queryId.getId()).isEqualTo("ccc");
		assertThat(queryId.getSourceId().getId()).isEqualTo("aaa.bbb");
	}

	@Test
	public void testRelativeCreation() throws Exception {
		SourceId sourceId = SourceId.ofId("aaa.bbb");

		assertThat(QueryId.of(sourceId, "ccc").getId()).isEqualTo("ccc");
		assertThat(QueryId.of(sourceId, "ccc").getSourceId().getId()).isEqualTo("aaa.bbb");

		assertThat(sourceId.getQueryId("ccc").getId()).isEqualTo("ccc");
		assertThat(sourceId.getQueryId("ccc").getSourceId().getId()).isEqualTo("aaa.bbb");

		assertThat(QueryId.of(SourceId.ofId("aaa.bbb"), "ccc").getSourceId().getId()).isEqualTo("aaa.bbb");
	}

	@Test
	public void testEquality() throws Exception {
		assertThat(QueryId.of("aaa.bbb")).isEqualTo(QueryId.of("aaa.bbb"));
		assertThat(QueryId.of(SourceId.ofId("aaa"), "bbb")).isEqualTo(QueryId.of("aaa.bbb"));
		assertThat(SourceId.ofId("aaa").getQueryId("bbb")).isEqualTo(QueryId.of("aaa.bbb"));

		assertThat(QueryId.of("bbb.aaa")).isNotEqualTo(QueryId.of("aaa.bbb"));
		assertThat(SourceId.ofId("aaa").getQueryId("bbb")).isNotEqualTo(SourceId.ofId("aaa2").getQueryId("bbb"));
	}

	@Test
	public void testHash() throws Exception {
		assertThat(QueryId.of("aaa.bbb").hashCode()).isEqualTo(QueryId.of("aaa.bbb").hashCode());
		assertThat(QueryId.of(SourceId.ofId("aaa"), "bbb").hashCode()).isEqualTo(QueryId.of("aaa.bbb").hashCode());
		assertThat(SourceId.ofId("aaa").getQueryId("bbb").hashCode()).isEqualTo(QueryId.of("aaa.bbb").hashCode());
		assertThat(QueryId.of("bbb.aaa").hashCode()).isNotEqualTo(QueryId.of("aaa.bbb").hashCode());
	}

}
