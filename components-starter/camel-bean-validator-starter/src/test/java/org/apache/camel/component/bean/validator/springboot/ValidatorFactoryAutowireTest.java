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
package org.apache.camel.component.bean.validator.springboot;






import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.GenericBootstrap;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.bean.validator.BeanValidatorEndpoint;
import org.apache.camel.component.bean.validator.BeanValidatorProducer;
import org.apache.camel.component.bean.validator.HibernateValidationProviderResolver;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.condition.OS.AIX;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;


@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        ValidatorFactoryAutowireTest.class
    }
)
public class ValidatorFactoryAutowireTest {

    
    @Autowired
    ProducerTemplate template;
    
    @Autowired
    CamelContext context;
    
    @EndpointInject("mock:result")
    MockEndpoint mock;
    
    
    
    private static ValidatorFactory validatorFactory;
    
    @Bean("myValidatorFactory")
    private ValidatorFactory getValidatorFactory() {
        GenericBootstrap bootstrap = Validation.byDefaultProvider();
        bootstrap.providerResolver(new HibernateValidationProviderResolver());

        validatorFactory = bootstrap.configure().buildValidatorFactory();
        return validatorFactory;
    }

    

    @DisabledOnOs(AIX)
    @Test
    void configureValidatorFactoryAutowired() throws Exception {
        BeanValidatorEndpoint endpoint
                = context.getEndpoint("bean-validator:dummy", BeanValidatorEndpoint.class);
        BeanValidatorProducer producer = (BeanValidatorProducer) endpoint.createProducer();

        assertSame(validatorFactory, endpoint.getValidatorFactory());
        assertSame(validatorFactory, producer.getValidatorFactory());
    }

}
