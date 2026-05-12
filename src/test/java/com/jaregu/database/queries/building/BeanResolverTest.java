package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.jaregu.database.queries.building.help.BeanResolverHelpClass;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BeanResolverTest {

	@BeforeEach
	public void setUp() {
	}

	@Test
	public void testSomePrivateImplementationAccess() {

		BeanResolver resolver = new BeanResolver(BeanResolverHelpClass.build());
		assertThat(resolver.getValue("foo")).isEqualTo("foo-result");
		assertThat(resolver.getValue("publicBar")).isEqualTo("public-bar");
		assertThatThrownBy(() -> resolver.getValue("protectedBar")).isInstanceOf(QueryBuildException.class);
		assertThatThrownBy(() -> resolver.getValue("privateBar")).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testBeanParameters() {

		BeanResolver resolver = new BeanResolver(new SomeBase());
		assertThat(resolver.getValue("aaa")).isEqualTo(123);
		assertThat(resolver.getValue("bbb")).isEqualTo("string value");
		assertThat(resolver.getValue("ccc")).isEqualTo(1234);
		assertThat(resolver.getValue("ddd")).isInstanceOf(SomeChild.class);
		assertThat(resolver.getValue("eee")).isNull();

		assertThat(resolver.getValue("ddd.aaa")).isEqualTo((byte) 11);
		assertThat(resolver.getValue("ddd.bbb")).isEqualTo("child string");
		assertThat(resolver.getValue("ddd.ccc")).isEqualTo("some protected field");
		assertThat(resolver.getValue("ddd.ddd")).isEqualTo(123d);
		assertThatThrownBy(() -> resolver.getValue("ddd.notVisible")).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testRecordStyleAccessor() {
		// Java 17 records expose components via `name()` accessors with no
		// get/is prefix. BeanResolver must resolve them.
		BeanResolver resolver = new BeanResolver(new SomeRecord(1, "alpha", null));
		assertThat(resolver.getValue("id")).isEqualTo(1);
		assertThat(resolver.getValue("label")).isEqualTo("alpha");
		assertThat(resolver.getValue("nested")).isNull();
		assertThatThrownBy(() -> resolver.getValue("missing")).isInstanceOf(QueryBuildException.class);
	}

	@Test
	public void testNestedRecordAccess() {
		SomeRecord inner = new SomeRecord(2, "inner", null);
		SomeRecord outer = new SomeRecord(1, "outer", inner);
		BeanResolver resolver = new BeanResolver(outer);
		assertThat(resolver.getValue("nested.id")).isEqualTo(2);
		assertThat(resolver.getValue("nested.label")).isEqualTo("inner");
	}

	@Test
	public void testRepeatedLookupsAreStable() {
		// Each getValue() call must return the live value of the underlying
		// bean — caching of reflection metadata must not freeze the value.
		MutableBean bean = new MutableBean();
		BeanResolver resolver = new BeanResolver(bean);
		bean.setCounter(1);
		assertThat(resolver.getValue("counter")).isEqualTo(1);
		bean.setCounter(2);
		assertThat(resolver.getValue("counter")).isEqualTo(2);
		bean.setCounter(99);
		assertThat(resolver.getValue("counter")).isEqualTo(99);
	}

	@Test
	public void testInheritedGetterIsResolved() {
		BeanResolver resolver = new BeanResolver(new SubBean());
		assertThat(resolver.getValue("aaa")).isEqualTo(123);
		assertThat(resolver.getValue("subOnly")).isEqualTo("sub-value");
	}

	public record SomeRecord(Integer id, String label, SomeRecord nested) {
	}

	public static class MutableBean {
		private int counter;

		public int getCounter() {
			return counter;
		}

		public void setCounter(int counter) {
			this.counter = counter;
		}
	}

	public static class SubBean extends SomeBase {
		public String getSubOnly() {
			return "sub-value";
		}
	}

	public interface SomeFooBarInterface {

		public String getFoo();
	}

	public static class SomeBase {

		public int aaa = 123;
		private String bbb = "string value";
		private Integer ccc = 1234;
		private SomeChild ddd = new SomeChild();
		public SomeChild eee = null;

		public String getBbb() {
			return bbb;
		}

		public Integer getCcc() {
			return ccc;
		}

		public SomeChild getDdd() {
			return ddd;
		}
	}

	public static class SomeChild {

		private byte aaa = 11;
		private String bbb = "child string";
		protected String ccc = "some protected field";
		public Number ddd = 123d;
		private long notVisible = 1;

		public byte getAaa() {
			return aaa;
		}

		public String getBbb() {
			return bbb;
		}

		public String getCcc() {
			return ccc;
		}

		public void incNotVisible() {
			this.notVisible = this.notVisible + 1;
		}
	}
}
