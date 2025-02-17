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
package org.apache.camel.component.telegram.springboot;


import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.TelegramConstants;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.apache.camel.component.telegram.springboot.TelegramMockRoutes.MockProcessor;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;


@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        TelegramProducerChatIdResolutionTest.class,
        TelegramProducerChatIdResolutionTest.TestConfiguration.class
    }
)
public class TelegramProducerChatIdResolutionTest extends TelegramTestSupport {

    
    static TelegramMockRoutes mockRoutes;
    
    @EndpointInject("direct:telegram")
    private Endpoint endpoint;

    @Test
    public void testRouteWithFixedChatId() {
        final MockProcessor<OutgoingTextMessage> mockProcessor = mockRoutes.getMock("sendMessage");
        mockProcessor.clearRecordedMessages();

        template.sendBody(endpoint, "Hello");

        final OutgoingTextMessage message = mockProcessor.awaitRecordedMessages(1, 5000).get(0);
        assertEquals("my-id", message.getChatId());
        assertEquals("Hello", message.getText());
        assertNull(message.getParseMode());
    }

    @Test
    public void testRouteWithOverridenChatId() {
        final MockProcessor<OutgoingTextMessage> mockProcessor = mockRoutes.getMock("sendMessage");
        mockProcessor.clearRecordedMessages();

        Exchange exchange = endpoint.createExchange();
        exchange.getIn().setBody("Hello 2");
        exchange.getIn().setHeader(TelegramConstants.TELEGRAM_CHAT_ID, "my-second-id");

        template.send(endpoint, exchange);

        final OutgoingTextMessage message = mockProcessor.awaitRecordedMessages(1, 5000).get(0);
        assertEquals("my-second-id", message.getChatId());
        assertEquals("Hello 2", message.getText());
        assertNull(message.getParseMode());

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
                    from("direct:telegram")
                            .to("telegram:bots?authorizationToken=mock-token&chatId=my-id");
                }
            };
        }

    }
    
    @Override
    @Bean
    protected TelegramMockRoutes createMockRoutes() {
        mockRoutes =
         new TelegramMockRoutes(port)
            .addEndpoint(
                    "sendMessage",
                    "POST",
                    OutgoingTextMessage.class,
                    TelegramTestUtil.stringResource("messages/send-message.json"));
        return mockRoutes;
    }
}
