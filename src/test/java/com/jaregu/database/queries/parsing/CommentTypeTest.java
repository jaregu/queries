package com.jaregu.database.queries.parsing;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommentTypeTest {

	@Test
	public void testHypenMatches() throws Exception {
		assertEquals(false, CommentType.HYPHENS.matches("aaa"));
		assertEquals(false, CommentType.HYPHENS.matches("/* aaa */"));
		assertEquals(false, CommentType.HYPHENS.matches(" -- aaa/n"));

		assertEquals(true, CommentType.HYPHENS.matches("--aaa"));
		assertEquals(true, CommentType.HYPHENS.matches("-- aaa"));
		assertEquals(true, CommentType.HYPHENS.matches("--- aaa"));
		assertEquals(true, CommentType.HYPHENS.matches("--\n"));
		assertEquals(true, CommentType.HYPHENS.matches("--aaa\n"));
		assertEquals(true, CommentType.HYPHENS.matches("-- aaa \n"));
		assertEquals(true, CommentType.HYPHENS.matches("--- aaa \n"));
	}

	@Test
	public void testSlashMatches() throws Exception {
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches("aaa"));
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches("-- aaa \n"));
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches("/*"));
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches("/* aaa"));
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches("/* aaa *"));
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches(" /* aaa */"));
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches("/* aaa */ "));
		assertEquals(false, CommentType.SLASH_AND_ASTERISK.matches("/* aaa */\n"));

		assertEquals(true, CommentType.SLASH_AND_ASTERISK.matches("/* aaa */"));
		assertEquals(true, CommentType.SLASH_AND_ASTERISK.matches("/** aaa */"));
		assertEquals(true, CommentType.SLASH_AND_ASTERISK.matches("/* aaa\n\n */"));
		assertEquals(true, CommentType.SLASH_AND_ASTERISK.matches("/* aaa bbb */"));
	}

	@Test(expected = QueryParseException.class)
	public void testUnwrapNonHypenComment() throws Exception {
		CommentType.HYPHENS.unwrap(" -- aaa");
	}

	@Test
	public void testUnwrapHypenComment() throws Exception {
		assertEquals("aaa", CommentType.HYPHENS.unwrap("--aaa"));
		assertEquals("aaa", CommentType.HYPHENS.unwrap("-- aaa"));
		assertEquals("aaa", CommentType.HYPHENS.unwrap("--aaa "));
		assertEquals("aaa", CommentType.HYPHENS.unwrap("--aaa \n \n \n   \n"));
		assertEquals("aaa", CommentType.HYPHENS.unwrap("-- aaa    \n"));
		assertEquals("aaa", CommentType.HYPHENS.unwrap("-- aaa \t\n\t   "));
		assertEquals("aaa", CommentType.HYPHENS.unwrap("--aaa\n"));
		assertEquals("aaa bbb", CommentType.HYPHENS.unwrap("-- aaa bbb  "));
		assertEquals("aaa bbb", CommentType.HYPHENS.unwrap("-- aaa bbb  \n"));
	}

	public void testWrapHypenComment() throws Exception {
		assertEquals("--aaa\n", CommentType.HYPHENS.wrap("aaa"));
		assertEquals("-- aaa \n", CommentType.HYPHENS.wrap(" aaa "));
	}

	@Test(expected = QueryParseException.class)
	public void testUnwrapNonSlashComment() throws Exception {
		CommentType.SLASH_AND_ASTERISK.unwrap("/* bbb */ ");
	}

	@Test
	public void testUnwrapSlashComment() throws Exception {
		assertEquals("bbb", CommentType.SLASH_AND_ASTERISK.unwrap("/*bbb*/"));
		assertEquals("bbb", CommentType.SLASH_AND_ASTERISK.unwrap("/* bbb */"));
		assertEquals("bbb", CommentType.SLASH_AND_ASTERISK.unwrap("/* bbb \n*/"));
		assertEquals("aaa\nbbb", CommentType.SLASH_AND_ASTERISK.unwrap("/* aaa\nbbb \n*/"));
	}

	public void testWrapSlashComment() throws Exception {
		assertEquals("/*aaa*/", CommentType.HYPHENS.wrap("aaa"));
		assertEquals("/* aaa */", CommentType.HYPHENS.wrap(" aaa "));
	}
}
