package com.jaregu.database.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SourceIdTest {

	@Test
	public void testClassCreation() throws Exception {
		assertEquals("com.jaregu.database.queries.SourceIdTest", SourceId.ofClass(SourceIdTest.class).getId());
	}

	@Test
	public void testIdCreation() throws Exception {
		assertEquals("aaaBBB", SourceId.ofId("aaaBBB").getId());
		assertEquals("aaaBBB", SourceId.ofId("aaaBBB ").getId());
		assertEquals("aaaBBB", SourceId.ofId(" aaaBBB").getId());
		assertEquals("aaaBBB", SourceId.ofId(" aaaBBB ").getId());
	}

	@Test
	public void testResourceCreation() throws Exception {
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("aaa/bbb/ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("aaa/bbb/ccc.pdf").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("/aaa/bbb/ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("/aaa/bbb/ccc.pdf").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("aaa\\bbb\\ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("aaa\\bbb\\ccc.pdf").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("\\aaa\\bbb\\ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofResource("\\aaa\\bbb\\ccc.pdf").getId());
	}

	@Test
	public void testQueryIdCreation() throws Exception {
		SourceId sourceId = SourceId.ofId("aaa");
		assertEquals("bbb", sourceId.getQueryId("bbb").getId());
		assertSame(sourceId, sourceId.getQueryId("bbb").getSourceId());
	}

	@Test
	public void testEquality() throws Exception {
		assertTrue(SourceId.ofId("aaa").equals(SourceId.ofId("aaa")));
		assertFalse(SourceId.ofId("bbb").equals(SourceId.ofId("aaa")));
	}

	@Test
	public void testHash() throws Exception {
		assertTrue(SourceId.ofId("aaa").hashCode() == SourceId.ofId("aaa").hashCode());
		assertFalse(SourceId.ofId("bbb").hashCode() == SourceId.ofId("aaa").hashCode());
	}

}
