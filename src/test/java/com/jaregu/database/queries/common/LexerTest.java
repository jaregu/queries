package com.jaregu.database.queries.common;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

import com.jaregu.database.queries.common.Lexer.LexerMatcher;
import com.jaregu.database.queries.common.Lexer.LexerPattern;

public class LexerTest {

	@Test(expected = NullPointerException.class)
	public void testRequiredContent() throws Exception {
		new Lexer(null);
	}

	@Test
	public void testHasMoreAndExpect() throws Exception {
		Lexer lx = new Lexer("aaa");
		assertTrue(lx.hasMore());
		lx.expect("a");
		assertTrue(lx.hasMore());
		lx.expect("aa");
		assertFalse(lx.hasMore());
	}

	@Test(expected = LexerException.class)
	public void testExpectError() throws Exception {
		Lexer lx = new Lexer("aaa");
		lx.expect("b");
	}

	@Test
	public void testLookingAtString() throws Exception {
		Lexer lx = new Lexer("abc");
		assertTrue(lx.lookingAt("a"));
		assertTrue(lx.lookingAt("ab"));
		assertTrue(lx.lookingAt("abc"));
		assertFalse(lx.lookingAt("b"));
		assertFalse(lx.lookingAt("bc"));
		assertFalse(lx.lookingAt("c"));
		lx.expect("a");
		assertFalse(lx.lookingAt("a"));
		assertFalse(lx.lookingAt("ab"));
		assertFalse(lx.lookingAt("abc"));
		assertTrue(lx.lookingAt("b"));
		assertTrue(lx.lookingAt("bc"));
		assertFalse(lx.lookingAt("c"));
		lx.expect("b");
		assertFalse(lx.lookingAt("a"));
		assertFalse(lx.lookingAt("ab"));
		assertFalse(lx.lookingAt("abc"));
		assertFalse(lx.lookingAt("b"));
		assertFalse(lx.lookingAt("bc"));
		assertTrue(lx.lookingAt("c"));
		lx.expect("c");
		assertFalse(lx.lookingAt("a"));
		assertFalse(lx.lookingAt("ab"));
		assertFalse(lx.lookingAt("abc"));
		assertFalse(lx.lookingAt("b"));
		assertFalse(lx.lookingAt("bc"));
		assertFalse(lx.lookingAt("c"));
	}

	@Test
	public void testLookingAtAnyDigit() throws Exception {
		Lexer lx = new Lexer("abc 123 def");
		assertFalse(lx.lookingAt(Lexer.anyDigit()));
		lx.expect("abc ");
		assertTrue(lx.lookingAt(Lexer.anyDigit()));
		lx.expect("1");
		assertTrue(lx.lookingAt(Lexer.anyDigit()));
		lx.expect("23");
		assertFalse(lx.lookingAt(Lexer.anyDigit()));
	}

	@Test
	public void testLookingAtAnyLetter() throws Exception {
		Lexer lx = new Lexer("abc 123 def");
		assertTrue(lx.lookingAt(Lexer.anyLetter()));
		lx.expect("abc");
		assertFalse(lx.lookingAt(Lexer.anyLetter()));
		lx.expect(" 123 ");
		assertTrue(lx.lookingAt(Lexer.anyLetter()));
	}

	@Test
	public void testLookingAtAnyLetterOrDigit() throws Exception {
		Lexer lx = new Lexer("abc 123 def");
		assertTrue(lx.lookingAt(Lexer.anyLetterOrDigit()));
		lx.expect("abc");
		assertFalse(lx.lookingAt(Lexer.anyLetterOrDigit()));
		lx.expect(" ");
		assertTrue(lx.lookingAt(Lexer.anyLetterOrDigit()));
	}

	@Test
	public void testLookingAtWhitespace() throws Exception {
		Lexer lx = new Lexer("abc 123\ndef");
		assertFalse(lx.lookingAt(Lexer.whitespace()));
		lx.expect("abc");
		assertTrue(lx.lookingAt(Lexer.whitespace()));
		lx.expect(" ");
		assertFalse(lx.lookingAt(Lexer.whitespace()));
		lx.expect("123");
		assertTrue(lx.lookingAt(Lexer.whitespace()));
	}

