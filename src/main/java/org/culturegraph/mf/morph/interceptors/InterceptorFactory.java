/*
 *  Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.morph.interceptors;

import org.culturegraph.mf.morph.FlushListener;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.morph.MorphBuilder;
import org.culturegraph.mf.morph.NamedValuePipe;

/**
 * Interface for classes which create interceptors for Metamorph. When creating
 * an instance of {@link Metamorph} an implementation of
 * {@link InterceptorFactory} can be passed to it. During the construction of
 * the transformation pipeline the {@link MorphBuilder} adds the interceptor
 * objects returned by the factory to the pipeline.
 *
 * An interceptor factory can return {@code null} when asked for creating an
 * interceptor. The {@link NullInterceptorFactory} implements this behaviour and
 * serves as a default implementation of {@link InterceptorFactory} which
 * creates a Metamorph pipeline without any interceptors.
 *
 * @author Christoph Böhme
 *
 */
public interface InterceptorFactory {

	/**
	 * Returns an interceptor which is placed between a NamedValueSource and a
	 * NamedValueReceiver to intercept named values passed between the two
	 * objects.
	 *
	 * @return an interceptor object
	 */
	NamedValuePipe createNamedValueInterceptor();

	/**
	 * Returns an interceptor which wraps the flush listener passed as an
	 * argument and intercepts calls of the {@link FlushListener.flush} method.
	 *
	 * @param listener
	 *            flush listener object whose invokations should be intercepted.
	 * @return an interceptor object which should be registered with the
	 *         {@link Metamorph} object in place of the original
	 *         {@code listener}.
	 */
	FlushListener createFlushInterceptor(FlushListener listener);

}
