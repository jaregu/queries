package com.jaregu.database.queries.compiling.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Test;

public class VariableTest {

	@Test
	public void testVariableParse() {
		assertTrue(Variable.parse(":aaa").get() instanceof Variable);
		assertEquals("aaa", Variable.parse(":aaa").get().getName());

		assertTrue(Variable.parse(":aaa.bbb").get() instanceof Variable);
		assertEquals("aaa.bbb", Variable.parse(":aaa.bbb").get().getName());
	}

	@Test
	public void testNonParsable() {
		assertEquals(Optional.empty(), Variable.parse("1231333"));
		assertEquals(Optional.empty(), Variable.parse("asasas"));
		assertEquals(Optional.empty(), Variable.parse("'asdas'"));
		assertEquals(Optional.empty(), Variable.parse("123abc123"));
	}

	@Test
	public void testParseException() {
		try {
			Variable.parse(":asdasd.");
			fail();
		} catch (ExpressionParseException e) {
		}

		try {
			Variable.parse(":asdasd..aaa");
			fail();
		} catch (ExpressionParseException e) {
		}

		try {
			Variable.parse(":.aasdasd");
			fail();
		} catch (ExpressionParseException e) {
		}
	}
}
