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

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.stackify.api.common.ApiClients;
import com.stackify.api.common.ApiConfiguration;
import com.stackify.api.common.ApiConfigurations;
import com.stackify.api.common.log.LogAppender;

/**
 * Log4j 2.x logger appender for sending logs to Stackify.
 * 
 * <p>
 * Example appender configuration (log4j2.xml file):
 * <pre>
 * <Configuration packages="com.stackify.log.log4j2">
 *     <Appenders>
 *         <StackifyLog name="STACKIFY" apiKey="YOUR_API_KEY" application="YOUR_APPLICATION_NAME" environment="YOUR_ENVIRONMENT"/>
 *         ...
 *     <Appenders>
 *     <Loggers>
 *         <Root ...>
 *             ...
 *             <AppenderRef ref="STACKIFY"/>
 *         </Root>
 *     </Loggers>
 * </Configuration>
 * </pre>
 *
 * <p>
 * Be sure to shutdown Log4j to flush this appender of any logs and shutdown the background thread:
 * <pre>
 * ((LoggerContext) LogManager.getContext(false)).stop();
 * </pre>
 
 * @author Eric Martin
 */
@Plugin(name = "StackifyLog", category = "Core", elementType = "appender")
public class StackifyLogAppender extends NonReentrantAppender {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -6565668877549998441L;

	/**
	 * API URL (Appender configuration parameter)
	 */
	private static final String DEFAULT_API_URL = "https://api.stackify.com";

	/**
	 * API URL (Appender configuration parameter)
	 */
	private final String apiUrl;
	
	/**
	 * API Key (Appender configuration parameter)
	 */
	private final String apiKey;
	
	/**
	 * Application name (Appender configuration parameter)
	 */
	private final String application;
	
	/**
	 * Environment (Appender configuration parameter)
	 */
	private final String environment;
		
	/**
	 * Generic log appender
	 */
	private LogAppender<LogEvent> logAppender;
	
	/**
	 * Factory method for the appender
	 * @param name The Appender name
	 * @param filter The Filter to associate with the Appender
	 * @param apiUrl API URL
	 * @param apiKey API Key
	 * @param application Application name
	 * @param environment Environment
	 * @return StackifyLogAppender
	 */
	@PluginFactory
	public static StackifyLogAppender createAppender(@PluginAttribute("name") final String name, 
			                                         @PluginElement("filters") final Filter filter,
			                                         @PluginAttribute("apiUrl") final String apiUrl,
			                                         @PluginAttribute("apiKey") final String apiKey,
			                                         @PluginAttribute("application") final String application,
			                                         @PluginAttribute("environment") final String environment) {
	    return new StackifyLogAppender(name, filter, apiUrl, apiKey, application, environment);
	}
	
    /**
     * Constructor.
	 * @param name The Appender name
	 * @param filter The Filter to associate with the Appender
	 * @param apiUrl API URL
	 * @param apiKey API Key
	 * @param application Application name
	 * @param environment Environment
     */
	protected StackifyLogAppender(final String name, final Filter filter, final String apiUrl, final String apiKey, 
			final String application, final String environment) {
		super(name, filter, null);
		
		this.apiUrl = (apiUrl != null) ? apiUrl : DEFAULT_API_URL;		
		this.apiKey = apiKey;
		this.application = application;
		this.environment = environment;
	}

	/**
	 * @return the apiUrl
	 */
	public String getApiUrl() {
		return apiUrl;
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @return the application
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * @return the environment
	 */
	public String getEnvironment() {
		return environment;
	}

	/**
	 * @see org.apache.logging.log4j.core.filter.AbstractFilterable#start()
	 */
	@Override
	public void start() {
		super.start();

		if (logAppender == null) {
			
			// build the api config
			
			ApiConfiguration apiConfig = ApiConfigurations.fromPropertiesWithOverrides(apiUrl, apiKey, application, environment);
			
			// get the client project name with version
	
			String clientName = ApiClients.getApiClient(StackifyLogAppender.class, "/stackify-log-log4j2.properties", "stackify-log-log4j2");
	
			// build the log appender
			
			try {
				this.logAppender = new LogAppender<LogEvent>(clientName, new LogEventAdapter(apiConfig.getEnvDetail()));
				this.logAppender.activate(apiConfig);
			} catch (Exception e) {
				error("Exception starting the Stackify_LogBackgroundService", e);
			}
		}
	}

	/**
	 * @see com.stackify.log.log4j2.NonReentrantAppender#subAppend(org.apache.logging.log4j.core.LogEvent)
	 */
	@Override
	protected void subAppend(final LogEvent event) {
		try {
			this.logAppender.append(event);
		} catch (Exception e) {
			error("Exception appending event to Stackify Log Appender", event, e);
		}
	}

	/**
	 * @see org.apache.logging.log4j.core.filter.AbstractFilterable#stop()
	 */
	@Override
	public void stop() {
		super.stop();
		
		try {
			this.logAppender.close();
		} catch (Exception e) {
			error("Exception closing Stackify Log Appender", e);
		}
	}
}