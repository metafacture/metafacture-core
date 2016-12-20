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
package org.culturegraph.mf.metamorph;

import org.culturegraph.mf.metamorph.api.FlushListener;
import org.culturegraph.mf.metamorph.api.InterceptorFactory;
import org.culturegraph.mf.metamorph.api.NamedValuePipe;

/**
 * An implementation of {@link InterceptorFactory} which returns no
 * interceptors. It can be used as a default implementation when no interception
 * should occur.
 *
 * @author Christoph Böhme
 *
 */
final class NullInterceptorFactory implements InterceptorFactory {

	@Override
	public NamedValuePipe createNamedValueInterceptor() {
		return null;
	}

	@Override
	public FlushListener createFlushInterceptor(final FlushListener listener) {
		return null;
	}

}
