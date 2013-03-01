/**
 * 
 */
/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.source;

import java.util.List;

import org.culturegraph.mf.framework.DefaultSender;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.types.Event;


/**
 * Replays the events from an event list.
 * 
 * @author Christoph Böhme
 *
 */
public final class EventListSource extends DefaultSender<StreamReceiver> {

	private List<Event> events;
	
	public EventListSource() {
		this(null);
	}
	
	public EventListSource(final List<Event> events) {
		super();
		this.events = events;
	}

	public void setEvents(final List<Event> events) {
		this.events = events;
	}
	
	public void replay() {
		if (events == null) {
			throw new IllegalStateException("No event list set");
		}
		
		for (Event ev: events) {
			switch (ev.getType()) {
			case START_RECORD:
				getReceiver().startRecord(ev.getName());
				break;
			case END_RECORD:
				getReceiver().endRecord();
				break;
			case START_ENTITY:
				getReceiver().startEntity(ev.getName());
				break;
			case END_ENTITY:
				getReceiver().endEntity();
				break;
			case LITERAL:
				getReceiver().literal(ev.getName(), ev.getValue());
				break;
			default:
				throw new IllegalStateException();
			}
		}
	}
	
}
