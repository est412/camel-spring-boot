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
package org.apache.camel.component.infinispan.remote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.infinispan.InfinispanConstants;
import org.apache.camel.component.infinispan.InfinispanQueryBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.sampledomain.User;
import org.infinispan.protostream.sampledomain.marshallers.GenderMarshaller;
import org.infinispan.protostream.sampledomain.marshallers.UserMarshaller;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.infinispan.query.remote.client.impl.MarshallerRegistration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
		classes = {
				CamelAutoConfiguration.class,
				InfinispanRemoteQueryConsumerIT.class
		}
)
public class InfinispanRemoteQueryConsumerIT extends InfinispanRemoteQueryTestSupport {

	@Bean
	InfinispanQueryBuilder continuousQueryBuilderAll() {
		return qf -> qf.from(User.class).having("name").like("%Q0%").build();
	}

	@Bean
	InfinispanQueryBuilder continuousQueryBuilderNoMatch() {
		return qf -> qf.from(User.class).having("name").like("%TEST%").build();
	}

	@Bean
	InfinispanQueryBuilder continuousQueryBuilder() {
		return qf -> qf.from(User.class).having("name").like("CQ%").build();
	}

	// *****************************
	//
	// *****************************

	@Test
	public void continuousQuery() throws Exception {
		MockEndpoint continuousQueryBuilderNoMatch = getMockEndpoint("mock:continuousQueryNoMatch");
		continuousQueryBuilderNoMatch.expectedMessageCount(0);

		MockEndpoint continuousQueryBuilderAll = getMockEndpoint("mock:continuousQueryAll");
		continuousQueryBuilderAll.expectedMessageCount(CQ_USERS.length * 2);

		MockEndpoint continuousQuery = getMockEndpoint("mock:continuousQuery");
		continuousQuery.expectedMessageCount(4);

		for (int i = 0; i < 4; i++) {
			continuousQuery.message(i).header(InfinispanConstants.KEY).isEqualTo(createKey(CQ_USERS[i % 2]));
			continuousQuery.message(i).header(InfinispanConstants.CACHE_NAME).isEqualTo(getCache().getName());
			if (i >= 2) {
				continuousQuery.message(i).header(InfinispanConstants.EVENT_TYPE)
						.isEqualTo(InfinispanConstants.CACHE_ENTRY_LEAVING);
				continuousQuery.message(i).header(InfinispanConstants.EVENT_DATA).isNull();
			} else {
				continuousQuery.message(i).header(InfinispanConstants.EVENT_TYPE)
						.isEqualTo(InfinispanConstants.CACHE_ENTRY_JOINING);
				continuousQuery.message(i).header(InfinispanConstants.EVENT_DATA).isNotNull();
				continuousQuery.message(i).header(InfinispanConstants.EVENT_DATA).isInstanceOf(User.class);
			}
		}

		for (final User user : CQ_USERS) {
			getCache().put(createKey(user), user);
		}

		assertEquals(CQ_USERS.length, getCache().size());

		for (final User user : CQ_USERS) {
			getCache().remove(createKey(user));
		}

		assertTrue(getCache().isEmpty());

		continuousQuery.assertIsSatisfied();
		continuousQueryBuilderNoMatch.assertIsSatisfied();
		continuousQueryBuilderAll.assertIsSatisfied();
	}

	// *****************************
	//
	// *****************************

	@BeforeAll
	protected static void setupResources() throws Exception {
		String proto = Util.read(InfinispanRemoteQueryConsumerIT.class.getResourceAsStream("/sample_bank_account/bank.proto"));

		BasicCache<Object, Object> cache = getCacheByName(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
		cache.put("sample_bank_account/bank.proto", proto);

		MarshallerRegistration.init(MarshallerUtil.getSerializationContext(cacheContainer));
		SerializationContext serCtx = MarshallerUtil.getSerializationContext(cacheContainer);
		serCtx.registerProtoFiles(FileDescriptorSource.fromResources("/sample_bank_account/bank.proto"));
		serCtx.registerMarshaller(new UserMarshaller());
		serCtx.registerMarshaller(new GenderMarshaller());
	}

	@BeforeEach
	protected void beforeEach() {
		// cleanup the default test cache before each run
		getCache().clear();
	}

	@Bean
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() {
				fromF("infinispan:%s?queryBuilder=#continuousQueryBuilder", getCacheName())
						.to("mock:continuousQuery");
				fromF("infinispan:%s?queryBuilder=#continuousQueryBuilderNoMatch", getCacheName())
						.to("mock:continuousQueryNoMatch");
				fromF("infinispan:%s?queryBuilder=#continuousQueryBuilderAll", getCacheName())
						.to("mock:continuousQueryAll");
			}
		};
	}
}
