/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.metamorph.api;

/**
 * Interface for classes which create interceptors for Metamorph.
 * <p>
 * Interceptors are added into the Metamorph transformation pipeline during
 * its construction.
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
	 * argument and intercepts calls of the {@link FlushListener#flush} method.
	 *
	 * @param listener flush listener object whose invocations should be
	 *                 intercepted.
	 * @return an interceptor object which should be registered in place of the
	 * original {@code listener}.
	 */
	FlushListener createFlushInterceptor(FlushListener listener);

}
