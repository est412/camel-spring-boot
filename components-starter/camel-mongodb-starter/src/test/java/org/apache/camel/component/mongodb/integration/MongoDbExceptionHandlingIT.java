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
package org.apache.camel.component.mongodb.integration;

import com.mongodb.DBObject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.bson.Document;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                CamelAutoConfiguration.class,
                MongoDbExceptionHandlingIT.class,
                MongoDbExceptionHandlingIT.TestConfiguration.class,
                AbstractMongoDbITSupport.MongoConfiguration.class
        }
)
public class MongoDbExceptionHandlingIT extends AbstractMongoDbITSupport {

    @Configuration
    public class TestConfiguration {

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct:findAll").to(
                                    "mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=findAll&dynamicity=true")
                            .to("mock:resultFindAll");

                    from("direct:findOneByQuery").to(
                                    "mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=findOneByQuery&dynamicity=true")
                            .to("mock:resultFindOneByQuery");

                    from("direct:findById").to(
                                    "mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=findById&dynamicity=true")
                            .to("mock:resultFindById");
                }
            };
        }
    }

    @Test
    public void testInduceParseException() {
        // Test that the collection has 0 documents in it
        assertEquals(0, testCollection.countDocuments());
        pumpDataIntoTestCollection();

        // notice missing quote at the end of Einstein
        try {
            template.requestBody("direct:findOneByQuery", "{\"scientist\": \"Einstein}");
            fail("Should have thrown an exception");
        } catch (Exception e) {
            extractAndAssertCamelMongoDbException(e, null);
        }
    }

    @Test
    public void testInduceParseAndThenOkException() {
        // Test that the collection has 0 documents in it
        assertEquals(0, testCollection.countDocuments());
        pumpDataIntoTestCollection();

        // notice missing quote at the end of Einstein
        try {
            template.requestBody("direct:findOneByQuery", "{\"scientist\": \"Einstein}");
            fail("Should have thrown an exception");
        } catch (Exception e) {
            extractAndAssertCamelMongoDbException(e, null);
        }

        // this one is okay
        DBObject out = template.requestBody("direct:findOneByQuery", "{\"scientist\": \"Einstein\"}", DBObject.class);
        assertNotNull(out);
        assertEquals("Einstein", out.get("scientist"));
    }

    @Test
    public void testErroneousDynamicOperation() {
        // Test that the collection has 0 documents in it
        assertEquals(0, testCollection.countDocuments());
        pumpDataIntoTestCollection();

        try {
            template.requestBodyAndHeader("direct:findOneByQuery", new Document("scientist", "Einstein").toJson(),
                    MongoDbConstants.OPERATION_HEADER, "dummyOp");
            fail("Should have thrown an exception");
        } catch (Exception e) {
            extractAndAssertCamelMongoDbException(e, "Operation specified on header is not supported. Value: dummyOp");
        }

    }

}
