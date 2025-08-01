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

package de.codecentric.boot.admin.server.domain.values;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EndpointsTest {

	@Test
	void should_return_endpoint_or_empty() {
		Endpoints endpoints = Endpoints.single("id", "path");
		assertThat(endpoints.isPresent("id")).isTrue();
		assertThat(endpoints.get("id")).contains(Endpoint.of("id", "path"));
		assertThat(endpoints.get("none!")).isEmpty();
	}

	@Test
	void factory_methods() {
		assertThat(Endpoints.empty()).isEqualTo(Endpoints.of(Collections.emptyList())).isEqualTo(Endpoints.of(null));
		assertThat(Endpoints.of(Collections.singletonList(Endpoint.of("id", "path"))))
			.isEqualTo(Endpoints.empty().withEndpoint("id", "path"))
			.isEqualTo(Endpoints.single("id", "path"));
	}

	@Test
	void should_throw_on_iterator_modification() {
		Endpoints endpoints = Endpoints.single("id", "path");
		assertThatThrownBy(() -> endpoints.iterator().remove()).isInstanceOf(UnsupportedOperationException.class);
	}

}
