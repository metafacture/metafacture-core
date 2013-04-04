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
package org.culturegraph.mf.stream.sink;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.culturegraph.mf.exceptions.ValidationException;
import org.culturegraph.mf.exceptions.WellformednessException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.types.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Validates an stream of events using a list of expected stream events.
 * If the stream is invalid a {@link ValidationException} is thrown. If
 * the stream is not well-formed a {@link WellformednessException} is 
 * thrown.
 *  
 * @see WellformednessChecker
 * @see EventList
 * @see ValidationException
 * @see WellformednessException
 *  
 * @author Christoph Böhme
 * 
 */
public final class StreamValidator implements StreamReceiver {

	private static final String CANNOT_CHANGE_OPTIONS = "Cannot change options during validation";
	private static final String VALIDATION_FAILED = "Validation failed. Please reset the validator";

	private static final String NO_RECORD_FOUND = "No record found: ";
	private static final String NO_ENTITY_FOUND = "No entity found: ";
	private static final String NO_LITERAL_FOUND = "No literal found: ";
	private static final String UNCONSUMED_RECORDS_FOUND = "Unconsumed records found";

	private static final Logger LOG = LoggerFactory.getLogger(StreamValidator.class);

	/**
	 * Internal representation of stream events
	 */
	private static final class EventNode {
		private static final String SEPARATOR = ", ";
		private static final String CONSUMED_INDICATOR = "<OK>";
		private final Event event;
		private final EventNode parent;
		private final List<EventNode> children;

		private boolean consumed;

		public EventNode(final Event event, final EventNode parent) {
			this.event = event;
			this.parent = parent;
			// The null-event is used to indicate the stream-start:
			if (this.event == null || this.event.getType() == Event.Type.START_RECORD
					|| this.event.getType() == Event.Type.START_ENTITY) {
				children = new LinkedList<EventNode>();
			} else {
				children = null;
			}

			consumed = false;
		}

		public Event getEvent() {
			return event;
		}

		public EventNode getParent() {
			return parent;
		}

		public List<EventNode> getChildren() {
			return children;
		}

		public boolean isConsumed() {
			return consumed;
		}

		public void setConsumed(final boolean consumed) {
			this.consumed = consumed;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			final String consumedIndicator;
			if (consumed) {
				consumedIndicator = CONSUMED_INDICATOR;
			} else {
				consumedIndicator = "";
			}

			if (event == null) {
				appendChildren(builder);
			} else {
				switch (event.getType()) {
				case START_RECORD:
					builder.append(event.getName() + consumedIndicator + "{");
					appendChildren(builder);
					builder.append("}");
					break;

				case START_ENTITY:
					builder.append(event.getName() + consumedIndicator + "[");
					appendChildren(builder);
					builder.append("]");
					break;

				case LITERAL:
					builder.append(event.getName() + "=" + event.getValue() + consumedIndicator);
					break;

				default:
					break;
				}
			}
			return builder.toString();
		}

		private void appendChildren(final StringBuilder builder) {
			String sep = "";
			for (EventNode e : children) {
				builder.append(sep);
				builder.append(e.toString());
				sep = SEPARATOR;
			}
		}
	}

	private final EventNode eventStream;
	private final Stack<List<EventNode>> stack = new Stack<List<EventNode>>();
	private boolean validating;
	private boolean validationFailed;

	private boolean strictRecordOrder;
	private boolean strictKeyOrder;
	private boolean strictValueOrder;

	private final WellFormednessChecker wellFormednessChecker = new WellFormednessChecker();

	public StreamValidator(final List<Event> expectedStream) {
		this.eventStream = new EventNode(null, null);
		foldEventStream(this.eventStream, expectedStream.iterator());

		resetStream();
	}

	public boolean isStrictRecordOrder() {
		return strictRecordOrder;
	}

	public void setStrictRecordOrder(final boolean strictRecordOrder) {
		if (validating) {
			throw new IllegalStateException(CANNOT_CHANGE_OPTIONS);
		}

		this.strictRecordOrder = strictRecordOrder;
	}

	public boolean isStrictKeyOrder() {
		return strictKeyOrder;
	}

	public void setStrictKeyOrder(final boolean strictKeyOrder) {
		if (validating) {
			throw new IllegalStateException(CANNOT_CHANGE_OPTIONS);
		}

		this.strictKeyOrder = strictKeyOrder;
	}

	public boolean isStrictValueOrder() {
		return strictValueOrder;
	}

	public void setStrictValueOrder(final boolean strictValueOrder) {
		if (validating) {
			throw new IllegalStateException(CANNOT_CHANGE_OPTIONS);
		}

		this.strictValueOrder = strictValueOrder;
	}

	@Override
	public void startRecord(final String identifier) {
		if (validationFailed) {
			throw new ValidationException(VALIDATION_FAILED);
		}

		wellFormednessChecker.startRecord(identifier);

		validating = true;

		if (!openGroups(Event.Type.START_RECORD, identifier, strictRecordOrder, false)) {
			validationFailed = true;
			logEventStream();
			throw new ValidationException(NO_RECORD_FOUND + identifier);
		}
	}

	@Override
	public void endRecord() {
		if (validationFailed) {
			throw new ValidationException(VALIDATION_FAILED);
		}

		wellFormednessChecker.endRecord();

		if (!closeGroups()) {
			validationFailed = true;
			logEventStream();
			throw new ValidationException(NO_RECORD_FOUND + "No record matched the sequence of stream events");
		}

	}

