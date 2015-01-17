/*
 * Copyright (C) 2015 Philipp Nanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.github.philippn.springremotingautoconfigure.server.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.github.philippn.springremotingautoconfigure.annotation.RemoteExport;

/**
 * @author Philipp Nanz
 */
@Configuration
public class HttpInvokerServiceExporterRegistrar implements BeanDefinitionRegistryPostProcessor {

	final static Logger logger = LoggerFactory.getLogger(HttpInvokerServiceExporterRegistrar.class);

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// Nadda
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(
			BeanDefinitionRegistry registry) throws BeansException {
		for (String beanName : registry.getBeanDefinitionNames()) {
			BeanDefinition definition = registry.getBeanDefinition(beanName);
			if (definition.getBeanClassName() != null) {
				try {
					Class<?> resolvedClass = ClassUtils.forName(definition.getBeanClassName(), null);
					Class<?>[] beanInterfaces = resolvedClass.getInterfaces();
					for (Class<?> clazz : beanInterfaces) {
						if (AnnotationUtils.isAnnotationDeclaredLocally(RemoteExport.class, clazz)) {
							setupExport(clazz, beanName, registry);
						}
					}
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Unable to inspect class " + 
							definition.getBeanClassName() + " for @RemoteExport annotations");
				}
			}
		}
	}

	private void setupExport(Class<?> clazz, String beanName,
			BeanDefinitionRegistry registry) {
		Assert.isTrue(clazz.isInterface(), "Annotation @RemoteExport may only be used on interfaces");
		
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(HttpInvokerServiceExporter.class)
				.addPropertyReference("service", beanName)
				.addPropertyValue("serviceInterface", clazz);
		
		String mappingPath = getMappingPath(clazz);
		
		registry.registerBeanDefinition(mappingPath, builder.getBeanDefinition());
		
		logger.info("Mapping HttpInvokerServiceExporter for " + clazz.getSimpleName() + " to [" + mappingPath + "]");
	}

	protected String getMappingPath(Class<?> clazz) {
		RemoteExport definition = AnnotationUtils.findAnnotation(clazz, RemoteExport.class);
		if (definition.mappingPath().length() > 0) {
			return definition.mappingPath();
		}
		return "/" + clazz.getSimpleName();
	}
}
