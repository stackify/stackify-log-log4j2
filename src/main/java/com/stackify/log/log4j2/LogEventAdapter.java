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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackify.api.EnvironmentDetail;
import com.stackify.api.LogMsg;
import com.stackify.api.StackifyError;
import com.stackify.api.WebRequestDetail;
import com.stackify.api.common.lang.Throwables;
import com.stackify.api.common.log.EventAdapter;
import com.stackify.api.common.log.ServletLogContext;
import com.stackify.api.common.util.Maps;
import com.stackify.api.common.util.Preconditions;

/**
 * LogEventAdapter
 * @author Eric Martin
 */
public class LogEventAdapter implements EventAdapter<LogEvent> {

	/**
	 * Environment detail
	 */
	private final EnvironmentDetail envDetail;
	
	/**
	 * JSON converter
	 */
	private final ObjectMapper json = new ObjectMapper();
	
	/**
	 * Constructor
	 * @param envDetail Environment detail
	 */
	public LogEventAdapter(final EnvironmentDetail envDetail) {
		Preconditions.checkNotNull(envDetail);
		this.envDetail = envDetail;
	}
	
	/**
	 * @see com.stackify.api.common.log.EventAdapter#getThrowable(java.lang.Object)
	 */
	@Override
	public Throwable getThrowable(final LogEvent event) {
		return event.getThrown();
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#getStackifyError(java.lang.Object, java.lang.Throwable)
	 */
	@Override
	public StackifyError getStackifyError(final LogEvent event, final Throwable exception) {
		
		StackifyError.Builder builder = StackifyError.newBuilder();
		builder.environmentDetail(envDetail);		
		builder.occurredEpochMillis(new Date(event.getTimeMillis()));
		
		if (exception != null) {
			builder.error(Throwables.toErrorItem(getMessage(event), exception));
		} else {
			String className = null;
			String methodName = null;
			int lineNumber = 0;
			
			StackTraceElement source = event.getSource();
			
			if (source != null) {
				className = source.getClassName();
				methodName = source.getMethodName();
				
				try {
					lineNumber = source.getLineNumber();
				} catch (Throwable e) {
				}
			}
			
			builder.error(Throwables.toErrorItem(getMessage(event), className, methodName, lineNumber));
		}
		
		String user = ServletLogContext.getUser();
		
		if (user != null) {
			builder.userName(user);
		}
		
		WebRequestDetail webRequest = ServletLogContext.getWebRequest();
		
		if (webRequest != null) {
			builder.webRequestDetail(webRequest);
		}
		
		builder.serverVariables(Maps.fromProperties(System.getProperties()));
		
		return builder.build();
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#getLogMsg(java.lang.Object, com.google.common.base.Optional)
	 */
	@Override
	public LogMsg getLogMsg(final LogEvent event, final StackifyError error) {
		
		LogMsg.Builder builder = LogMsg.newBuilder();
		
		builder.msg(getMessage(event));

		Map<String, String> props = getProperties(event);
		
		if (!props.isEmpty()) {
			try {
				builder.data(json.writeValueAsString(props));
			} catch (Exception e) {
				// do nothing
			}
		}
				
		builder.ex(error);
		builder.th(event.getThreadName());
		builder.epochMs(event.getTimeMillis());
		builder.level(event.getLevel().toString().toLowerCase());

		String transactionId = ServletLogContext.getTransactionId();
		
		if (transactionId != null) {
			builder.transId(transactionId);
		}

		StackTraceElement source = event.getSource();

		if (source != null) {			
			builder.srcMethod(source.getClassName() + "." + source.getMethodName());
			
			try {
				builder.srcLine(source.getLineNumber());
			} catch (Throwable e) {
			}
		}
		
		return builder.build();
	}

	/**
	 * Gets the log message from the event
	 * @param event The event
	 * @return The log message
	 */
	public String getMessage(final LogEvent event) {
		
		Message message = event.getMessage();
		
		if (message != null) {
			return message.getFormattedMessage();
		}
		
		return null;
	}
	
	/**
	 * Gets properties from the event's MDC and MDC
	 * @param event The logging event
	 * @return Map assembled from the event's MDC and NDC
	 */
	public Map<String, String> getProperties(final LogEvent event) {
		
		Map<String, String> properties = new HashMap<String, String>();
		
		// unload the MDC
		
		Map<String, String> mdc = event.getContextMap();
		
		if (mdc != null) {
		    Iterator<Map.Entry<String, String>> mdcIterator = mdc.entrySet().iterator();
		    
		    while (mdcIterator.hasNext()) {
		        Map.Entry<String, String> entryPair = (Map.Entry<String, String>) mdcIterator.next();
		        
		        String key = entryPair.getKey();
		        String value = entryPair.getValue();
		        
		        properties.put(key, value != null ? value.toString() : null);
		    }
		}
		
		// unload the NDC
		
		ContextStack contextStack = event.getContextStack();
		
		if (contextStack != null) {
			String ndc = contextStack.peek();
			
			if (ndc != null) {
				if (!ndc.isEmpty()) {
					properties.put("NDC", ndc);
				}
			}
		}
		
		// return the properties
		
		return properties;		
	}

	/**
	 * @see com.stackify.api.common.log.EventAdapter#isErrorLevel(java.lang.Object)
	 */
	@Override
	public boolean isErrorLevel(LogEvent event) {
		return (event.getLevel() == Level.ERROR) || (event.getLevel() == Level.FATAL);
	}
}