	@Override
	public void startEntity(final String name) {
		if (validationFailed) {
			throw new ValidationException(VALIDATION_FAILED);
		}

		wellFormednessChecker.startEntity(name);

		if (!openGroups(Event.Type.START_ENTITY, name, strictKeyOrder, strictValueOrder)) {
			validationFailed = true;
			logEventStream();
			throw new ValidationException(NO_ENTITY_FOUND + name);
		}
	}

	@Override
	public void endEntity() {
		if (validationFailed) {
			throw new ValidationException(VALIDATION_FAILED);
		}

		wellFormednessChecker.endEntity();

		if (!closeGroups()) {
			validationFailed = true;
			logEventStream();
			throw new ValidationException(NO_ENTITY_FOUND + "No entity matched the sequence of stream events");
		}
	}

	@Override
	public void literal(final String name, final String value) {
		if (validationFailed) {
			throw new ValidationException(VALIDATION_FAILED);
		}

		wellFormednessChecker.literal(name, value);

		final List<EventNode> stackFrame = stack.peek();

		final Iterator<EventNode> iter = stackFrame.iterator();
		while (iter.hasNext()) {
			final EventNode eventNode = iter.next();
			if (!consumeLiteral(eventNode, name, value)) {
				resetGroup(eventNode);
				iter.remove();
			}
		}

		if (stackFrame.isEmpty()) {
			validationFailed = true;
			logEventStream();
			throw new ValidationException(NO_LITERAL_FOUND + name + "=" + value);
		}
	}

	@Override
	public void resetStream() {
		wellFormednessChecker.resetStream();

		validating = false;
		validationFailed = false;

		stack.clear();
		stack.push(new LinkedList<EventNode>());
		stack.peek().add(eventStream);	
	}
	
	@Override
	public void closeStream() {
		if (validationFailed) {
			throw new ValidationException(VALIDATION_FAILED);
		}

		wellFormednessChecker.closeStream();

		validating = false;

		stack.pop();

		if (isGroupConsumed(eventStream)) {
			eventStream.setConsumed(true);
		} else {
			validationFailed = true;
			logEventStream();
			throw new ValidationException(UNCONSUMED_RECORDS_FOUND);
		}
	}

	private void foldEventStream(final EventNode parent, final Iterator<Event> eventStream) {
		while (eventStream.hasNext()) {
			final Event event = eventStream.next();
			if (event.getType() == Event.Type.LITERAL) {
				parent.getChildren().add(new EventNode(event, parent));
			} else if (event.getType() == Event.Type.START_RECORD || event.getType() == Event.Type.START_ENTITY) {
				final EventNode newNode = new EventNode(event, parent);
				parent.getChildren().add(newNode);
				foldEventStream(newNode, eventStream);
			} else if (event.getType() == Event.Type.END_RECORD || event.getType() == Event.Type.END_ENTITY) {
				return;
			}
		}
	}

	private boolean openGroups(final Event.Type type, final String name, final boolean strictKeyOrder,
			final boolean strictValueOrder) {
		final List<EventNode> stackFrame = stack.peek();
		stack.push(new LinkedList<EventNode>());

		final Iterator<EventNode> iter = stackFrame.iterator();
		while (iter.hasNext()) {
			final EventNode eventNode = iter.next();
			if (!consumeGroups(eventNode, type, name, strictKeyOrder, strictValueOrder)) {
				resetGroup(eventNode);
				iter.remove();
			}
		}

		return !stackFrame.isEmpty();
	}

	private boolean closeGroups() {
		EventNode lastMatchParent = null;
		final Iterator<EventNode> iter = stack.pop().iterator();
		while (iter.hasNext()) {
			final EventNode eventNode = iter.next();
			if (eventNode.getParent() != lastMatchParent && isGroupConsumed(eventNode)) {
				eventNode.setConsumed(true);
				lastMatchParent = eventNode.getParent();
			} else {
				resetGroup(eventNode);
			}
		}

		return lastMatchParent != null;
	}

	private boolean consumeGroups(final EventNode group, final Event.Type type, final String name,
			final boolean strictKeyOrder, final boolean strictValueOrder) {
		boolean foundMatch = false;
		for (EventNode c : group.getChildren()) {
			if (!c.isConsumed()) {
				final Event event = c.getEvent();
				if (compare(name, event.getName())) {
					if (event.getType() == type) {
						stack.peek().add(c);
						foundMatch = true;
					} else if (strictValueOrder) {
						break;
					}
				}
				if (strictKeyOrder) {
					break;
				}
			}
		}
		return foundMatch;
	}

	private boolean consumeLiteral(final EventNode group, final String name, final String value) {
		boolean foundMatch = false;
		for (EventNode eventNode : group.getChildren()) {
			if (!eventNode.isConsumed()) {
				final Event event = eventNode.getEvent();
				if (compare(name, event.getName())) {
					if (event.getType() == Event.Type.LITERAL && compare(value, event.getValue())) {
						eventNode.setConsumed(true);
						foundMatch = true;
						break;
					} else if (strictValueOrder) {
						break;
					}
				} else if (strictKeyOrder) {
					break;
				}
			}
		}
		return foundMatch;
	}

	private boolean isGroupConsumed(final EventNode group) {
		boolean consumed = true;
		for (EventNode c : group.getChildren()) {
			consumed = consumed && c.isConsumed();
		}
		return consumed;
	}

	private void resetGroup(final EventNode group) {
		if (group.getChildren() != null) {
			for (EventNode c : group.getChildren()) {
				resetGroup(c);
				c.setConsumed(false);
			}
		}
	}

	private boolean compare(final String str1, final String str2) {
		if (str1 == null) {
			return str2 == null;
		}
		return str1.equals(str2);
	}

	private void logEventStream() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Event Stream: " + eventStream.toString());
		}
	}

}
