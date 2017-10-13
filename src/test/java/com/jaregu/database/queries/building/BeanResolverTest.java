package com.jaregu.database.queries.building;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.jaregu.database.queries.building.help.BeanResolverHelpClass;

@RunWith(MockitoJUnitRunner.class)
public class BeanResolverTest {

	@Before
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
