/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.culturegraph.mf.monitoring;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Leaves the event stream untouched but logs it to the info log.
 * The {@link StreamReceiver} may be {@code null}.
 * In this case {@link StreamLogger} behaves as a sink, just logging.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("logs events")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("log-stream")
public final class StreamLogger
		extends DefaultStreamPipe<StreamReceiver> {

	private static final Logger LOG = LoggerFactory.getLogger(StreamLogger.class);

	private final String logPrefix;

	public StreamLogger() {
		this("");
	}

	public StreamLogger(final String logPrefix) {
		super();
		this.logPrefix = logPrefix;
	}

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		LOG.debug("{}start record {}", logPrefix, identifier);
		if (null != getReceiver()) {
			getReceiver().startRecord(identifier);
		}
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		LOG.debug("{}end record", logPrefix);
		if (null != getReceiver()) {
			getReceiver().endRecord();
		}
	}

	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		LOG.debug("{}start entity {}", logPrefix, name);
		if (null != getReceiver()) {
			getReceiver().startEntity(name);
		}
	}

	@Override
	public void endEntity() {
		assert !isClosed();
		LOG.debug("{}end entity", logPrefix);
		if (null != getReceiver()) {
			getReceiver().endEntity();
		}

	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		LOG.debug("{}literal {}={}", logPrefix, name, value);
		if (null != getReceiver()) {
			getReceiver().literal(name, value);
		}
	}

	@Override
	protected void onResetStream() {
		LOG.debug("{}resetStream", logPrefix);
	}

	@Override
	protected void onCloseStream() {
		LOG.debug("{}closeStream", logPrefix);
	}

}
