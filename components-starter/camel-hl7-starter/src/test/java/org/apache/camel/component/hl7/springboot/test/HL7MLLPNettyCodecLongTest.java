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
package org.apache.camel.component.hl7.springboot.test;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7MLLPNettyDecoderFactory;
import org.apache.camel.component.hl7.HL7MLLPNettyEncoderFactory;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.MDM_T02;
import ca.uhn.hl7v2.model.v25.segment.MSH;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.util.IOHelper;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        HL7MLLPNettyCodecLongTest.class,
        HL7MLLPNettyCodecLongTest.TestConfiguration.class
    }
)
public class HL7MLLPNettyCodecLongTest extends HL7TestSupport {

    
    @Autowired
    ProducerTemplate template;

    @EndpointInject("mock:result")
    MockEndpoint mock;

    
    @Test
    public void testSendHL7Message() throws Exception {
        // START SNIPPET: e2
        BufferedReader in = IOHelper.buffered(new InputStreamReader(getClass().getResourceAsStream("/mdm_t02.txt")));
        String line = "";
        String message = "";
        while (line != null) {
            if ((line = in.readLine()) != null) {
                message += line + "\r";
            }
        }
        message = message.substring(0, message.length() - 1);
        assertEquals(70010, message.length());
        String out = template.requestBody(
                "netty:tcp://127.0.0.1:" + getPort() + "?sync=true&encoders=#hl7encoder&decoders=#hl7decoder", message,
                String.class);
        assertEquals("some response", out);
        // END SNIPPET: e2
    }

    // *************************************
    // Config
    // *************************************

    @Configuration
    public static class TestConfiguration {

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("netty:tcp://127.0.0.1:" + getPort() + "?sync=true&encoders=#hl7encoder&decoders=#hl7decoder")
                            .process(new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    assertEquals(70010, exchange.getIn().getBody(byte[].class).length);
                                    MDM_T02 input = (MDM_T02) exchange.getIn().getBody(Message.class);
                                    assertEquals("2.5", input.getVersion());
                                    MSH msh = input.getMSH();
                                    assertEquals("20071129144629", msh.getDateTimeOfMessage().getTime().getValue());
                                    exchange.getMessage().setBody("some response");
                                }
                            }).to("mock:result");
                }
            };
        }
    }
    
    @Bean("hl7decoder")
    private HL7MLLPNettyDecoderFactory addDecoder() throws Exception {

        HL7MLLPNettyDecoderFactory decoder = new HL7MLLPNettyDecoderFactory();
        decoder.setCharset("iso-8859-1");
        return decoder;
    }

    @Bean("hl7encoder")
    private HL7MLLPNettyEncoderFactory addEncoder() throws Exception {

        HL7MLLPNettyEncoderFactory encoder = new HL7MLLPNettyEncoderFactory();
        encoder.setCharset("iso-8859-1");
        return encoder;
    }
}
