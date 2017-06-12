package com.jaregu.database.queries.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.jaregu.database.queries.SourceId;
import com.jaregu.database.queries.common.StreamReader;

public interface QueriesSource {

	SourceId getSourceId();

	String getContent();

	public static QueriesSource of(SourceId sourceId, Supplier<String> contentSupplier) {
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
	 * {@link SourceId#of(Class)}.
	 * 
	 * @param clazz
	 * @return
	 */
	public static QueriesSource of(Class<?> clazz) {
		return new QueriesSourceImpl(SourceId.of(clazz), () -> {
			String resourceName = clazz.getSimpleName() + ".sql";
			try (InputStream inputStream = clazz.getResourceAsStream(resourceName)) {
				if (inputStream == null) {
					throw new QueriesParseException("Can't find resource with name: " + resourceName);
				}
				return StreamReader.toUtf8String(inputStream);
			} catch (IOException exc) {
				throw new QueriesParseException("Error while reading from resource: " + resourceName, exc);
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
	public static QueriesSource of(String path) {
		return new QueriesSourceImpl(SourceId.ofPath(path), () -> {
			try (InputStream inputStream = QueriesSource.class.getClassLoader().getResourceAsStream(path)) {
				if (inputStream == null) {
					throw new QueriesParseException("Can't find resource with path: " + path);
				}
				return StreamReader.toUtf8String(inputStream);
			} catch (IOException exc) {
				throw new QueriesParseException("Error while reading from resource: " + path, exc);
			}
		});
	}
}
