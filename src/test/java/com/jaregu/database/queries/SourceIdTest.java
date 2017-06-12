package com.jaregu.database.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SourceIdTest {

	@Test
	public void testClassCreation() throws Exception {
		assertEquals("com.jaregu.database.queries.SourceIdTest", SourceId.of(SourceIdTest.class).getId());
	}

	@Test
	public void testStringCreation() throws Exception {
		assertEquals("aaaBBB", SourceId.of("aaaBBB").getId());
		assertEquals("aaaBBB", SourceId.of("aaaBBB ").getId());
		assertEquals("aaaBBB", SourceId.of(" aaaBBB").getId());
		assertEquals("aaaBBB", SourceId.of(" aaaBBB ").getId());
	}

	@Test
	public void testFileNameCreation() throws Exception {
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("aaa/bbb/ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("aaa/bbb/ccc.pdf").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("/aaa/bbb/ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("/aaa/bbb/ccc.pdf").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("aaa\\bbb\\ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("aaa\\bbb\\ccc.pdf").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("\\aaa\\bbb\\ccc").getId());
		assertEquals("aaa.bbb.ccc", SourceId.ofPath("\\aaa\\bbb\\ccc.pdf").getId());
	}

	@Test
	public void testEquality() throws Exception {
		assertTrue(SourceId.of("aaa").equals(SourceId.of("aaa")));
		assertFalse(SourceId.of("bbb").equals(SourceId.of("aaa")));
	}

	@Test
	public void testHash() throws Exception {
		assertTrue(SourceId.of("aaa").hashCode() == SourceId.of("aaa").hashCode());
		assertFalse(SourceId.of("bbb").hashCode() == SourceId.of("aaa").hashCode());
	}

}
