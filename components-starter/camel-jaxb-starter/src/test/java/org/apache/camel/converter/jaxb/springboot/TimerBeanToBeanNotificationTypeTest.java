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
import org.apache.camel.jaxb.MyNotificationService;
import org.apache.camel.jaxb.NotificationType;
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
        TimerBeanToBeanNotificationTypeTest.class,
        TimerBeanToBeanNotificationTypeTest.TestConfiguration.class
    }
)
public class TimerBeanToBeanNotificationTypeTest {
    
    
        
    @Autowired
    ProducerTemplate template;

    
    @EndpointInject("mock:notify")
    private MockEndpoint notify;

    @Test
    public void testBeanToBean() throws Exception {
        notify.expectedMessageCount(1);
        notify.message(0).body().isInstanceOf(NotificationType.class);

        notify.assertIsSatisfied();
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
                public void configure() throws Exception {
                    from("timer:foo?delay=500&repeatCount=1")
                            .log("Timer triggered")
                            .bean(MyNotificationService.class, "createNotification")
                            .bean(MyNotificationService.class, "sendNotification");
                }
            };
        }
    }
}
