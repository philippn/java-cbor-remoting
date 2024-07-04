/*
 * Copyright (C) 2015-2024 Philipp Nanz
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
package com.github.philippn.springremotingautoconfigure.client.spring;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.github.philippn.springremotingautoconfigure.mixin.ThrowableMixin;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class HttpInvokerProxyFactoryBean implements FactoryBean<Object>, InitializingBean, MethodInterceptor {

	private Object serviceProxy;
	private Class<?> serviceInterface;
	private String serviceUrl;
	private final CloseableHttpClient httpClient = HttpClients.createDefault();
	private final CBORFactory cborFactory = new CBORFactory(CBORMapper.builder()
			.visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
			.addMixIn(Throwable.class, ThrowableMixin.class)
			.findAndAddModules()
			.build());

	@Override
	public void afterPropertiesSet() {
		if (getServiceUrl() == null) {
			throw new IllegalArgumentException("Property 'serviceUrl' is required");
		}
		Class<?> ifc = getServiceInterface();
		Assert.notNull(ifc, "Property 'serviceInterface' is required");
		this.serviceProxy = new ProxyFactory(ifc, this).getProxy();
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		HttpPost post = new HttpPost(serviceUrl);
		post.setEntity(new MethodInvocationEntity(invocation, cborFactory));
		try {
			return httpClient.execute(post, new MethodInvocationResponseHandler(invocation, cborFactory));
		} catch (MethodInvocationException e) {
			throw e.getCause();
		}
	}

	/**
	 * Set the interface of the service to access.
	 * The interface must be suitable for the particular service and remoting strategy.
	 * <p>Typically required to be able to create a suitable service proxy,
	 * but can also be optional if the lookup returns a typed proxy.
	 */
	public void setServiceInterface(Class<?> serviceInterface) {
		Assert.notNull(serviceInterface, "'serviceInterface' must not be null");
		Assert.isTrue(serviceInterface.isInterface(), "'serviceInterface' must be an interface");
		this.serviceInterface = serviceInterface;
	}

	/**
	 * Return the interface of the service to access.
	 */
	public Class<?> getServiceInterface() {
		return this.serviceInterface;
	}

	/**
	 * Set the URL of this remote accessor's target service.
	 * The URL must be compatible with the rules of the particular remoting provider.
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Return the URL of this remote accessor's target service.
	 */
	public String getServiceUrl() {
		return this.serviceUrl;
	}

	@Override
	@Nullable
	public Object getObject() {
		return this.serviceProxy;
	}

	@Override
	public Class<?> getObjectType() {
		return getServiceInterface();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}