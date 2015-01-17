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
package com.github.philippn.springremotingautoconfigure.client.annotation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.github.philippn.springremotingautoconfigure.annotation.RemoteExport;

/**
 * @author Philipp Nanz
 */
@Configuration
public class HttpInvokerProxyFactoryBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

	final static Logger logger = LoggerFactory.getLogger(HttpInvokerProxyFactoryBeanRegistrar.class);

	@Autowired
	private HttpInvokerRequestExecutor httpInvokerRequestExecutor;
	@Value("${remote.baseUrl}")
	private String baseUrl = "http://localhost:8080";

	/**
	 * @return the httpInvokerRequestExecutor
	 */
	public HttpInvokerRequestExecutor getHttpInvokerRequestExecutor() {
		return httpInvokerRequestExecutor;
	}

	/**
	 * @param httpInvokerRequestExecutor the httpInvokerRequestExecutor to set
	 */
	public void setHttpInvokerRequestExecutor(
			HttpInvokerRequestExecutor httpInvokerRequestExecutor) {
		this.httpInvokerRequestExecutor = httpInvokerRequestExecutor;
	}

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @param baseUrl the baseUrl to set
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

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
		
		Set<String> basePackages = new HashSet<>();
		for (String beanName : registry.getBeanDefinitionNames()) {
			BeanDefinition definition = registry.getBeanDefinition(beanName);
			if (definition.getBeanClassName() != null) {
				try {
					Class<?> resolvedClass = ClassUtils.forName(definition.getBeanClassName(), null);
					EnableHttpInvokerAutoProxy autoProxy = 
							AnnotationUtils.findAnnotation(resolvedClass, EnableHttpInvokerAutoProxy.class);
					if (autoProxy != null) {
						if (autoProxy.basePackages().length > 0) {
							Collections.addAll(basePackages, autoProxy.basePackages());
						} else {
							basePackages.add(resolvedClass.getPackage().getName());
						}
					}
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Unable to inspect class " + 
							definition.getBeanClassName() + " for @EnableHttpInvokerAutoProxy annotations");
				}
			}
		}
		
		ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(false) {

					/* (non-Javadoc)
					 * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition)
					 */
					@Override
					protected boolean isCandidateComponent(
							AnnotatedBeanDefinition beanDefinition) {
						return beanDefinition.getMetadata().isInterface() && 
								beanDefinition.getMetadata().isIndependent();
					}
		};
		scanner.addIncludeFilter(new AnnotationTypeFilter(RemoteExport.class));
		
		for (String basePackage : basePackages) {
			for (BeanDefinition definition : scanner.findCandidateComponents(basePackage)) {
				if (definition.getBeanClassName() != null) {
					try {
						Class<?> resolvedClass = ClassUtils.forName(definition.getBeanClassName(), null);
						setupProxy(resolvedClass, registry);
					} catch (ClassNotFoundException e) {
						throw new IllegalStateException("Unable to inspect class " + 
								definition.getBeanClassName() + " for @RemoteExport annotations");
					}
				}
			}	
		}
	}

	protected void setupProxy(Class<?> clazz, BeanDefinitionRegistry registry) {
		Assert.isTrue(clazz.isInterface(), "Annotation @RemoteExport may only be used on interfaces");
		
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(HttpInvokerProxyFactoryBean.class)
				.addPropertyValue("serviceInterface", clazz)
				.addPropertyValue("serviceUrl", getBaseUrl() + getMappingPath(clazz));
		
		registry.registerBeanDefinition(clazz.getSimpleName() + "Proxy", 
				builder.getBeanDefinition());
		
		logger.info("Created HttpInvokerProxyFactoryBean for " + clazz.getSimpleName());	
	}

	protected String getMappingPath(Class<?> clazz) {
		RemoteExport definition = AnnotationUtils.findAnnotation(clazz, RemoteExport.class);
		if (definition.mappingPath().length() > 0) {
			return definition.mappingPath();
		}
		return "/" + clazz.getSimpleName();
	}
}
