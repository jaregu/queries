package com.jaregu.database.queries.compiling.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Test;

public class ConstantTest {

	@Test
	public void testConstantOf() {
		assertTrue(Constant.of("") instanceof ConstantString);
		assertEquals("", Constant.of("").getValue());

		assertTrue(Constant.of("123") instanceof ConstantString);
		assertEquals("123", Constant.of("123").getValue());

		assertTrue(Constant.of(true) instanceof ConstantBoolean);
		assertEquals(true, Constant.of(true).getValue());

		assertTrue(Constant.of(false) instanceof ConstantBoolean);
		assertEquals(false, Constant.of(false).getValue());

		assertTrue(Constant.of((short) 999) instanceof ConstantLong);
		assertEquals(999l, Constant.of((short) 999).getValue());

		assertTrue(Constant.of((byte) 123) instanceof ConstantLong);
		assertEquals(123l, Constant.of((byte) 123).getValue());

		assertTrue(Constant.of(12312312) instanceof ConstantLong);
		assertEquals(12312312l, Constant.of(12312312).getValue());

		assertTrue(Constant.of(12334343243l) instanceof ConstantLong);
		assertEquals(12334343243l, Constant.of(12334343243l).getValue());

		assertTrue(Constant.of(123.23d) instanceof ConstantDecimal);
		assertEquals(new BigDecimal("123.23"), Constant.of(123.23d).getValue());

		assertTrue(Constant.of(123.23f) instanceof ConstantDecimal);
		assertEquals(new BigDecimal("123.23"), Constant.of(123.23f).getValue());

		assertTrue(Constant.of(new BigDecimal("123")) instanceof ConstantDecimal);
		assertEquals(new BigDecimal("123"), Constant.of(new BigDecimal("123")).getValue());

		assertTrue(Constant.of(null) instanceof ConstantNull);
		assertEquals(null, Constant.of(null).getValue());

		assertTrue(Constant.of(this) instanceof ConstantObject);
		assertEquals(this, Constant.of(this).getValue());
	}

	@Test
	public void testConstantParse() {
		assertTrue(Constant.parse("nUll").get() instanceof ConstantNull);
		assertEquals(null, Constant.parse("nUll").get().getValue());

		assertTrue(Constant.parse("''").get() instanceof ConstantString);
		assertEquals("", Constant.parse("''").get().getValue());

		assertTrue(Constant.parse("'123''asd'").get() instanceof ConstantString);
		assertEquals("123'asd", Constant.parse("'123''asd'").get().getValue());

		assertTrue(Constant.parse("trUE").get() instanceof ConstantBoolean);
		assertEquals(Boolean.TRUE, Constant.parse("trUE").get().getValue());

		assertTrue(Constant.parse("falSe").get() instanceof ConstantBoolean);
		assertEquals(Boolean.FALSE, Constant.parse("falSe").get().getValue());

		assertTrue(Constant.parse("falSe").get() instanceof ConstantBoolean);
		assertEquals(Boolean.FALSE, Constant.parse("falSe").get().getValue());

		assertTrue(Constant.parse("123123123").get() instanceof ConstantLong);
		assertEquals(123123123l, Constant.parse("123123123").get().getValue());

		assertTrue(Constant.parse("123.11").get() instanceof ConstantDecimal);
		assertEquals(new BigDecimal("123.11"), Constant.parse("123.11").get().getValue());

		assertTrue(Constant.parse("123.00").get() instanceof ConstantDecimal);
		assertEquals(new BigDecimal("123.00"), Constant.parse("123.00").get().getValue());

		assertTrue(Constant.parse("-123.1333").get() instanceof ConstantDecimal);
		assertEquals(new BigDecimal("-123.1333"), Constant.parse("-123.1333").get().getValue());
	}

	@Test
	public void testNonParsable() {
		assertEquals(Optional.empty(), Constant.parse("123..1333"));
		assertEquals(Optional.empty(), Constant.parse("asasas"));
	}

	@Test(expected = ExpressionParseException.class)
	public void testStringException() {
		Constant.parse("'asdasd");
	}
}
