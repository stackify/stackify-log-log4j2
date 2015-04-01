# stackify-log-log4j2

[![Maven Central](https://img.shields.io/maven-central/v/com.stackify/stackify-log-log4j2.svg)](http://mvnrepository.com/artifact/com.stackify/stackify-log-log4j2)
[![Build Status](https://travis-ci.org/stackify/stackify-log-log4j2.png)](https://travis-ci.org/stackify/stackify-log-log4j2)
[![Coverage Status](https://coveralls.io/repos/stackify/stackify-log-log4j2/badge.png?branch=master)](https://coveralls.io/r/stackify/stackify-log-log4j2?branch=master)

Log4j 2.x appender for sending log messages and exceptions to Stackify.

Errors and Logs Overview:

http://docs.stackify.com/m/7787/l/189767

Sign Up for a Trial:

http://www.stackify.com/sign-up/

## Installation

Add it as a maven dependency:
```xml
<dependency>
    <groupId>com.stackify</groupId>
    <artifactId>stackify-log-log4j2</artifactId>
    <version>INSERT_LATEST_MAVEN_CENTRAL_VERSION</version>
</dependency>
```

## Usage

Example configuration:
```xml
<Configuration packages="com.stackify.log.log4j2">
    <Appenders>
        <StackifyLog name="STACKIFY" apiKey="YOUR_API_KEY" application="YOUR_APPLICATION_NAME" environment="YOUR_ENVIRONMENT"/>
        ...
    <Appenders>
    <Loggers>
        <Root ...>
            ...
            <AppenderRef ref="STACKIFY"/>
        </Root>
    </Loggers>
</Configuration>
```

Note: *If you are logging from a device that has the stackify-agent installed, the environment setting is optional. We will use the environment associated to your device in Stackify.*

Be sure to shutdown Log4j to flush this appender of any errors and shutdown the background thread:
```java
((LoggerContext) LogManager.getContext(false)).stop();
```

## License

Copyright 2015 Stackify, LLC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
