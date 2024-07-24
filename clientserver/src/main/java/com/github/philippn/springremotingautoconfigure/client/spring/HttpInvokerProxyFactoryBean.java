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

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.github.philippn.springremotingautoconfigure.cbor.CborMapperFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class HttpInvokerProxyFactoryBean implements EnvironmentAware, FactoryBean<Object>, InitializingBean, MethodInterceptor {

    private Object serviceProxy;
    private Class<?> serviceInterface;
    private String serviceUrl;
    private String absoluteServiceUrl;
    private Environment environment;
    private CloseableHttpClient httpClient;
    private CborMapperFactory cborMapperFactory;
    private CBORMapper cborMapper;
    private CBORFactory cborFactory;

    @Override
    public void afterPropertiesSet() {
        if (getServiceUrl() == null) {
            throw new IllegalArgumentException("Property 'serviceUrl' is required");
        }
        Class<?> ifc = getServiceInterface();
        Assert.notNull(ifc, "Property 'serviceInterface' is required");
        this.cborMapper = cborMapperFactory.newMapper();
        this.cborFactory = new CBORFactory(cborMapper);
        this.serviceProxy = new ProxyFactory(ifc, this).getProxy();
        this.absoluteServiceUrl = buildServiceUrl();
    }

    protected String buildServiceUrl() {
        String baseUrl = environment.getProperty("remote.baseUrl", "http://localhost:8080");
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + serviceUrl;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        HttpPost post = new HttpPost(absoluteServiceUrl);
        post.setEntity(new MethodInvocationEntity(invocation, cborFactory));
        try {
            return httpClient.execute(post, new MethodInvocationResponseHandler(invocation, cborFactory, cborMapper.getTypeFactory()));
        } catch (MethodInvocationException e) {
            throw e.getCause();
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @return the httpClient
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * @param httpClient the httpClient to set
     */
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * @return the cborMapperFactory
     */
    public CborMapperFactory getCborMapperFactory() {
        return cborMapperFactory;
    }

    /**
     * @param cborMapperFactory the cborMapperFactory to set
     */
    public void setCborMapperFactory(CborMapperFactory cborMapperFactory) {
        this.cborMapperFactory = cborMapperFactory;
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