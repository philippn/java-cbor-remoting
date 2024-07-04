/*
 * Copyright (C) 2015-2016 Philipp Nanz
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
package com.github.philippn.springremotingautoconfigure.util;

import org.springframework.core.annotation.AnnotationUtils;

import com.github.philippn.springremotingautoconfigure.annotation.RemoteExport;

/**
 * @author Philipp Nanz
 */
public class RemotingUtils {

	public static String buildMappingPath(Class<?> serviceInterface) {
		RemoteExport definition = AnnotationUtils.findAnnotation(
				serviceInterface, RemoteExport.class);
		if (definition.mappingPath().length() > 0) {
			return definition.mappingPath();
		}
		return "/remoting/" + serviceInterface.getSimpleName();
	}
}
