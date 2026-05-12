package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jaregu.database.queries.building.ParameterBinder.Result;

public class ParameterBinderDefaultImplTest {

	private ParameterBinderDefaultImpl binder;

	@BeforeEach
	public void setUp() {
		binder = new ParameterBinderDefaultImpl();
	}

	@Test
	public void testProccessNull() {
		Result result = binder.process(null);
		assertThat(result.getSql()).isEqualTo("?");
		assertThat(result.getParameters()).containsExactly((Object) null);
	}

	@Test
	public void testProccess() {
		Result result = binder.process("abc");
		assertThat(result.getSql()).isEqualTo("?");
		assertThat(result.getParameters()).containsExactly("abc");
	}
}
