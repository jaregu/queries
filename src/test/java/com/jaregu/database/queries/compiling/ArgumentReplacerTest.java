package com.jaregu.database.queries.compiling;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.junit.Test;

public class ArgumentReplacerTest {

	@Test
	public void testErrors() {
		error(() -> getReplaced(""));
		error(() -> getReplaced(" "));
		error(() -> getReplaced("    "));
		error(() -> getReplaced("aaaaa'"));
		error(() -> getReplaced("aa''aaa'"));
		error(() -> getReplaced("'''"));
		error(() -> getReplaced(null));
	}

	private String getReplaced(String part) {
		return ArgumentReplacer.forSql(part).replace();
	}

	@Test
	public void testQuestionMark() throws Exception {
		assertEquals("something = ?", ArgumentReplacer.forSql("something = ?").addComment(true).replace());
		assertEquals("something = ?", ArgumentReplacer.forSql("something = ?").addComment(false).replace());
		assertEquals(" something = ? ", ArgumentReplacer.forSql(" something = ? ").addComment(true).replace());
		assertEquals(" something = ? ", ArgumentReplacer.forSql(" something = ? ").addComment(false).replace());
	}

	@Test
	public void testStringConstant() {
		testDifferentPositions("'aaa'");
		testDifferentPositions("''");
		testDifferentPositions("''''");
		testDifferentPositions("'aaa tim''s money'");
	}

	@Test
	public void testNumberConstant() {
		testDifferentPositions("123");
		testDifferentPositions("1");
		testDifferentPositions("99");
	}

	@Test
	public void testSomeFunctions() {
		testDifferentPositions("(xxx)");
		testDifferentPositions("(aaa(xxx, 'something', orthis))");
		testDifferentPositions("((aaaa), (1211, (12)))");
		testDifferentPositions("(aaa  (xxx, 'something', orthis))");
		testDifferentPositions("(aaa(xxx, '(', orthis))");
	}
	
	@Test
	public void testEndingWithComma() {
		assertEquals("a = ?, ", ArgumentReplacer.forSql("a = 'const', ").addComment(false).replace());
		assertEquals("a = ? , ", ArgumentReplacer.forSql("a = 123 , ").addComment(false).replace());
		assertEquals("a = ? , ", ArgumentReplacer.forSql("a = ? , ").addComment(false).replace());
		assertEquals("a = ? , ", ArgumentReplacer.forSql("a = (aaa (bb), (ccc)) , ").addComment(false).replace());
	}

	@Test
	public void testNull() {
		testDifferentPositions("NULL");
		testDifferentPositions("null");
		testDifferentPositions("NuLl");
	}

	private void testDifferentPositions(String part) {
		testDifferentReplacements(part, false);
		testDifferentReplacements(part, true);
	}

	private void testDifferentReplacements(String part, boolean addComment) {

		test(addComment, "?", part, part);
		test(addComment, " ?", " " + part, part);
		test(addComment, " ? ", " " + part + " ", part);
		test(addComment, "  ?  ", "  " + part + "  ", part);
		test(addComment, ",?", "," + part, part);
		test(addComment, ")?", ")" + part, part);
		test(addComment, "=?", "=" + part, part);
		test(addComment, ",? ", "," + part + " ", part);
		test(addComment, ")? ", ")" + part + " ", part);
		test(addComment, " = ? ", " = " + part + " ", part);
		test(addComment, " , ? ", " , " + part + " ", part);
		test(addComment, " ) ? ", " ) " + part + " ", part);
		test(addComment, " = ? ", " = " + part + " ", part);
		test(addComment, " x.aaa = ? ", " x.aaa = " + part + " ", part);
	}

	private void test(boolean addComment, String expected, String originalPart, String replacement) {
		String replaced = ArgumentReplacer.forSql(originalPart).addComment(addComment).replace();
		assertEquals(addComment ? expected + " /* " + replacement + " */ " : expected, replaced);
	}

	private void error(Callable<String> callable) {
		try {
			callable.call();
			fail();
		} catch (Exception e) {
		}
	}
}
