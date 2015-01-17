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
package com.github.philippn.springremotingautoconfigure.sample.client;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.github.philippn.springremotingautoconfigure.client.annotation.EnableHttpInvokerAutoProxy;
import com.github.philippn.springremotingautoconfigure.sample.TimeService;

/**
 * @author Philipp Nanz
 */
@SpringBootApplication
@EnableScheduling
@EnableHttpInvokerAutoProxy(basePackages={"com.github.philippn.springremotingautoconfigure.sample"})
public class TimeServiceClientApplication {

	private final static Logger logger = LoggerFactory.getLogger(TimeServiceClientApplication.class);

	@Autowired
	private TimeService timeService;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(TimeServiceClientApplication.class, args);
	}

	@Scheduled(fixedRate=5000)
	public void printCurrentServerTime() {
		LocalDateTime dt = timeService.serverTime();
		logger.info("Current server time: " + dt);
	}
}
