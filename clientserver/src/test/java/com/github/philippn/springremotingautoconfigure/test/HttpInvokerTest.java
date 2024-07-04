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

import com.github.philippn.springremotingautoconfigure.test.service.PingService;
import com.github.philippn.springremotingautoconfigure.test.service.PingServiceWithMappingPath;
import com.github.philippn.springremotingautoconfigure.test.service.exception.PingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Philipp Nanz
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class HttpInvokerTest {

	@Autowired
	@Qualifier("PingServiceProxy")
	private PingService pingServiceProxy;

	@Autowired
	@Qualifier("PingServiceWithMappingPathProxy")
	private PingServiceWithMappingPath pingServiceWithMappingPathProxy;

	@Test
	void testDefaultMappingPath() throws PingException {
		assertEquals("pong", pingServiceProxy.ping("ping"));
	}

	@Test
	void testSpecifiedMappingPath() throws PingException {
		assertEquals("pong", pingServiceWithMappingPathProxy.ping("ping"));
	}

	@Test
	public void testException() {
		try {
			assertEquals("pong", pingServiceProxy.ping("pong"));
			fail();
		} catch (PingException e) {
			assertEquals("Unsupported message: pong", e.getMessage());
		}
	}
}
