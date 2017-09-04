package com.jaregu.database.queries.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;

public class ParsedQueryPartImplTest {

	@Test
	public void testHyphenComment() throws Exception {
		testHyphenComment("-- some comment  \n", "some comment");
		testHyphenComment("-- some comment  ", "some comment");
		testHyphenComment("--some comment", "some comment");
		testHyphenComment("--- some comment  \n", "- some comment");
	}

	private void testHyphenComment(String content, String comment) {
		PartTester tester = new PartTester(content);
		tester.setCommentType(CommentType.HYPHENS);
		tester.setCommentContent(comment);
		tester.test();
	}

	@Test
	public void testSlashComment() throws Exception {
		testSlashComment("/* some comment  */", "some comment");
		testSlashComment("/*some comment*/", "some comment");
		testSlashComment("/*\nsome multiple \nlines comment\n   \n*/", "some multiple \nlines comment");
		testSlashComment("/** some comment */", "* some comment");
		testSlashComment("/** some\n \ncomment  \n*/", "* some\n \ncomment");
	}

	private void testSlashComment(String content, String comment) {
		PartTester tester = new PartTester(content);
		tester.setCommentType(CommentType.SLASH_AND_ASTERISK);
		tester.setCommentContent(comment);
		tester.test();
	}

	@Test
	public void testNamedVariables() throws Exception {
		testNamedVariable(":aaa", "aaa");
		testNamedVariable(":aaa.bbb", "aaa.bbb");
	}

	private void testNamedVariable(String content, String name) {
		PartTester tester = new PartTester(content);
		tester.setVariableName(name);
		tester.test();
	}

	@Test
	public void testAnonymousVariable() throws Exception {
		ParsedQueryPartImpl part = new ParsedQueryPartImpl("?");
		assertTrue(part.isAnonymousVariable());
		assertFalse(part.isComment());
		assertFalse(part.isNamedVariable());
		assertFalse(part.isSimplePart());
	}

	@Test
	public void testContent() throws Exception {
		testContent("some really nice content");
		testContent("   some content   ");
	}

	private void testContent(String content) throws Exception {
		PartTester tester = new PartTester(content);
		tester.test();
	}

	private static class PartTester {

		private List<Function<String, ParsedQueryPart>> creators = Arrays.asList(ParsedQueryPartImpl::new,
				ParsedQueryPart::create);

		private String content;
		private Optional<String> comment = Optional.empty();
		private Optional<CommentType> commentType = Optional.empty();
		private Optional<String> variableName = Optional.empty();

		public PartTester(String content) {
			this.content = content;
		}

		public void setCommentContent(String comment) {
			this.comment = Optional.of(comment);
		}

		public void setCommentType(CommentType commentType) {
			this.commentType = Optional.of(commentType);
		}

		public void setVariableName(String variableName) {
			this.variableName = Optional.of(variableName);
		}

		public void test() {
			for (Function<String, ParsedQueryPart> creator : creators) {
				ParsedQueryPart part = creator.apply(content);

				assertEquals(commentType.isPresent(), part.isComment());
				assertEquals(variableName.isPresent(), part.isNamedVariable());
				assertEquals(false, part.isAnonymousVariable());
				assertEquals(!(commentType.isPresent() || variableName.isPresent()), part.isSimplePart());
				if (commentType.isPresent()) {
					assertEquals(commentType.get(), part.getCommentType());
				} else {
					try {
						part.getCommentType();
						fail();
					} catch (RuntimeException e) {
					}
				}
				if (comment.isPresent()) {
					assertEquals(comment.get(), part.getCommentContent());
				} else {
					try {
						part.getCommentContent();
						fail();
					} catch (RuntimeException e) {
					}
				}
				if (variableName.isPresent()) {
					assertEquals(variableName.get(), part.getVariableName());
				} else {
					try {
						part.getVariableName();
						fail();
					} catch (RuntimeException e) {
					}
				}
			}
		}
	}
}
