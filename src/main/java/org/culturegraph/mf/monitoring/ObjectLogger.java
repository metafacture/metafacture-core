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
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logs the string representation of every object.
 *
 * @param <T> object type
 *
 * @author Christoph Böhme
 *
 */
@Description("logs objects with the toString method")
@In(Object.class)
@Out(Object.class)
@FluxCommand("log-object")
public final class ObjectLogger<T>
		extends DefaultObjectPipe<T, ObjectReceiver<T>> {

	private static final Logger LOG = LoggerFactory.getLogger(ObjectLogger.class);

	private final String logPrefix;

	public ObjectLogger() {
		this("");
	}

	public ObjectLogger(final String logPrefix) {
		super();
		this.logPrefix = logPrefix;
	}

	@Override
	public void process(final T obj) {
		LOG.debug("{}{}", logPrefix, obj);
		if (getReceiver() != null) {
			getReceiver().process(obj);
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
