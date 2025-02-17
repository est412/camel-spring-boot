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



import javax.mail.Message;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mail.MailConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.jvnet.mock_javamail.Mailbox;


@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        MailCustomContentTypeTest.class,
        MailCustomContentTypeTest.TestConfiguration.class
    }
)
public class MailCustomContentTypeTest {

    
    @Autowired
    ProducerTemplate template;
    
    @Autowired
    CamelContext context;
    
    @EndpointInject("mock:result")
    MockEndpoint mock;
    
    @Test
    public void testSendHtmlMail() throws Exception {
        Mailbox.clearAll();

        template.sendBody("direct:a", "<html><body><h1>Hello</h1>World</body></html>");

        Mailbox box = Mailbox.get("claus@localhost");
        Message msg = box.get(0);

        assertTrue(msg.getContentType().startsWith("text/html"));
        assertEquals("text/html; charset=UTF-8", msg.getContentType());
        assertEquals("<html><body><h1>Hello</h1>World</body></html>", msg.getContent());
    }

    @Test
    public void testSendHtmlMailIso88591() throws Exception {
        Mailbox.clearAll();

        template.sendBody("direct:c", "<html><body><h1>Hello</h1>World</body></html>");

        Mailbox box = Mailbox.get("claus@localhost");
        Message msg = box.get(0);

        assertTrue(msg.getContentType().startsWith("text/html"));
        assertEquals("text/html; charset=iso-8859-1", msg.getContentType());
        assertEquals("<html><body><h1>Hello</h1>World</body></html>", msg.getContent());
    }

    @Test
    public void testNullBody() throws Exception {
        Mailbox.clearAll();

        template.sendBodyAndHeader("direct:b", null, MailConstants.MAIL_CONTENT_TYPE, "text/plain; charset=iso-8859-1");

        Mailbox box = Mailbox.get("claus@localhost");
        Message msg = box.get(0);
        assertEquals("text/plain; charset=iso-8859-1", msg.getContentType());
        assertEquals("", msg.getContent());
    }

    @Test
    public void testSendPlainMailContentTypeInHeader() throws Exception {
        Mailbox.clearAll();

        template.sendBodyAndHeader("direct:b", "Hello World", MailConstants.MAIL_CONTENT_TYPE,
                "text/plain; charset=iso-8859-1");

        Mailbox box = Mailbox.get("claus@localhost");
        Message msg = box.get(0);
        assertEquals("text/plain; charset=iso-8859-1", msg.getContentType());
        assertEquals("Hello World", msg.getContent());
    }

    @Test
    public void testSendPlainMailContentTypeInHeader2() throws Exception {
        Mailbox.clearAll();

        template.sendBodyAndHeader("direct:b", "Hello World", Exchange.CONTENT_TYPE, "text/plain; charset=iso-8859-1");

        Mailbox box = Mailbox.get("claus@localhost");
        Message msg = box.get(0);
        assertEquals("text/plain; charset=iso-8859-1", msg.getContentType());
        assertEquals("Hello World", msg.getContent());
    }

    @Test
    public void testSendPlainMailContentTypeTinyTypeInHeader() throws Exception {
        Mailbox.clearAll();

        // Camel will fixup the Content-Type if you do not have a space after the semi colon
        template.sendBodyAndHeader("direct:b", "Hello World", MailConstants.MAIL_CONTENT_TYPE, "text/plain;charset=iso-8859-1");

        Mailbox box = Mailbox.get("claus@localhost");
        Message msg = box.get(0);
        // the content type should have a space after the semi colon
        assertEquals("text/plain; charset=iso-8859-1", msg.getContentType());
        assertEquals("Hello World", msg.getContent());
    }
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct:a").to("smtp://claus@localhost?contentType=text/html;charset=UTF-8");
                    from("direct:b").to("smtp://claus@localhost");
                    from("direct:c").to("smtp://claus@localhost?contentType=text/html;charset=iso-8859-1");
                }
            };
        }
    }
    
   

}
