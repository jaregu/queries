package com.jaregu.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ApplicationMode {

	DEVELOPEMENT, PRODUCTION;

	private static final Logger log = LoggerFactory.getLogger(ApplicationMode.class);

	private static final ApplicationMode CURRENT = getMode();

	private static ApplicationMode getMode() {
		ApplicationMode mode;
		String modeProp = System.getProperty("jaregu.applicationMode");
		if (modeProp != null && modeProp.toLowerCase().startsWith("prod")) {
			mode = PRODUCTION;
		} else {
			mode = DEVELOPEMENT;
		}
		log.info(
				"Application mode: {}. To change it supply system property jaregu.applicationMode. Example: -Djaregu.applicationMode=\"prod\"",
				mode);
		return mode;
	}

	public static ApplicationMode getCurrent() {
		return CURRENT;
	}

	public static boolean isProduction() {
		return CURRENT == PRODUCTION;
	}

	public static boolean isDevelopment() {
		return CURRENT == DEVELOPEMENT;
	}
}
