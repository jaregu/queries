package com.jaregu.database.queries;

public class QueriesConfigImpl implements QueriesConfig {

	final static QueriesConfigImpl DEFAULT_INSTANCE = new QueriesConfigImpl(false);

	final private boolean isOriginalArgumentCommented;

	private QueriesConfigImpl(boolean isOriginalArgumentCommented) {
		this.isOriginalArgumentCommented = isOriginalArgumentCommented;
	}

	@Override
	public boolean isOriginalArgumentCommented() {
		return isOriginalArgumentCommented;
	}

	/**
	 * Returns a configuration instance that is equivalent to {@code this}, but
	 * with changed one setting
	 * 
	 * @param isOriginalArgumentCommented
	 * @return
	 */
	public QueriesConfigImpl setOriginalArgumentCommented(boolean isOriginalArgumentCommented) {
		return new QueriesConfigImpl(isOriginalArgumentCommented);
	}

	public static QueriesConfigImpl getDefault() {
		return DEFAULT_INSTANCE;
	}
}
