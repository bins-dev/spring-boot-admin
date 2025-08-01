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

package de.codecentric.boot.admin.server.utils.jackson;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class InstanceRegisteredEventMixinTest {

	private final ObjectMapper objectMapper;

	private JacksonTester<InstanceRegisteredEvent> jsonTester;

	protected InstanceRegisteredEventMixinTest() {
		AdminServerModule adminServerModule = new AdminServerModule(new String[] { ".*password$" });
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		objectMapper = Jackson2ObjectMapperBuilder.json().modules(adminServerModule, javaTimeModule).build();
	}

	@BeforeEach
	void setup() {
		JacksonTester.initFields(this, objectMapper);
	}

	@Test
	void verifyDeserialize() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "REGISTERED")
			.put("registration",
					new JSONObject().put("name", "test")
						.put("managementUrl", "http://localhost:9080/")
						.put("healthUrl", "http://localhost:9080/heath")
						.put("serviceUrl", "http://localhost:8080/")
						.put("source", "http-api")
						.put("metadata", new JSONObject().put("PASSWORD", "******").put("user", "humptydumpty")))
			.toString();

		InstanceRegisteredEvent event = objectMapper.readValue(json, InstanceRegisteredEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));

		Registration registration = event.getRegistration();
		assertThat(registration).isNotNull();
		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:9080/");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:9080/heath");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:8080/");
		assertThat(registration.getSource()).isEqualTo("http-api");
		assertThat(registration.getMetadata()).containsOnly(entry("PASSWORD", "******"), entry("user", "humptydumpty"));
	}

	@Test
	void verifyDeserializeWithOnlyRequiredProperties() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("timestamp", 1587751031.000000000)
			.put("type", "REGISTERED")
			.put("registration", new JSONObject().put("name", "test").put("healthUrl", "http://localhost:9080/heath"))
			.toString();

		InstanceRegisteredEvent event = objectMapper.readValue(json, InstanceRegisteredEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isZero();
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));

		Registration registration = event.getRegistration();
		assertThat(registration).isNotNull();
		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getManagementUrl()).isNull();
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:9080/heath");
		assertThat(registration.getServiceUrl()).isNull();
		assertThat(registration.getSource()).isNull();
		assertThat(registration.getMetadata()).isEmpty();
	}

	@Test
	void verifyDeserializeWithoutRegistration() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "REGISTERED")
			.toString();

		InstanceRegisteredEvent event = objectMapper.readValue(json, InstanceRegisteredEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));
		assertThat(event.getRegistration()).isNull();

	}

	@Test
	void verifyDeserializeWithEmptyRegistration() throws JSONException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "REGISTERED")
			.put("registration", new JSONObject())
			.toString();

		assertThatThrownBy(() -> objectMapper.readValue(json, InstanceRegisteredEvent.class))
			.isInstanceOf(JsonMappingException.class)
			.hasCauseInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("must not be empty");
	}

	@Test
	void verifySerialize() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		Registration registration = Registration.create("test", "http://localhost:9080/heath")
			.managementUrl("http://localhost:9080/")
			.serviceUrl("http://localhost:8080/")
			.source("http-api")
			.metadata("PASSWORD", "qwertz123")
			.metadata("user", "humptydumpty")
			.build();

		InstanceRegisteredEvent event = new InstanceRegisteredEvent(id, 12345678L, timestamp, registration);

		JsonContent<InstanceRegisteredEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(12345678);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("REGISTERED");
		assertThat(jsonContent).extractingJsonPathValue("$.registration").isNotNull();

		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.name").isEqualTo("test");
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.managementUrl")
			.isEqualTo("http://localhost:9080/");
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.healthUrl")
			.isEqualTo("http://localhost:9080/heath");
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.serviceUrl")
			.isEqualTo("http://localhost:8080/");
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.source").isEqualTo("http-api");
		assertThat(jsonContent).extractingJsonPathMapValue("$.registration.metadata")
			.containsOnly(entry("PASSWORD", "******"), entry("user", "humptydumpty"));
	}

	@Test
	void verifySerializeWithOnlyRequiredProperties() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		Registration registration = Registration.create("test", "http://localhost:9080/heath").build();

		InstanceRegisteredEvent event = new InstanceRegisteredEvent(id, 0L, timestamp, registration);

		JsonContent<InstanceRegisteredEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(0);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("REGISTERED");
		assertThat(jsonContent).extractingJsonPathValue("$.registration").isNotNull();

		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.name").isEqualTo("test");
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.managementUrl").isNull();
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.healthUrl")
			.isEqualTo("http://localhost:9080/heath");
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.serviceUrl").isNull();
		assertThat(jsonContent).extractingJsonPathStringValue("$.registration.source").isNull();
		assertThat(jsonContent).extractingJsonPathMapValue("$.registration.metadata").isEmpty();
	}

	@Test
	void verifySerializeWithoutRegistration() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		InstanceRegisteredEvent event = new InstanceRegisteredEvent(id, 12345678L, timestamp, null);

		JsonContent<InstanceRegisteredEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(12345678);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("REGISTERED");
		assertThat(jsonContent).extractingJsonPathValue("$.registration").isNull();
	}

}
