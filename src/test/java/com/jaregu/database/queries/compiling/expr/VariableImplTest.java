package com.jaregu.database.queries.compiling.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.jaregu.database.queries.building.NamedResolver;
import com.jaregu.database.queries.building.ParametersResolver;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VariableImplTest {

	private VariableImpl impl = new VariableImpl("someName");

	@Mock
	private ParametersResolver variableResolver;

	@Mock
	private NamedResolver namedResolver;

	private EvaluationContext context;

	@BeforeEach
	public void setUp() {
		context = EvaluationContext.forVariableResolver(variableResolver).build();
		when(variableResolver.toNamed()).thenReturn(namedResolver);
	}

	@Test
	public void testName() {
		assertEquals("someName", impl.getName());
	}

	@Test
	public void testEvaluationWithoutContext() {
		assertThrows(ExpressionEvalException.class, () -> impl.getValue());
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
