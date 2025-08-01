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

package de.codecentric.boot.admin.server.domain.events;

import java.io.Serial;
import java.time.Instant;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * This event gets emitted when an instance is unregistered.
 *
 * @author Johannes Edmeier
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceDeregisteredEvent extends InstanceEvent {

	public static final String TYPE = "DEREGISTERED";

	@Serial
	private static final long serialVersionUID = 1L;

	public InstanceDeregisteredEvent(InstanceId instance, long version) {
		this(instance, version, Instant.now());
	}

	public InstanceDeregisteredEvent(InstanceId instance, long version, Instant timestamp) {
		super(instance, version, TYPE, timestamp);
	}

}
