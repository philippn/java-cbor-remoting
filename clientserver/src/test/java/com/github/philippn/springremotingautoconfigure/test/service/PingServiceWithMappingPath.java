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
package com.github.philippn.springremotingautoconfigure.test.service;

import com.github.philippn.springremotingautoconfigure.annotation.RemoteExport;
import com.github.philippn.springremotingautoconfigure.test.service.exception.PingException;

/**
 * @author Philipp Nanz
 */
@RemoteExport(mappingPath="/ThisIsIt")
public interface PingServiceWithMappingPath {

	/**
	 * Returns <code>pong</code>.
	 * @param message the message
	 * @return <code>pong</code>
	 */
	String ping(String message) throws PingException;
}
