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
		@SuppressWarnings("unused")
		public String getPublicBar() {
			return "public-bar";
		}

		@SuppressWarnings("unused")
		protected String getProtectedBar() {
			return "protected-bar";
		}

		@SuppressWarnings("unused")
		private String getPrivateBar() {
			return "private-bar";
		}
	}
}
