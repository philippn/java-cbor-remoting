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
package com.github.philippn.springremotingautoconfigure.cbor;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.github.philippn.springremotingautoconfigure.cbor.mixin.ThrowableMixin;

import java.util.List;

/**
 * 
 */
public class CborMapperFactory {

    private final List<CborMapperCustomizer> customizers;

    public CborMapperFactory(List<CborMapperCustomizer> customizers) {
        this.customizers = customizers;
    }

    public CBORMapper newMapper() {
        CBORMapper.Builder builder = CBORMapper.builder()
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .addMixIn(Throwable.class, ThrowableMixin.class)
                .findAndAddModules();
        if (customizers != null) {
            for (CborMapperCustomizer customizer : customizers) {
                customizer.apply(builder);
            }
        }
        return builder.build();
    }
}
