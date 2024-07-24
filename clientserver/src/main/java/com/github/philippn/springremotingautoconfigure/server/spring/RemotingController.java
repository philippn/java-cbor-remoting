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
package com.github.philippn.springremotingautoconfigure.server.spring;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.github.philippn.springremotingautoconfigure.cbor.CborMapperFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class RemotingController implements Controller, InitializingBean {

    private Object service;
    private Class<?> serviceInterface;
    private CborMapperFactory cborMapperFactory;
    private CBORFactory cborFactory;
    private TypeFactory typeFactory;

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public CborMapperFactory getCborMapperFactory() {
        return cborMapperFactory;
    }

    public void setCborMapperFactory(CborMapperFactory cborMapperFactory) {
        this.cborMapperFactory = cborMapperFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CBORMapper mapper = cborMapperFactory.newMapper();
        cborFactory = new CBORFactory(mapper);
        typeFactory = mapper.getTypeFactory();
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try (CBORParser input = cborFactory.createParser(request.getInputStream())) {
            input.nextToken();
            input.nextToken();
            String methodName = input.getValueAsString();
            input.nextToken();
            int arity = input.getValueAsInt();
            Optional<Method> method = findMethod(methodName, arity);
            if (method.isEmpty()) {
                response.sendError(422);
                return null;
            }
            Object[] args = new Object[arity];
            for (int i = 0; i < arity; i++) {
                JavaType argType = typeFactory.constructType(method.get().getGenericParameterTypes()[i]);
                input.nextToken();
                args[i] = input.getCodec().readValue(input, argType);
            }
            try {
                Object ret = method.get().invoke(service, args);
                try (CBORGenerator output = cborFactory.createGenerator(response.getOutputStream())) {
                    output.writeStartArray();
                    output.writeBoolean(true);
                    if (!Void.class.equals(method.get().getReturnType())) {
                        output.writeObject(ret);
                    }
                    output.writeEndArray();
                }
                return null;
            } catch (InvocationTargetException e) {
                try (CBORGenerator output = cborFactory.createGenerator(response.getOutputStream())) {
                    output.writeStartArray();
                    output.writeBoolean(false);
                    output.writeString(e.getCause().getClass().getName());
                    output.writeObject(e.getCause());
                    output.writeEndArray();
                }
                return null;
            }
        } catch (IllegalAccessException | IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private Optional<Method> findMethod(String methodName, int arity) {
        return Arrays.stream(serviceInterface.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> m.getParameterCount() == arity)
                .findFirst();
    }
}
