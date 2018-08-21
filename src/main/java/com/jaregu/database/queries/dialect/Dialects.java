package com.jaregu.database.queries.dialect;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.jaregu.database.queries.QueryException;
import com.jaregu.database.queries.building.Query;

/**
 * Built-in queries dialects selection see {@link #dialect(Dialect)}
 */
public interface Dialects<T extends Dialects<?>> {

	/**
	 * Sets used database dialect. Use one of <code>dialect...</code> methods to
	 * set dialect from built-in dialects.
	 * <p>
	 * Dialect is used to convert some existing query to other query with some
	 * additions like <code>LIMIT ? OFFSET ?</code> clause
	 * 
	 * See {@link Query} class <code>to...()</code> methods for all available
	 * conversions like {@link Query#toPagedQuery(Integer, Integer)}.
	 * 
	 * @param dialect
	 * @return
	 */
	T dialect(Dialect dialect);

	/**
	 * Sets dialect from built-ins using data source database product info. Use
	 * one of <code>dialect...</code> methods to set dialect explicitly from
	 * built-in dialects.
	 * 
	 * @param dataSource
	 * @return
	 */
	default T detectDialect(DataSource dataSource) {
		try (Connection connection = dataSource.getConnection()) {
			return detectDialect(connection);
		} catch (RuntimeException e) {
			throw e;
		} catch (SQLException e) {
			throw new QueryException("Failed detect database dialect: " + e, e);
		}
	}

	default T detectDialect(Connection connection) {
		try {
			String productName = connection.getMetaData().getDatabaseProductName();
			int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
			switch (productName) {
			case "Oracle":
				if (majorVersion < 12)
					throw new QueryException("Oracle version " + majorVersion + " is not supported!");
				return dialectOracle12Plus();
			case "Microsoft SQL Server":
				if (majorVersion < 11)
					throw new QueryException("MSSQL version " + majorVersion + " is not supported!");
				return dialectMicrosoftSQLServer2012Plus();
			case "MySQL":
				return dialectMySQL();
			case "PostgreSQL":
				return dialectPostgreSQL();
			case "HSQL Database Engine":
				return dialectHSQLDB();
			case "H2":
				return dialectH2();
			default:
				return dialectDefault();
			}
		} catch (SQLException e) {
			throw new QueryException("Failed detect database dialect: " + e, e);
		}
	}

	default T dialectDefault() {
		return dialect(defaultDialect());
	}

	default T dialectH2() {
		return dialect(h2());
	}

	default T dialectHSQLDB() {
		return dialect(hsqldb());
	}

	default T dialectMicrosoftSQLServer2012Plus() {
		return dialect(microsoftSQLServer2012Plus());
	}

	default T dialectMySQL() {
		return dialect(mySQL());
	}

	default T dialectOracle12Plus() {
		return dialect(oracle12Plus());
	}

	default T dialectPostgreSQL() {
		return dialect(postgreSQL());
	}

	static Dialect defaultDialect() {
		return new DefaultDialectImpl();
	}

	static Dialect h2() {
		return new H2Dialect();
	}

	static Dialect hsqldb() {
		return new HSQLDBDialect();
	}

	static Dialect microsoftSQLServer2012Plus() {
		return new MicrosoftSQLServer2012PlusDialect();
	}

	static Dialect mySQL() {
		return new MySQLDialect();
	}

	static Dialect oracle12Plus() {
		return new Oracle12PlusDialect();
	}

	static Dialect postgreSQL() {
		return new PostgreSQLDialect();
	}
}
