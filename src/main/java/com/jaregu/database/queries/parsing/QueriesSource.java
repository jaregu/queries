package com.jaregu.database.queries.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

import com.jaregu.database.queries.QueriesConfig;
import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.common.StreamReader;
import com.jaregu.database.queries.dialect.Dialect;
import com.jaregu.database.queries.dialect.Dialects;

/**
 * Implementations represents one SQL statements source. In one source can be
 * multiple statements. Implement this interface or use one of
 * {@link #ofClass(Class)}, {@link #ofResource(String)},
 * {@link #ofContent(SourceId, Supplier)} creator methods.
 *
 */
public interface QueriesSource {

	SourceId getId();

	QueryId getQueryId(String id);

	String readContent(QueriesConfig config);

	/**
	 * Queries SQL source coming from supplier, can be used to load SQL content
	 * from anywhere, not only from classpath. See also {@link #ofClass(Class)}
	 * and {@link #ofResource(String)}
	 * 
	 * @param sourceId
	 * @param contentSupplier
	 * @return
	 */
	public static QueriesSource ofContent(SourceId sourceId, Supplier<String> contentSupplier) {
		return new QueriesSourceImpl(sourceId, (c) -> contentSupplier.get());
	}

	/**
	 * Queries source of system resource where SQL file name is class name with
	 * <i>sql</i> extension. For class <i>aaa.bbb.FooBar</i> it would be file
	 * with name <i>aaa/bbb/FooBar.sql</i>. See
	 * {@link Class#getResource(String)}
	 * 
	 * <p>
	 * Resulting queries source ID will be class name. See
	 * {@link SourceId#ofClass(Class)}.
	 * 
	 * <p>
	 * If there is file aaa/bbb/FooBar.{dialectSufix}.sql where dialectSufix is
	 * configured {@link QueriesConfig#getDialect()}
	 * {@link Dialect#getSuffix()} suffix (for example
	 * aaa/bbb/FooBar.mariadb.sql using {@link Dialects#dialectMariaDB()} ),
	 * then it used instead of default file (without suffix)
	 * 
	 * @param clazz
	 * @return
	 */
	public static QueriesSource ofClass(Class<?> clazz) {
		return new QueriesSourceImpl(SourceId.ofClass(clazz), (c) -> {
			String sufix = Optional.ofNullable(c.getDialect().getSuffix()).map(s -> "." + s).orElse("");
			String dialectResourceName = clazz.getSimpleName() + sufix + ".sql";
			String defaultResourceName = clazz.getSimpleName() + ".sql";

			InputStream inputStream = clazz.getResourceAsStream(dialectResourceName);
			if (inputStream == null) {
				inputStream = clazz.getResourceAsStream(defaultResourceName);
				if (inputStream == null) {
					throw new QueryParseException("Can't find resource with name: " + defaultResourceName);
				}
			}

			try (InputStream in = inputStream) {
				return StreamReader.toUtf8String(in);
			} catch (IOException exc) {
				throw new QueryParseException("Error while reading from resource: " + defaultResourceName, exc);
			}
		});
	}

	/**
	 * Queries source of system resource SQL file. See
	 * {@link ClassLoader#getResource(String)} for more details. Use it with
	 * path like <i>aaa/bbb/someFile.sql</i>
	 * 
	 * <p>
	 * Resulting queries source ID will be path name where path separator
	 * replaced with dot name. See {@link SourceId#ofResource(String)}.
	 * 
	 * @param path
	 * @return
	 */
	public static QueriesSource ofResource(String path) {
		return new QueriesSourceImpl(SourceId.ofResource(path), (c) -> {
			try (InputStream inputStream = QueriesSource.class.getClassLoader().getResourceAsStream(path)) {
				if (inputStream == null) {
					throw new QueryParseException("Can't find resource with path: " + path);
				}
				return StreamReader.toUtf8String(inputStream);
			} catch (IOException exc) {
				throw new QueryParseException("Error while reading from resource: " + path, exc);
			}
		});
	}
}
