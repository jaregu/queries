package com.jaregu.database.queries.springboot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.jaregu.database.queries.annotation.Table;
import com.jaregu.database.queries.proxy.QueriesSourceClass;

/**
 * Scans the supplied packages and registers:
 *
 * <ul>
 *   <li>For each {@link QueriesSourceClass}-annotated interface — a
 *       {@code QueriesSource} bean derived from the interface and a
 *       {@code FactoryBean<T>} that produces {@code queries.proxy(T.class)},
 *       so the DAO is injectable anywhere as the plain interface type.</li>
 *   <li>For each {@link Table}-annotated class — a
 *       {@link QueriesEntity} bean (alias = simple class name) so the
 *       {@code entityFieldGenerator} SQL macro can reflect over the class's
 *       {@code @Column} fields. Matches Guice's
 *       {@code QueriesModule.entityModule(Class)} ergonomics.</li>
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
 * the package of the annotated class is used. Need a non-default entity alias?
 * Skip the scan for that class (move it out of the scanned packages) and
 * declare an explicit {@code @Bean QueriesEntity ...} instead, or rely on the
 * default and avoid simple-name collisions across scanned packages.
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