	@Test
	public void testLookingAtRegexp() throws Exception {
		LexerMatcher regexpMatcher = Lexer.regexp(Pattern.compile("\\d{2}"));
		Lexer lx = new Lexer("abc 123 de12f");
		assertFalse(lx.lookingAt(regexpMatcher));
		lx.expect("abc ");
		assertTrue(lx.lookingAt(regexpMatcher));
		lx.expect("1");
		assertTrue(lx.lookingAt(regexpMatcher));
		lx.expect("2");
		assertFalse(lx.lookingAt(regexpMatcher));
		lx.expect("3 de");
		assertTrue(lx.lookingAt(regexpMatcher));
		lx.expect("1");
		assertFalse(lx.lookingAt(regexpMatcher));
	}

	@Test
	public void testLookingAtEOF() throws Exception {
		Lexer lx = new Lexer("abc 123");
		assertFalse(lx.lookingAt(Lexer.eof()));
		lx.expect("abc 12");
		assertFalse(lx.lookingAt(Lexer.eof()));
		lx.expect("3");
		assertTrue(lx.lookingAt(Lexer.eof()));
		assertFalse(lx.hasMore());
	}

	@Test(expected = LexerException.class)
	public void testEmptyStringReadingError() throws Exception {
		LexerPattern pattern = Lexer.newPattern().stopAfter(Lexer.anyLetterOrDigit());
		Lexer lx = new Lexer("");
		assertFalse(lx.hasMore());
		lx.read(pattern);
	}

	@Test(expected = LexerException.class)
	public void testAfterEndReadingError() throws Exception {
		LexerPattern pattern = Lexer.newPattern().stopAfter(Lexer.anyLetterOrDigit());
		Lexer lx = new Lexer("a");
		assertTrue(lx.hasMore());
		assertEquals("a", lx.read(pattern));
		assertFalse(lx.hasMore());
		lx.read(pattern);
	}

	@Test(expected = LexerException.class)
	public void testNoMatchingReading() throws Exception {
		LexerPattern pattern = Lexer.newPattern().stopAfter(Lexer.anyDigit());
		Lexer lx = new Lexer("1a");
		assertTrue(lx.hasMore());
		assertEquals("1", lx.read(pattern));
		assertTrue(lx.hasMore());
		lx.read(pattern);
	}

	@Test
	public void testStringReading() throws Exception {
		testPattern("abc 123 def", Lexer.newPattern().stopAfter(" "), "abc ", "123 ", expect("def"));
		testPattern("abc 123 def", Lexer.newPattern().stopAfterAnyOf("c", "3").stopAfter("f"), "abc", " 123", " def");
		testPattern("abc 123 def", Lexer.newPattern().stopAfterAnyOf("c", "3", "f").stopBeforeAnyOf("b", "2", "e"), "a",
				null, expect("b"), "c", " 1", null, expect("2"), "3", " d", null, expect("e"), "f");
	}

	@Test
	public void testAnyDigitReading() throws Exception {
		testPattern("abc 123 def", Lexer.newPattern().stopAfter(Lexer.anyDigit()), "abc 123", expect(" def"));
		testPattern("abc 123 def", Lexer.newPattern().stopBefore(Lexer.anyDigit()), "abc ", null, expect("123 def"));
	}

	@Test
	public void testAnyLetterReading() throws Exception {
		testPattern("abc 123 def", Lexer.newPattern().stopAfter(Lexer.anyLetter()), "abc", " 123 def");
		testPattern("abc def xy", Lexer.newPattern().stopAfter(Lexer.anyLetter()), "abc", " def", " xy");
		testPattern("abc 123 def", Lexer.newPattern().stopBefore(Lexer.anyLetter()), null, expect("abc"), " 123 ",
				expect("def"));
	}

	@Test
	public void testAnyLetterOrDigitReading() throws Exception {
		testPattern("abc 123 def", Lexer.newPattern().stopAfter(Lexer.anyLetterOrDigit()), "abc", " 123", " def");
		testPattern("abc 123 def", Lexer.newPattern().stopBefore(Lexer.anyLetterOrDigit()), null, expect("abc"), " ",
				expect("123"), " ", expect("def"));
	}

