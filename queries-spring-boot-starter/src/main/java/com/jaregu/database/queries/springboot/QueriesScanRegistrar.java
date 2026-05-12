package com.jaregu.database.queries.springboot;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.jaregu.database.queries.parsing.QueriesSource;
import com.jaregu.database.queries.proxy.QueriesSourceClass;

/**
 * Registers a {@link QueriesProxyFactoryBean} and a {@link QueriesSource} bean
 * for every {@link QueriesSourceClass}-annotated interface found in the
 * packages configured on {@link QueriesScan}.
 */
public class QueriesScanRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String QUERIES_SCAN_ANNOTATION = QueriesScan.class.getName();

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes attrs = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(QUERIES_SCAN_ANNOTATION));
		if (attrs == null) {
			return;
		}

		Set<String> basePackages = resolveBasePackages(attrs, importingClassMetadata);
		if (basePackages.isEmpty()) {
			return;
		}

		ClassPathScanningCandidateComponentProvider scanner = new InterfaceComponentProvider();
		scanner.addIncludeFilter(new AnnotationTypeFilter(QueriesSourceClass.class));

		ClassLoader classLoader = QueriesScanRegistrar.class.getClassLoader();

		for (String basePackage : basePackages) {
			for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
				String className = candidate.getBeanClassName();
				if (className == null) {
					continue;
				}
				Class<?> daoInterface = loadClass(classLoader, className);
				registerProxyFactoryBean(registry, daoInterface);
				registerQueriesSourceBean(registry, daoInterface);
			}
		}
	}

	private static void registerProxyFactoryBean(BeanDefinitionRegistry registry, Class<?> daoInterface) {
		String beanName = StringUtils.uncapitalize(daoInterface.getSimpleName());
		if (registry.containsBeanDefinition(beanName)) {
			return;
		}
		AbstractBeanDefinition bd = BeanDefinitionBuilder
				.genericBeanDefinition(QueriesProxyFactoryBean.class)
				.addConstructorArgValue(daoInterface)
				.getBeanDefinition();
		// FactoryBean<T> — expose the target interface so injection by type
		// resolves to the proxied DAO rather than to QueriesProxyFactoryBean.
		bd.setAttribute("factoryBeanObjectType", daoInterface);
		registry.registerBeanDefinition(beanName, bd);
	}

	private static void registerQueriesSourceBean(BeanDefinitionRegistry registry, Class<?> daoInterface) {
		String beanName = "queriesSource$" + daoInterface.getName().replace('.', '$');
		if (registry.containsBeanDefinition(beanName)) {
			return;
		}
		AbstractBeanDefinition bd = BeanDefinitionBuilder
				.genericBeanDefinition(QueriesSource.class, () -> QueriesSource.ofClass(daoInterface))
				.getBeanDefinition();
		registry.registerBeanDefinition(beanName, bd);
	}

	private static Set<String> resolveBasePackages(AnnotationAttributes attrs, AnnotationMetadata metadata) {
		Set<String> packages = new LinkedHashSet<>();
		packages.addAll(Arrays.asList(attrs.getStringArray("basePackages")));
		for (Class<?> clazz : attrs.getClassArray("basePackageClasses")) {
			packages.add(ClassUtils.getPackageName(clazz));
		}
		if (packages.isEmpty()) {
			// Fall back to the package of the @QueriesScan-annotated class.
			packages.add(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return packages;
	}

	private static Class<?> loadClass(ClassLoader classLoader, String className) {
		try {
			return ClassUtils.forName(className, classLoader);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot load @QueriesSourceClass candidate: " + className, e);
		}
	}

	/**
	 * Scanner variant that accepts interfaces (the default
	 * {@link ClassPathScanningCandidateComponentProvider} rejects them because
	 * they aren't independent components in the bean sense).
	 */
	private static final class InterfaceComponentProvider extends ClassPathScanningCandidateComponentProvider {

		InterfaceComponentProvider() {
			super(false);
		}

		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			AnnotationMetadata metadata = beanDefinition.getMetadata();
			return metadata.isIndependent() && metadata.isInterface();
		}
	}
}
