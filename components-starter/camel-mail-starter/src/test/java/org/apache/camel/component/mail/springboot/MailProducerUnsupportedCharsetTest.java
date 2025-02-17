/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.mail.springboot;



import static org.apache.camel.test.junit5.TestSupport.assertIsInstanceOf;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.jvnet.mock_javamail.Mailbox;


@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        MailProducerUnsupportedCharsetTest.class
    }
)
public class MailProducerUnsupportedCharsetTest {

    
    @Autowired
    ProducerTemplate template;
    
    @Autowired
    CamelContext context;
    
    @EndpointInject("mock:result")
    MockEndpoint mock;
    
    @Test
    public void testSencUnsupportedCharset() throws Exception {
        Mailbox.clearAll();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("pop3://jones@localhost?password=secret&initialDelay=100&delay=100&ignoreUnsupportedCharset=true")
                        .to("mock:result");
            }
        });
        context.start();

        
        mock.expectedBodiesReceived("Hello World", "Bye World");
        mock.allMessages().header("Content-Type").isEqualTo("text/plain");

        Map<String, Object> headers = new HashMap<>();
        headers.put("To", "jones@localhost");
        headers.put("Content-Type", "text/plain");
        template.sendBodyAndHeaders("smtp://localhost?ignoreUnsupportedCharset=true", "Hello World", headers);

        headers.clear();
        headers.put("To", "jones@localhost");
        headers.put("Content-Type", "text/plain; charset=ansi_x3.110-1983");
        template.sendBodyAndHeaders("smtp://localhost?ignoreUnsupportedCharset=true", "Bye World", headers);

        mock.assertIsSatisfied();
    }

    @Test
    public void testSencUnsupportedCharsetDisabledOption() throws Exception {
        Mailbox.clearAll();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("pop3://jones@localhost?password=secret&initialDelay=100&delay=100&ignoreUnsupportedCharset=false")
                        .to("mock:result");
            }
        });
        context.start();

        
        mock.expectedBodiesReceived("Hello World");
        mock.allMessages().header("Content-Type").isEqualTo("text/plain");

        Map<String, Object> headers = new HashMap<>();
        headers.put("To", "jones@localhost");
        headers.put("Content-Type", "text/plain");
        template.sendBodyAndHeaders("smtp://localhost?ignoreUnsupportedCharset=false", "Hello World", headers);

        headers.clear();
        headers.put("To", "jones@localhost");
        headers.put("Content-Type", "text/plain; charset=XXX");
        try {
            template.sendBodyAndHeaders("smtp://localhost?ignoreUnsupportedCharset=false", "Bye World", headers);
            fail("Should have thrown an exception");
        } catch (RuntimeCamelException e) {
            assertIsInstanceOf(UnsupportedEncodingException.class, e.getCause());
        }

        mock.assertIsSatisfied();
    }
}
