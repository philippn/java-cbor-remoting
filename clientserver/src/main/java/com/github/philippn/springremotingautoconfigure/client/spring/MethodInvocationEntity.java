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
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.google.common.io.FileBackedOutputStream;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;

public class MethodInvocationEntity extends AbstractHttpEntity {

    private final CBORFactory cborFactory;
    private final FileBackedOutputStream outputStream;

    public MethodInvocationEntity(MethodInvocation methodInvocation, CBORFactory cborFactory) throws IOException {
        super("application/cbor", null, true);
        this.cborFactory = cborFactory;
        this.outputStream = new FileBackedOutputStream(1024*1024);
        initOutputStream(methodInvocation);
    }

    private void initOutputStream(MethodInvocation methodInvocation) throws IOException {
        try (CBORGenerator output = cborFactory.createGenerator(outputStream)) {
            output.writeStartArray();
            output.writeString(methodInvocation.getMethod().getName());
            output.writeNumber(methodInvocation.getMethod().getParameterCount());
            for (int i = 0; i < methodInvocation.getMethod().getParameterCount(); i++) {
                output.writeObject(methodInvocation.getArguments()[i]);
            }
            output.writeEndArray();
        }
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return outputStream.asByteSource().openStream();
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return -1;
    }
}
