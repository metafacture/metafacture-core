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
package org.culturegraph.mf.javaintegration.pojo;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayDeque;
import java.util.Deque;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Creates and fills a new object instance with stream data and sends the result
 * to the given object receiver.
 *
 * @param <T> The type of the object to create.
 * @author Thomas Seidel
 */
@Description("Creates a pojo (Plain Old Java Object) based on a record containing the member values")
@In(StreamReceiver.class)
@Out(Object.class)
@FluxCommand("encode-pojo")
public class PojoEncoder<T> extends DefaultStreamPipe<ObjectReceiver<T>> {

	static {
		// Initialize the property manager to map the primitive data types to
		// the corresponding object based types, e.g. int to Integer
		PropertyEditorManager.registerEditor(Boolean.class,
				PropertyEditorManager.findEditor(boolean.class).getClass());
		PropertyEditorManager.registerEditor(Integer.class,
				PropertyEditorManager.findEditor(int.class).getClass());
		PropertyEditorManager.registerEditor(Long.class, PropertyEditorManager
				.findEditor(long.class).getClass());
	}

	private final TypeEncoderFactory typeEncoderFactory = new TypeEncoderFactory();
	private final Deque<TypeEncoder> typeEncoderStack = new ArrayDeque<>();
	private final Class<T> pojoClass;

	public PojoEncoder(final Class<T> pojoClass) {
		this.pojoClass = pojoClass;
	}

	@Override
	public void startRecord(final String identifier) {
		typeEncoderStack.clear();
		typeEncoderStack.push(new ComplexTypeEncoder(pojoClass));
	}

	@Override
	public void endRecord() {
		assert typeEncoderStack.size() == 1;
		@SuppressWarnings("unchecked")
		final T instance = (T) typeEncoderStack.peek().getInstance();
		getReceiver().process(instance);
		typeEncoderStack.clear();
	}

	@Override
	public void startEntity(final String name) {
		final TypeEncoder currentTypeEncoder = typeEncoderStack.peek();
		final ValueType newType = currentTypeEncoder.getValueType(name);
		final TypeEncoder newTypeEncoder = typeEncoderFactory.create(newType);
		currentTypeEncoder.setValue(name, newTypeEncoder.getInstance());
		typeEncoderStack.push(newTypeEncoder);
	}

	@Override
	public void endEntity() {
		typeEncoderStack.pop();
	}

	@Override
	public void literal(final String name, final String value) {
		final TypeEncoder currentTypeEncoder = typeEncoderStack.peek();
		final Class<?> targetType = currentTypeEncoder.getValueType(name)
				.getRawClass();
		currentTypeEncoder.setValue(name,
				createObjectFromString(value, targetType));
	}

	private static Object createObjectFromString(final String value,
			final Class<?> targetType) {
		final PropertyEditor propertyEditor = PropertyEditorManager
				.findEditor(targetType);
		propertyEditor.setAsText(value);
		return propertyEditor.getValue();
	}

	@Override
	public void onCloseStream() {
		typeEncoderStack.clear();
	}

	@Override
	public void onResetStream() {
		typeEncoderStack.clear();
	}

}
