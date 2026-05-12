package com.jaregu.database.queries.springboot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.jaregu.database.queries.proxy.QueriesSourceClass;

/**
 * Scans the supplied packages for interfaces annotated with
 * {@link QueriesSourceClass} and, for each one, registers:
 * <ul>
 *   <li>a {@code QueriesSource} bean derived from the interface (so the
 *       {@link QueriesAutoConfiguration#queries Queries} bean picks it up)</li>
 *   <li>a {@code FactoryBean<T>} that returns
 *       {@code queries.proxy(T.class)} — so the DAO interface is injectable
 *       directly anywhere in the application</li>
 * </ul>
 *
 * <p>Typical usage on the application class:
 *
 * <pre>{@code
 * @SpringBootApplication
 * @QueriesScan(basePackageClasses = JobDAO.class)
 * public class App { ... }
 * }</pre>
 *
 * <p>If neither {@link #basePackages} nor {@link #basePackageClasses} is set,
 * the package of the annotated class is used.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(QueriesScanRegistrar.class)
public @interface QueriesScan {

	/**
	 * Base packages to scan for {@link QueriesSourceClass}-annotated
	 * interfaces. Mutually inclusive with {@link #basePackageClasses}.
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages}. The packages of each
	 * listed class are scanned.
	 */
	Class<?>[] basePackageClasses() default {};
}
