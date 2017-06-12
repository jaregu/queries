package com.jaregu.database.queries.compiling.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.ParamsResolver;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionParserImplTest {

	private ExpressionParserImpl impl = new ExpressionParserImpl();

	@Mock
	private ParamsResolver variableResolver;

	@Before
	public void setUp() {
		when(variableResolver.getValue("aaa")).thenReturn("AAA");
		when(variableResolver.getValue("N0")).thenReturn(0);
		when(variableResolver.getValue("N123")).thenReturn(123);
		when(variableResolver.getValue("N123456")).thenReturn(123.456);
		when(variableResolver.getValue("bT")).thenReturn(true);
		when(variableResolver.getValue("bF")).thenReturn(false);
	}

	@Test
	public void testIsLikeExpression() {
		isLike("aaa");
		isLike(".aaa");
		isLike("+");
		isLike("<=");
		isLike("<=   aa < bbb");
		isLike("1+2+3");
		isLike("'1 '+'2 3 4'+' 3'");
		isLike("1+'aaa '' bbb'+3");
		isLike("(aaa)");
		isLike(" (1)  + (2) *  (3)   ");
		isLike(" (1)  + (2 + (1 + 2 + 3) + (1 +4 + (6 + 7))) *  (3)   ");
		isLike("()()(())((()())())");
		isNotLike("1 + b c + 2");
	}

	private void isLike(String expression) {
		assertTrue(impl.isLikeExpression(expression));
	}

	private void isNotLike(String expression) {
		assertFalse(impl.isLikeExpression(expression));
	}

	@Test
	public void testExpressions() {
		eval(123l, "123");
		eval(null, " null");
		eval(true, " true ");
		eval(false, "FALSE  ");
		eval(new BigDecimal("123.456"), " 123.456 ");
		eval("abc", "'abc'");
		eval("abc abc", "'abc abc'");
		eval("abc ' abc ", " 'abc '' abc '  ");
		eval("AAA", ".aaa");
		eval(0, ".N0");
		eval(123, ".N123");
		eval(123.456, ".N123456");
		eval(true, ".bT");
		eval(false, ".bF");

		eval(3l, "1+2");
		eval(6l, " 1+2+ 3   ");
		eval(7l, "1 + 2 *   3");
		eval(7l, " (1)  + (2) * \n (3)  \n \n");
		eval(9l, "(1+ 2  )* 3");
		eval(79l, " (1)  + (2 + (1 + 1 * 1 * 2 + 3) + (1 +4 + (6 + 7))) *  (3)   ");

		eval("1 2 3 4 5", "'1 '+'2 3 4'+' 5'");
		eval("%AAA%", "'%'+ .aaa +'%'");

		eval(false, "1 >2");
		eval(true, "1 <2");
		eval(true, "1 <=2");
		eval(true, "2 <=2");
		eval(false, "2< 2");
		eval(true, "true && true");
		eval(false, "true && false");
		eval(true, "true || true");
		eval(true, "true || false");
		eval(false, "false || false");
		eval(true, "2< 2 || 4 >2");
		eval(false, "2< 2 || 4 <=2");
		eval(false, "true && true && true && false");
		eval(true, "true && true || true && false");
		eval(false, "true && (true || true) && false");
		eval(false, "(1 > 2 || 2 <=2) && (true && .bF)");
	}

	private void eval(Object result, String expression) {
		assertEquals(result, impl.parse(expression).eval(variableResolver));
	}

	@Test
	public void testParseException() {
		testException("aaa");
		testException("+");
		testException("'a'+");
		testException("<=   aa < bbb");
		testException("(1+2 + (3+4) + 5");
	}

	private void testException(String expression) {
		try {
			impl.parse(expression);
			fail();
		} catch (ExpressionParseException e) {
		}
	}
}
