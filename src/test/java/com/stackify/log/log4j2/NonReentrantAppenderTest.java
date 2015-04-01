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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * NonReentrantAppender JUnit Test
 * 
 * @author Eric Martin
 */
public class NonReentrantAppenderTest {

	/**
	 * testAppend
	 */
	@Test
	public void testAppend() {
		TestAppender appender = new TestAppender("NAME", Mockito.mock(Filter.class), Mockito.mock(Layout.class));
		
		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getLevel()).thenReturn(Level.DEBUG);
		
		appender.append(event);
		
		List<LogEvent> events = appender.getEvents();
		Assert.assertEquals(1,  events.size());
		Assert.assertEquals(event, events.get(0));
	}

	/**
	 * testReentrantAppend
	 */
	@Test
	public void testReentrantAppend() {		
		TestAppender appender = new TestAppender("NAME", Mockito.mock(Filter.class), Mockito.mock(Layout.class));
		
		LogEvent event = Mockito.mock(LogEvent.class);
		Mockito.when(event.getLevel()).thenReturn(Level.ERROR);
		
		appender.append(event);
		
		List<LogEvent> events = appender.getEvents();
		Assert.assertEquals(1,  events.size());
		Assert.assertEquals(event, events.get(0));
	}

	/**
	 * TestAppender
	 */
	private static class TestAppender extends NonReentrantAppender {
		
		/**
		 * Events
		 */
		private final List<LogEvent> events = new ArrayList<LogEvent>();
		
		/**
		 * Constructor
		 * @param name
		 * @param filter
		 * @param layout
		 */
		protected TestAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout) {
			super(name, filter, layout);
		}

		/**
		 * @return the events
		 */
		protected List<LogEvent> getEvents() {
			return events;
		}

		/**
		 * @see com.stackify.error.log4j12.NonReentrantAppender#subAppend(org.apache.log4j.spi.LoggingEvent)
		 */
		@Override
		protected void subAppend(final LogEvent event) {
			events.add(event);
			
			if (event.getLevel() == Level.ERROR) {
				append(event);
			}
		}
	}
}
