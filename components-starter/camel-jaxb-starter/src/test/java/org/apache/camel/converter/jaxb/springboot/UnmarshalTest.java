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
package org.apache.camel.converter.jaxb.springboot;



import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.example.PurchaseOrder;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        UnmarshalTest.class,
        UnmarshalTest.TestConfiguration.class
    }
)
public class UnmarshalTest {
    
           
    @Autowired
    ProducerTemplate template;

    
    @EndpointInject("mock:result")
    private MockEndpoint resultEndpoint;

    @EndpointInject("mock:unmarshall")
    private MockEndpoint mockUnmarshall;

    @Test
    public void testSendXmlAndUnmarshal() throws Exception {
        PurchaseOrder expected = new PurchaseOrder();
        expected.setName("Wine");
        expected.setAmount(123.45);
        expected.setPrice(2.22);

        resultEndpoint.expectedBodiesReceived(expected);

        template.sendBody("direct:start", "<purchaseOrder name='Wine' amount='123.45' price='2.22'/>");

        resultEndpoint.assertIsSatisfied();
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
                    DataFormat jaxb = new JaxbDataFormat("org.apache.camel.example");

                    from("direct:start").unmarshal(jaxb).to("mock:result");
                }
            };
        }
    }
}
