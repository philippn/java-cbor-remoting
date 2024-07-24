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
package com.github.philippn.springremotingautoconfigure.test;

import com.github.philippn.springremotingautoconfigure.cbor.CborMapperCustomizer;
import com.github.philippn.springremotingautoconfigure.cbor.CborMapperFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.philippn.springremotingautoconfigure.client.annotation.EnableHttpInvokerAutoProxy;
import com.github.philippn.springremotingautoconfigure.server.annotation.EnableHttpInvokerAutoExport;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author Philipp Nanz
 */
@SpringBootApplication
@EnableHttpInvokerAutoExport
@EnableHttpInvokerAutoProxy
public class HttpInvokerTestApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(HttpInvokerTestApplication.class, args);
    }

    @Bean
    public CborMapperFactory cborMapperFactory(@Autowired(required = false) List<CborMapperCustomizer> customizers) {
        return new CborMapperFactory(customizers);
    }

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.createDefault();
    }
}
