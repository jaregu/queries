package com.jaregu.database.queries.building.help;

import com.jaregu.database.queries.building.BeanResolverTest.SomeFooBarInterface;

public class BeanResolverHelpClass {

	public static SomeFooBarInterface build() {
		return new PrivateImplementation();
	}

	private static class PrivateImplementation implements SomeFooBarInterface {

		/**
		 * Method is available from public interface
		 */
		@Override
		public String getFoo() {
			return "foo-result";
		}

		/**
		 * Public method without any interface to expose it
		 * 
		 * @return
		 */
		public String getPublicBar() {
			return "public-bar";
		}

		protected String getProtectedBar() {
			return "protected-bar";
		}

		private String getPrivateBar() {
			return "private-bar";
		}
	}
}
