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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.IOException;

public class MethodInvocationResponseHandler implements HttpClientResponseHandler<Object> {

    private final MethodInvocation methodInvocation;
    private final CBORFactory cborFactory;
    private final TypeFactory typeFactory;

    public MethodInvocationResponseHandler(MethodInvocation methodInvocation, CBORFactory cborFactory, TypeFactory typeFactory) {
        this.methodInvocation = methodInvocation;
        this.cborFactory = cborFactory;
        this.typeFactory = typeFactory;
    }

    @Override
    public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
        if (response.getCode() != 200) {
            throw new IllegalStateException(String.format("HTTP request failed: %s", response.getCode()));
        }
        if (Void.class.equals(methodInvocation.getMethod().getReturnType())) {
            return null;
        }
        try (CBORParser input = cborFactory.createParser(response.getEntity().getContent())) {
            input.nextToken();
            input.nextToken();
            boolean success = input.getValueAsBoolean();
            if (success) {
                input.nextToken();
                JavaType retType = typeFactory.constructType(methodInvocation.getMethod().getGenericReturnType());
                return input.getCodec().readValue(input, retType);
            } else {
                input.nextToken();
                String exceptionClassName = input.getValueAsString();
                try {
                    Class<?> exceptionClass = Class.forName(exceptionClassName);
                    input.nextToken();
                    throw new MethodInvocationException((Throwable) input.readValueAs(exceptionClass));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(String.format("Exception could not be deserialized: %s", exceptionClassName));
                }
            }
        }
    }
}
