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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.stackify.api.EnvironmentDetail;
import com.stackify.api.LogMsg;
import com.stackify.api.StackifyError;
import com.stackify.api.WebRequestDetail;
import com.stackify.api.common.log.ServletLogContext;

/**
 * LogEventAdapterTest JUnit Test
 * @author Eric Martin
 */
public class LogEventAdapterTest {

	/**
	 * testGetPropertiesWithoutMdcOrNdc
	 */
	@Test
	public void testGetPropertiesWithoutMdcOrNdc() {
		LogEvent event = Mockito.mock(LogEvent.class);

		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(0, properties.size());
	}
	
	/**
	 * testGetPropertiesWithMdc
	 */
	@Test
	public void testGetPropertiesWithMdc() {
		Map<String, String> mdcProperties = new HashMap<String, String>();
		mdcProperties.put("mdc1", "val1");
		mdcProperties.put("mdc2", "val2");
		
		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getContextMap()).thenReturn(mdcProperties);

		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(2, properties.size());
		Assert.assertEquals("val1", properties.get("mdc1"));
		Assert.assertEquals("val2", properties.get("mdc2"));
	}
	
	/**
	 * testGetPropertiesWithNdc
	 */
	@Test
	public void testGetPropertiesWithNdc() {
		ContextStack contextStack = Mockito.mock(ContextStack.class);
		Mockito.when(contextStack.peek()).thenReturn("ndcContext");

		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getContextStack()).thenReturn(contextStack);
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(1, properties.size());
		Assert.assertEquals("ndcContext", properties.get("NDC"));
	}

	/**
	 * testGetPropertiesWithMdcAndNdc
	 */
	@Test
	public void testGetPropertiesWithMdcAndNdc() {
		Map<String, String> mdcProperties = new HashMap<String, String>();
		mdcProperties.put("mdc1", "val1");
		mdcProperties.put("mdc2", "val2");
		
		ContextStack contextStack = Mockito.mock(ContextStack.class);
		Mockito.when(contextStack.peek()).thenReturn("ndcContext");

		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getContextMap()).thenReturn(mdcProperties);
		Mockito.when(event.getContextStack()).thenReturn(contextStack);

		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Map<String, String> properties = adapter.getProperties(event);

		Assert.assertNotNull(properties);
		Assert.assertEquals(3, properties.size());
		Assert.assertEquals("val1", properties.get("mdc1"));
		Assert.assertEquals("val2", properties.get("mdc2"));
		Assert.assertEquals("ndcContext", properties.get("NDC"));
	}
	
	/**
	 * testGetThrowable
	 */
	@Test
	public void testGetThrowable() {
		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getThrown()).thenReturn(new NullPointerException());
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Throwable throwable = adapter.getThrowable(event);
		
		Assert.assertNotNull(throwable);
	}

	/**
	 * testGetThrowableAbsent
	 */
	@Test
	public void testGetThrowableAbsent() {
		LogEvent event = Mockito.mock(LogEvent.class);
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		Throwable throwable = adapter.getThrowable(event);
		
		Assert.assertNull(throwable);
	}
	
	/**
	 * testGetLogMsg
	 */
	@Test
	public void testGetLogMsg() {
		String msg = "msg";
		StackifyError ex = Mockito.mock(StackifyError.class);
		String th = "th";
		String level = "debug";
		String srcClass = "srcClass";
		String srcMethod = "srcMethod";
		Integer srcLine = Integer.valueOf(14);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("key", "value");
		
		StackTraceElement source = new StackTraceElement(srcClass, srcMethod, null, srcLine);
		
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getFormattedMessage()).thenReturn(msg);
		
		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getMessage()).thenReturn(message);
		Mockito.when(event.getThreadName()).thenReturn(th);
		Mockito.when(event.getLevel()).thenReturn(Level.DEBUG);
		Mockito.when(event.getSource()).thenReturn(source);
		Mockito.when(event.getContextMap()).thenReturn(properties);

		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		LogMsg logMsg = adapter.getLogMsg(event, ex);
		
		Assert.assertNotNull(logMsg);
		Assert.assertEquals(msg, logMsg.getMsg());
		Assert.assertEquals("{\"key\":\"value\"}", logMsg.getData());
		Assert.assertEquals(ex, logMsg.getEx());		
		Assert.assertEquals(th, logMsg.getTh());		
		Assert.assertEquals(level, logMsg.getLevel());			
		Assert.assertEquals(srcClass + "." + srcMethod, logMsg.getSrcMethod());		
		Assert.assertEquals(srcLine, logMsg.getSrcLine());		
		Assert.assertEquals(srcLine, logMsg.getSrcLine());		
	}
	
	/**
	 * testGetStackifyError
	 */
	@Test
	public void testGetStackifyError() {
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getFormattedMessage()).thenReturn("Exception message");

		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getMessage()).thenReturn(message);
		
		Throwable exception = Mockito.mock(Throwable.class);
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		StackifyError error = adapter.getStackifyError(event, exception);
		
		Assert.assertNotNull(error);
	}
	
	/**
	 * testGetStackifyErrorServletContext
	 */
	@Test
	public void testGetStackifyErrorServletContext() {
		String user = "user";
		ServletLogContext.putUser(user);
		
		WebRequestDetail webRequest = WebRequestDetail.newBuilder().build();
		ServletLogContext.putWebRequest(webRequest);
		
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getFormattedMessage()).thenReturn("Exception message");

		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getMessage()).thenReturn(message);
		
		Throwable exception = Mockito.mock(Throwable.class);
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		StackifyError error = adapter.getStackifyError(event, exception);
		
		Assert.assertNotNull(error);
		
		Assert.assertEquals(user, error.getUserName());
		Assert.assertNotNull(error.getWebRequestDetail());
	}
	
	/**
	 * testGetLogMsgServletContext
	 */
	@Test
	public void testGetLogMsgServletContext() {
		String transactionId = UUID.randomUUID().toString();
		ServletLogContext.putTransactionId(transactionId);
		
		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getLevel()).thenReturn(Level.DEBUG);

		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		LogMsg logMsg = adapter.getLogMsg(event, null);
		
		Assert.assertNotNull(logMsg);
		Assert.assertEquals(transactionId, logMsg.getTransId());
	}
	
	/**
	 * testIsErrorLevel
	 */
	@Test
	public void testIsErrorLevel() {
		LogEvent debug = Mockito.mock(LogEvent.class);
		Mockito.when(debug.getLevel()).thenReturn(Level.DEBUG);

		LogEvent error = Mockito.mock(LogEvent.class);
		Mockito.when(error.getLevel()).thenReturn(Level.ERROR);
		
		LogEvent fatal = Mockito.mock(LogEvent.class);
		Mockito.when(fatal.getLevel()).thenReturn(Level.FATAL);
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));

		Assert.assertFalse(adapter.isErrorLevel(debug));
		Assert.assertTrue(adapter.isErrorLevel(error));
		Assert.assertTrue(adapter.isErrorLevel(fatal));
	}
	
	/**
	 * testGetStackifyErrorWithoutException
	 */
	@Test
	public void testGetStackifyErrorWithoutException() {
		StackTraceElement source = new StackTraceElement("class", "method", null, 123);
		
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getFormattedMessage()).thenReturn("Exception message");

		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getMessage()).thenReturn(message);
		Mockito.when(event.getSource()).thenReturn(source);
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		StackifyError error = adapter.getStackifyError(event, null);
		
		Assert.assertNotNull(error);
		Assert.assertEquals("StringException", error.getError().getErrorType());
	}
	
	/**
	 * testGetClassName
	 */
	@Test
	public void testGetClassName() {
		StackTraceElement source = new StackTraceElement("class", "method", null, 123);
		
		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getSource()).thenReturn(source);
		
		LogEventAdapter adapter = new LogEventAdapter(Mockito.mock(EnvironmentDetail.class));
		
		String className = adapter.getClassName(event);
		
		Assert.assertNotNull(className);
		Assert.assertEquals("class", className);
	}
}