	@Test
	public void testWhitespaceReading() throws Exception {
		testPattern("abc 123 def", Lexer.newPattern().stopAfter(Lexer.whitespace()), "abc ", "123 ", expect("def"));
		testPattern("abc 123\ndef", Lexer.newPattern().stopBefore(Lexer.whitespace()), "abc", expect(" "), "123",
				expect("\ndef"));
	}

	@Test
	public void testEOFReading() throws Exception {
		testPattern("abc 123 def", Lexer.newPattern().stopAfter(Lexer.eof()), "abc 123 def");
		testPattern("abc 123 def", Lexer.newPattern().stopBefore(Lexer.eof()), "abc 123 def");
		testPattern("abc 123 def", Lexer.newPattern().stopAtEof(), "abc 123 def");
	}

	@Test
	public void testRegexpReading() throws Exception {
		testPattern("abc 123", Lexer.newPattern().stopAfter(Lexer.regexp(Pattern.compile(".{1,2}"))), "ab", "c ", "12",
				"3");
		testPattern("abc 123", Lexer.newPattern().stopBefore(Lexer.regexp(Pattern.compile("\\s1"))), "abc",
				expect(" 123"));
	}

	@Test(expected = LexerException.class)
	public void testNonClosedSkipError() throws Exception {
		Lexer lx = new Lexer("123a123");
		lx.read(Lexer.newPattern().stopAfter(Lexer.eof()).skipAllBetween("a", "b"));
	}

	@Test
	public void testSkipRange() throws Exception {
		testPattern("abc '123' def", Lexer.newPattern().stopAfterAnyOf("1", "2", "3", "f").skipAllBetween("'", "'"),
				"abc '123' def");
		testPattern("123aaaaaabbbb", Lexer.newPattern().stopBefore("a").skipAllBetween("a", "b").stopBefore("b"),
				"123aaaaaab", null, expect("bbb"));
	}

	@Test
	public void testSkipSequence() throws Exception {
		testPattern("123456789", Lexer.newPattern().skipSequences("34", "56").stopAfterAnyOf("4", "6", "9"),
				"123456789");
	}

	@Test
	public void testCompexPatters() throws Exception {
		LexerMatcher digitAndSpaceMatcher = Lexer.regexp(Pattern.compile("\\d{1} "));
		testPattern("abc 123 def", Lexer.newPattern().stopAfterAnyOf("12").stopAfter(digitAndSpaceMatcher).stopAtEof(),
				"abc 12", "3 ", "def");
		testPattern("one 22 three",
				Lexer.newPattern().stopAfterAnyOf(Lexer.regexp(Pattern.compile("\\w+")), Lexer.eof()).stopAfter(" "),
				"one", " ", "22", " ", "three");

		testPattern("1 22 '33\"3' dd\"dd 'eee\"ee 666666",
				Lexer.newPattern().skipAllBetween("'", "'").skipAllBetween("\"", "\"").stopAfterAnyOf("3", "d", "666"),
				"1 22 '33\"3' d", "d", "\"dd 'eee\"ee 666", "666");

	}

	private void testPattern(String source, Lexer.LexerPattern pattern, CharSequence... chunks) {
		Lexer lx = new Lexer(source);
		for (CharSequence chunk : chunks) {
			assertTrue(lx.hasMore());
			if (chunk instanceof ExpectChunk) {
				lx.expect(chunk.toString());
			} else {
				assertEquals(chunk, lx.read(pattern));
			}
		}
		assertFalse(lx.hasMore());
	}

	private static CharSequence expect(String chunk) {
		return new ExpectChunk(chunk);
	}

	private static class ExpectChunk implements CharSequence {

		private String chunk;

		public ExpectChunk(String chunk) {
			this.chunk = chunk;
		}

		@Override
		public int length() {
			return chunk.length();
		}

		@Override
		public char charAt(int index) {
			return chunk.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return chunk.subSequence(start, end);
		}

		@Override
		public String toString() {
			return chunk;
		}
	}
}
