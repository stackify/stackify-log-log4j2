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

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

/**
 * Log4j appender that guards against reentering the same instance of the log4j appender
 * 
 * @author Eric Martin
 */
public abstract class NonReentrantAppender extends AbstractAppender {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -2891154307901596455L;
	
	/**
	 * Guard against re-entering an appender from the same appender
	 */
    private final ThreadLocal<Boolean> guard =
        new ThreadLocal<Boolean>() {
            @Override protected Boolean initialValue() {
                return Boolean.FALSE;
        }
    };

	/**
	 * Constructor
	 * @param name The Appender name
	 * @param filter The Filter to associate with the Appender
     * @param layout The layout to use to format the event.
	 */
	protected NonReentrantAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout) {
		super(name, filter, layout);
	}
	
	/**
	 * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
	 */
	@Override
	public void append(final LogEvent event) {
		
		if (guard == null) {
			return;
		}
		
		if (guard.get() == null) {
			return;
		}
		
		if (guard.get().equals(Boolean.TRUE)) {
			return;
		}

		try {
			guard.set(Boolean.TRUE);
			subAppend(event);
		} finally {
			guard.set(Boolean.FALSE);
		}	
	}

	/**
	 * Performs the logging of the event after the reentrant guard has been verified
	 * @param event The logging event
	 */
	protected abstract void subAppend(final LogEvent event);
}
