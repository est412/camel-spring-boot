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
package org.apache.camel.component.cxf.soap.springboot.greeterroute;

import javax.xml.ws.Endpoint;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.TestCxfFeature;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.component.cxf.spring.jaxws.CxfSpringEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.apache.hello_world_soap_http.GreeterImpl;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(classes = {
                           CamelAutoConfiguration.class, 
                           CxfGreeterPayLoadWithFeatureRouterTest.class,
                           CxfGreeterPayLoadWithFeatureRouterTest.TestConfiguration.class,
                           AbstractCXFGreeterRouterTest.TestConfiguration.class,
                           CxfAutoConfiguration.class
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CxfGreeterPayLoadWithFeatureRouterTest extends AbstractCXFGreeterRouterTest {

    private static String backServiceAddress = "/CxfGreeterPayLoadWithFeatureRouterTest/SoapContext/SoapPort";
    protected static Endpoint endpoint;
    
    

    @AfterEach
    public void stopService() {
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    @BeforeEach
    public void startService() {
        Object implementor = new GreeterImpl();
        endpoint = Endpoint.publish(backServiceAddress, implementor);
    }

    
    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {
        
        @Bean
        CxfEndpoint routerEndpoint() {
            CxfSpringEndpoint cxfEndpoint = new CxfSpringEndpoint();
            cxfEndpoint.setServiceClass(org.apache.hello_world_soap_http.GreeterImpl.class);
            cxfEndpoint.setAddress("/CxfGreeterPayLoadWithFeatureRouterTest/CamelContext/RouterPort");
            cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
            cxfEndpoint.setPublishedEndpointUrl("http://www.simple.com/services/test");
            return cxfEndpoint;
        }
        
        @Bean
        CxfEndpoint serviceEndpoint() {
            CxfSpringEndpoint cxfEndpoint = new CxfSpringEndpoint();
            cxfEndpoint.setServiceNameAsQName(serviceName);
            cxfEndpoint.setEndpointNameAsQName(endpointName);
            cxfEndpoint.setServiceClass(org.apache.hello_world_soap_http.Greeter.class);
            cxfEndpoint.setAddress("http://localhost:" + port + "/services" + backServiceAddress);
            cxfEndpoint.setWsdlURL("testutils/hello_world.wsdl");
            cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
            cxfEndpoint.getFeatures().add(new TestCxfFeature());
            return cxfEndpoint;
        }


        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    errorHandler(noErrorHandler());
                    from("cxf:bean:routerEndpoint")
                            .to("cxf:bean:serviceEndpoint");
                }
            };
        }
    }

}
