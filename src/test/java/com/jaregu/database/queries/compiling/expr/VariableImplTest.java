package com.jaregu.database.queries.compiling.expr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParametersResolver;

@RunWith(MockitoJUnitRunner.class)
public class VariableImplTest {

	private VariableImpl impl = new VariableImpl("someName");

	@Mock
	private ParametersResolver variableResolver;

	@Mock
	private NamedResolver namedResolver;

	private EvaluationContext context;

	@Before
	public void setUp() {
		context = EvaluationContext.forVariableResolver(variableResolver).build();
		when(variableResolver.toNamed()).thenReturn(namedResolver);
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
		when(namedResolver.getValue("someName")).thenReturn("someValue");
		String result = (String) EvaluationContext.withContext(context, () -> {
			return impl.getValue();
		});
		assertEquals("someValue", result);
	}
}
