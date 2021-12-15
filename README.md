 
[![Maven Central](https://img.shields.io/maven-central/v/com.stackify/stackify-log-log4j2.svg)](http://mvnrepository.com/artifact/com.stackify/stackify-log-log4j2)

## Stackify Log4j2 Logger

Log4j 2.x appender for sending log messages and exceptions to Stackify.

* **Errors and Logs Overview:** http://support.stackify.com/errors-and-logs-overview/
* **Sign Up for a Trial:** http://www.stackify.com/sign-up/

## Installation

Add it as a maven dependency:
```xml
<dependency>
    <groupId>com.stackify</groupId>
    <artifactId>stackify-log-log4j2</artifactId>
    <version>4.0.2</version>
    <scope>runtime</scope>
</dependency>
```

## Usage

Example configuration:
```xml
<Configuration packages="com.stackify.log.log4j2">
    <Appenders>
        <StackifyLog name="STACKIFY" apiKey="YOUR_API_KEY" application="YOUR_APPLICATION_NAME" environment="YOUR_ENVIRONMENT"/>
        ...
    </Appenders>
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
((org.apache.logging.log4j.core.LoggerContext) org.apache.logging.log4j.LogManager.getContext(false)).stop();
```

## Masking 

The Stackify appender has built-in data masking for credit cards and social security number values.

**Enable Masking:**

Add `<MaskEnabled>true</MaskEnabled>` inside the `<StackifyLog> ... </StackifyLog>` tag.

**Customize Masking:**

The example below has the following customizations: 

1. Credit Card value masking is disabled (`<Mask enabled="false">CREDITCARD</Mask>`)
2. IP Address masking is enabled (`<Mask enabled="true">IP</Mask>`). Built in masks are `CREDITCARD`, `SSN` and `IP`.
3. Custom masking to remove vowels using a regex (`<Mask enabled="true">[aeiou]</Mask>`)
 
```xml
<Configuration packages="com.stackify.log.log4j2">
    <Appenders>
        <StackifyLog name="STACKIFY" apiKey="YOUR_API_KEY" application="YOUR_APPLICATION_NAME" environment="YOUR_ENVIRONMENT">
            <MaskEnabled>true</MaskEnabled>
            <Mask enabled="false">CREDITCARD</Mask>
            <Mask enabled="true">SSN</Mask>
            <Mask enabled="true">IP</Mask>
            <Mask enabled="true">[aeiou]</Mask>
        </StackifyLog>
        ...
    </Appenders>
    <Loggers>
        <Root ...>
            ...
            <AppenderRef ref="STACKIFY"/>
        </Root>
    </Loggers>
</Configuration>
```

## Legacy Support 

For legacy support of **Java 1.6 and 1.7** use the following maven dependency: 
```
<dependency>
    <groupId>com.stackify</groupId>
    <artifactId>stackify-log-log4j2</artifactId>
    <version>2.1.2</version>
</dependency>
```

## License

Copyright 2020 Stackify, LLC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
