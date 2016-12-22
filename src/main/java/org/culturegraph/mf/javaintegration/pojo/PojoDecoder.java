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
package org.culturegraph.mf.javaintegration.pojo;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Outputs a record by decoding the members of a given pojo (Plain Old Java
 * Object). Each public getter or public field defines a member. The member name
 * is either the getter name without the get prefix or the field name.
 * <p>
 * The generated stream events depend on the member type:
 * <ul>
 *   <li>Simple types like Strings or the primitive types produces a literal
 *   event with the member name as literal name and the member value as literal
 *   value
 *
 *   <li>Instances of {@link MetafactureSource} can insert user defined events
 *   into the stream by implementing the {@link MetafactureSource#sendToStream}
 *   method.
 *
 *   <li>Complex types like other pojos produces an entity with the member name
 *   as entity name and the decoded pojo members as the entity's elements
 *
 *   <li>Lists, Arrays and Sets produce events for each entry according to the
 *   rules above. Each of these events is named by the member name.
 *
 *   <li>Maps produces an entity with the member name as entity name. Each map
 *   entry produces a sub entity with the string representation of the entry key
 *   as entity name and the entry value decoded to the rules above.
 * </ul>
 * <p>
 * Here are some examples:
 * <ul>
 *   <li>{@code String str = "abc" &rarr; literal("str", "abc")}
 *
 *   <li>{@code boolean bo = true &rarr; literal("bo", "true")}
 *
 *   <li>{@code List&lt;String&gt; li = &hellip; ("a", "b") &rarr; literal("li", "a"), literal("li", "b")}
 *
 *   <li>{@code String[] ar = &hellip; ("a", "b") &rarr; literal("ar", "a"), literal("ar", "b")}
 *
 *   <li>{@code Set&lt;String&gt; se = &hellip; ("a", "b") &rarr; literal("se", "a"), literal("se", "b")}
 *
 *   <li>{@code Map&lt;String, String&gt; ma = &hellip; ("a" : "b", "c" : "d") &rarr; startEntity("ma"), literal("a", "b"), literal("c", "d"), endEntity()}
 * </ul>
 * <p>
 * See in the test cases in {@code PojoDecoderTest} for more examples.
 *
 * @param <T>
 *            input object type
 *
 * @author Thomas Seidel
 *
 */
@Description("Outputs a record containing the member values of the input pojo (Plain Old Java Object)")
@In(Object.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-pojo")
public class PojoDecoder<T> extends DefaultObjectPipe<T, StreamReceiver> {

	private final TypeDecoderFactory typeDecoderFactory = new TypeDecoderFactory();

	@Override
	public void process(final T obj) {
		if (obj == null) {
			return;
		}
		assert !isClosed();
		final TypeDecoder typeDecoder = typeDecoderFactory.create(obj.getClass());
		getReceiver().startRecord("");
		typeDecoder.decodeToStream(getReceiver(), null, obj);
		getReceiver().endRecord();
	}

}
