/*
 * Copyright 2015 Stackify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stackify.log.log4j2;

import com.stackify.api.common.ApiConfiguration;
import com.stackify.api.common.log.LogAppender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * StackifyLogAppender JUnit Test
 * @author Eric Martin
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({StackifyLogAppender.class})
@PowerMockIgnore( {"javax.management.*"})
public class StackifyLogAppenderTest {
	
	/**
	 * testConstructor
	 */
	@Test
	public void testConstructor() {
		String name = "STACKIFY";
		Filter filter = Mockito.mock(Filter.class);
		String apiUrl = "url";
		String apiKey = "key";
		String application = "app";
		String environment = "env";
		
		StackifyLogAppender appender = StackifyLogAppender.createAppender(name, filter, apiUrl, apiKey, application, environment, null, null, null);
		
		Assert.assertNotNull(appender);
		
		Assert.assertEquals(name, appender.getName());
		Assert.assertEquals(filter, appender.getFilter());
		Assert.assertEquals(apiUrl, appender.getApiUrl());
		Assert.assertEquals(apiKey, appender.getApiKey());
		Assert.assertEquals(application, appender.getApplication());
		Assert.assertEquals(environment, appender.getEnvironment());
	}
	
	/**
	 * testConstructorDefault
	 */
	@Test
	public void testConstructorDefault() {
		String name = "STACKIFY";
		String apiKey = "key";
		String application = "app";
		
		StackifyLogAppender appender = StackifyLogAppender.createAppender(name, null, null, apiKey, application, null, null, null,null);
		
		Assert.assertNotNull(appender);
		
		Assert.assertEquals(name, appender.getName());
		Assert.assertNull(appender.getFilter());
		Assert.assertEquals("https://api.stackify.com", appender.getApiUrl());
		Assert.assertEquals(apiKey, appender.getApiKey());
		Assert.assertEquals(application, appender.getApplication());
		Assert.assertNull(appender.getEnvironment());
	}
	
	/**
	 * testStartAppendStop
	 * @throws Exception 
	 */
	@Test
	public void testStartAppendStop() throws Exception {
		StackifyLogAppender appender = StackifyLogAppender.createAppender("STACKIFY", null, null, "key", "app", null, null, null, null);

		LogAppender<LogEvent> logAppender = Mockito.mock(LogAppender.class);
		PowerMockito.whenNew(LogAppender.class).withAnyArguments().thenReturn(logAppender);

		appender.start();
		
		Mockito.verify(logAppender).activate(Mockito.any(ApiConfiguration.class));
		
		LogEvent event = Mockito.mock(LogEvent.class);
		appender.subAppend(event);
		
		Mockito.verify(logAppender).append(event);

		appender.stop();
		
		Mockito.verify(logAppender).close();
	}
}
