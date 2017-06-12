package com.jaregu.database.queries.compiling.expr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.ParamsResolver;

@RunWith(MockitoJUnitRunner.class)
public class VariableImplTest {

	private VariableImpl impl = new VariableImpl("someName");

	@Mock
	private ParamsResolver variableResolver;

	private EvaluationContext context;

	@Before
	public void setUp() {
		context = EvaluationContext.forVariableResolver(variableResolver).build();
	}

	@Test
	public void testName() {
		assertEquals("someName", impl.getName());
	}

	@Test(expected = ExpressionEvalException.class)
	public void testEvaluationWithoutContext() {
		impl.getValue();
	}

	@Test
	public void testEvaluation() {
		when(variableResolver.getValue("someName")).thenReturn("someValue");
		String result = (String) EvaluationContext.withContext(context, () -> {
			return impl.getValue();
		});
		assertEquals("someValue", result);
	}
}
