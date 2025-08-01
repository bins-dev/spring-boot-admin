/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ClientPropertiesTest {

	@Test
	void should_default_autoDeregister_to_false() {
		MockEnvironment env = new MockEnvironment();

		ClientProperties clientProperties = new ClientProperties();
		assertThat(clientProperties.isAutoDeregistration(env)).isFalse();

		clientProperties.setAutoDeregistration(false);
		assertThat(clientProperties.isAutoDeregistration(env)).isFalse();

		clientProperties.setAutoDeregistration(true);
		assertThat(clientProperties.isAutoDeregistration(env)).isTrue();
	}

	@Test
	void should_default_autoDeregister_to_true() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("VCAP_APPLICATION", "");

		ClientProperties clientProperties = new ClientProperties();
		assertThat(clientProperties.isAutoDeregistration(env)).isTrue();

		clientProperties.setAutoDeregistration(false);
		assertThat(clientProperties.isAutoDeregistration(env)).isFalse();

		clientProperties.setAutoDeregistration(true);
		assertThat(clientProperties.isAutoDeregistration(env)).isTrue();
	}

	@Test
	void should_return_all_adminUrls() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setApiPath("register");
		clientProperties.setUrl(new String[] { "http://first", "http://second" });

		assertThat(clientProperties.getAdminUrl()).containsExactly("http://first/register", "http://second/register");
	}

}
