package com.jaregu.database.queries.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.jaregu.database.queries.QueryId;
import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.common.StreamReader;

public interface QueriesSource {

	SourceId getId();

	QueryId getQueryId(String id);

	String getContent();

	public static QueriesSource ofContent(SourceId sourceId, Supplier<String> contentSupplier) {
		return new QueriesSourceImpl(sourceId, contentSupplier);
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
	 * @param clazz
	 * @return
	 */
	public static QueriesSource ofClass(Class<?> clazz) {
		return new QueriesSourceImpl(SourceId.ofClass(clazz), () -> {
			String resourceName = clazz.getSimpleName() + ".sql";
			try (InputStream inputStream = clazz.getResourceAsStream(resourceName)) {
				if (inputStream == null) {
					throw new QueryParseException("Can't find resource with name: " + resourceName);
				}
				return StreamReader.toUtf8String(inputStream);
			} catch (IOException exc) {
				throw new QueryParseException("Error while reading from resource: " + resourceName, exc);
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
	 * replaced with dot name. See {@link SourceId#ofPath(String)}.
	 * 
	 * @param resourcePath
	 * @return
	 */
	public static QueriesSource ofResource(String path) {
		return new QueriesSourceImpl(SourceId.ofResource(path), () -> {
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
