package com.jaregu.database.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SourceIdTest {

	@Test
	public void testClassCreation() throws Exception {
		assertEquals("com.jaregu.database.queries.SourceIdTest", SourceId.ofClass(SourceIdTest.class).getId());
	}

	@Test
	public void testStringCreation() throws Exception {
		assertEquals("aaaBBB", SourceId.ofId("aaaBBB").getId());
		assertEquals("aaaBBB", SourceId.ofId("aaaBBB ").getId());
		assertEquals("aaaBBB", SourceId.ofId(" aaaBBB").getId());
		assertEquals("aaaBBB", SourceId.ofId(" aaaBBB ").getId());
	}

	@Test
	public void testFileNameCreation() throws Exception {
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
