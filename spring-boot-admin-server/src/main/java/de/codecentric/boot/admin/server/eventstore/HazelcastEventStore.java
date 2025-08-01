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

package de.codecentric.boot.admin.server.eventstore;

import java.util.List;

import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * Event-Store backed by a Hazelcast-map.
 *
 * @author Johannes Edmeier
 */
public class HazelcastEventStore extends ConcurrentMapEventStore {

	private static final Logger log = LoggerFactory.getLogger(HazelcastEventStore.class);

	public HazelcastEventStore(IMap<InstanceId, List<InstanceEvent>> eventLogs) {
		this(100, eventLogs);
	}

	public HazelcastEventStore(int maxLogSizePerAggregate, IMap<InstanceId, List<InstanceEvent>> eventLog) {
		super(maxLogSizePerAggregate, eventLog);

		eventLog.addEntryListener(new EntryAdapter<InstanceId, List<InstanceEvent>>() {
			@Override
			public void entryUpdated(EntryEvent<InstanceId, List<InstanceEvent>> event) {
				log.debug("Updated {}", event);
				long lastKnownVersion = getLastVersion(event.getOldValue());
				List<InstanceEvent> newEvents = event.getValue()
					.stream()
					.filter((e) -> e.getVersion() > lastKnownVersion)
					.toList();
				HazelcastEventStore.this.publish(newEvents);
			}
		}, true);
	}

}
