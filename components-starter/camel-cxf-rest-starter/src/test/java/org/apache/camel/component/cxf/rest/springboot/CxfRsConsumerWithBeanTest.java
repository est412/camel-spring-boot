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
package org.apache.camel.component.cxf.rest.springboot;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxrs.testbean.ServiceUtil;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        CxfRsConsumerWithBeanTest.class,
        CxfRsConsumerWithBeanTest.TestConfiguration.class,
        CxfAutoConfiguration.class
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CxfRsConsumerWithBeanTest {

    private static final String CXT = "/CxfRsConsumerWithBeanTest";
    private static final String CXF_RS_ENDPOINT_URI
            = "cxfrs://" + CXT
              + "/rest?resourceClasses=org.apache.camel.component.cxf.jaxrs.testbean.CustomerServiceResource";
    private static final String CXF_RS_ENDPOINT_URI_2
            = "cxfrs://" + CXT
              + "/rest2?resourceClasses=org.apache.camel.component.cxf.jaxrs.testbean.CustomerServiceResource";

    @Bean("service")
    protected ServiceUtil bindToRegistry() {
        return new ServiceUtil();
    }
    
    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new UndertowServletWebServerFactory();
    }
    
    @Test
    public void testPutConsumer() throws Exception {
        sendPutRequest("http://localhost:8080/services" + CXT + "/rest/customerservice/c20");
        sendPutRequest("http://localhost:8080/services" + CXT + "/rest2/customerservice/c20");
    }

    private void sendPutRequest(String uri) throws Exception {
        HttpPut put = new HttpPut(uri);
        StringEntity entity = new StringEntity("string");
        entity.setContentType("text/plain");
        put.setEntity(entity);
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        try {
            HttpResponse response = httpclient.execute(put);
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("c20string", EntityUtils.toString(response.getEntity()));
        } finally {
            httpclient.close();
        }
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

                    from(CXF_RS_ENDPOINT_URI).to("bean://service?method=invoke(${body[0]}, ${body[1]})");
                    from(CXF_RS_ENDPOINT_URI_2).bean(ServiceUtil.class, "invoke(${body[0]}, ${body[1]})");
                }
            };
        }
    }
    
}
