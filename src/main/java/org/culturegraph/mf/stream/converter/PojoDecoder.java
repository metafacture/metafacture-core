/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream.converter;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outputs a record by decoding the members of a given pojo (Plain Old Java
 * Object). Each public getter or public field defines a member. The member name
 * is either the getter name without the get prefix or the field name.
 * 
 * <p>
 * The generated stream events depend on the member type:
 * <ul>
 * <li>Simple types like Strings or the primitive types produces a literal event
 * with the member name as literal name and the member value as literal value</li>
 * <li>Instances of {@link MetafactureSource} can insert user defined events
 * into the stream by implementing the {@link MetafactureSource#sendToStream}
 * method.
 * <li>Complex types like other pojos produces an entity with the member name as
 * entity name and the decoded pojo members as the entity's elements</li>
 * <li>Lists, Arrays and Sets produce events for each entry according to the
 * rules above. Each of these events is named by the member name.</li>
 * <li>Maps produces an entity with the member name as entity name. Each map
 * entry produces a sub entity with the string representation of the entry key
 * as entity name and the entry value decoded to the rules above.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Here are some examples:
 * <ul>
 * <li>{@code String str = "abc" -> literal("str", "abc")}</li>
 * <li>{@code boolean bo = true -> literal("bo", "true")}</li>
 * <li>
 * {@code List<String> li = ... ("a", "b") -> literal("li", "a"), literal("li", "b")}
 * <li>
 * {@code String[] ar = ... ("a", "b") -> literal("ar", "a"), literal("ar", "b")}
 * </li>
 * <li>
 * {@code Set<String> se = ... ("a", "b") -> literal("se", "a"), literal("se", "b")}
 * </li>
 * <li>
 * {@code Map<String,String> ma = ... ("a" : "b", "c" : "d") -> startEntity("ma"), literal("a", "b"), literal("c", "d"), endEntity()}
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * See {@link PojoDecoderTest} for more examples.
 * </p>
 * 
 * @param <T>
 *            input object type
 * @author Thomas Seidel
 * 
 */
@Description("Outputs a record containing the member values of the input pojo (Plain Old Java Object)")
@In(Object.class)
@Out(StreamReceiver.class)
public class PojoDecoder<T> extends DefaultObjectPipe<T, StreamReceiver> {

	private static final Logger LOG = LoggerFactory
			.getLogger(PojoDecoder.class);

	/**
	 * Use this interfaces to include a metafacture event stream to the pojo
	 * decoder. If the {@link PojoDecoder} detects a type implementing this
	 * interface, it will call the {@link #sendToStream} method.
	 * 
	 * @author Thomas Seidel
	 * 
	 */
	public interface MetafactureSource {
		void sendToStream(final StreamReceiver streamReceiver);
	}

	/**
	 * A ValueGetter retrieves a pojos's member, via getter method or field
	 * access. Used by {@link ComplexTypeDecoder} only.
	 * 
	 * @author Thomas Seidel
	 * 
	 */
	private interface ValueGetter {

		Object getValue(final Object object);

		String getName();

		Class<?> getValueType();

	}

	private static class MethodValueGetter implements ValueGetter {

		private static final String METHOD_PREFIX = "get";

		private final String name;
		private final Method method;

		public static boolean supportsMethod(final Method m) {
			return Modifier.isPublic(m.getModifiers())
					&& m.getName().length() > METHOD_PREFIX.length()
					&& m.getName().startsWith(METHOD_PREFIX);
		}

		public MethodValueGetter(final Method method) {
			assert supportsMethod(method);
			this.method = method;
			// remove prefix then lower case first character
			name = Introspector.decapitalize(method.getName().substring(
					METHOD_PREFIX.length()));
		}

		@Override
		public Object getValue(final Object object) {
			try {
				return method.invoke(object);
			} catch (final IllegalArgumentException e) {
				throw new MetafactureException(
						"The given object don't have a method named "
								+ method.getName(), e);
			} catch (final IllegalAccessException e) {
				throw new MetafactureException("Can't access the method named "
						+ method.getName(), e);
			} catch (final InvocationTargetException e) {
				throw new MetafactureException("Invoking the method named "
						+ method.getName() + " throws an excpetion", e);
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?> getValueType() {
			return method.getReturnType();
		}

	}

	private static class FieldValueGetter implements ValueGetter {

		final Field field;

		public static boolean supportsField(final Field f) {
			return Modifier.isPublic(f.getModifiers());
		}

		public FieldValueGetter(final Field field) {
			assert supportsField(field);
			this.field = field;
		}

		@Override
		public Object getValue(final Object object) {
			try {
				return field.get(object);
			} catch (final IllegalArgumentException e) {
				throw new MetafactureException(
						"The given object don't have a field named "
								+ field.getName(), e);
			} catch (final IllegalAccessException e) {
				throw new MetafactureException("Can't access the field named "
						+ field.getName(), e);
			}
		}

		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public Class<?> getValueType() {
			return field.getType();
		}

	}

	/**
	 * A TypeDecoder decodes an object to a metafacture stream using a given
	 * name.
	 * 
	 * @author Thomas Seidel
	 * 
	 */
	private interface TypeDecoder {

		void decodeToStream(final StreamReceiver streamReceiver,
				final String name, final Object object);

	}

	private final TypeDecoderFactory typeDecoderFactory = new TypeDecoderFactory();

	private static class TypeDecoderFactory {
		private final Map<Class<?>, TypeDecoder> typeDecoders = new HashMap<Class<?>, TypeDecoder>();

		private TypeDecoder create(final Class<?> clazz) {
			if (typeDecoders.containsKey(clazz)) {
				return typeDecoders.get(clazz);
			}
			TypeDecoder typeDecoder;
			if (SimpleTypeDecoder.supportsType(clazz)) {
				typeDecoder = new SimpleTypeDecoder();
			} else if (MetafactureSourceTypeDecoder.supportsType(clazz)) {
				typeDecoder = new MetafactureSourceTypeDecoder();
			} else if (CollectionTypeDecoder.supportsType(clazz)) {
				typeDecoder = new CollectionTypeDecoder(this);
			} else if (ArrayTypeDecoder.supportsType(clazz)) {
				typeDecoder = new ArrayTypeDecoder(this);
			} else if (ComplexTypeDecoder.supportsType(clazz)) {
				typeDecoder = new ComplexTypeDecoder(clazz, this);
			} else if (MapTypeDecoder.supportsType(clazz)) {
				typeDecoder = new MapTypeDecoder(this);
			} else {
				throw new MetafactureException("Can't decode type " + clazz);
			}
			typeDecoders.put(clazz, typeDecoder);
			LOG.debug("typeDecoders: {})", typeDecoders);
			return typeDecoder;
		}
	}

	private static class SimpleTypeDecoder implements TypeDecoder {

		public static boolean supportsType(final Class<?> clazz) {
			return clazz.isPrimitive() || clazz.equals(String.class);
		}

		@Override
		public void decodeToStream(final StreamReceiver streamReceiver,
				final String name, final Object object) {
			streamReceiver.literal(name, object.toString());
		}

	}

	private static class MetafactureSourceTypeDecoder implements TypeDecoder {

		public static boolean supportsType(final Class<?> clazz) {
			return MetafactureSource.class.isAssignableFrom(clazz);
		}

		@Override
		public void decodeToStream(final StreamReceiver streamReceiver,
				final String name, final Object object) {
			final MetafactureSource metafactureSource = (MetafactureSource) object;
			streamReceiver.startEntity(name);
			metafactureSource.sendToStream(streamReceiver);
			streamReceiver.endEntity();
		}

	}

	private static class ComplexTypeDecoder implements TypeDecoder {

		private final TypeDecoderFactory typeDecoderFactury;
		private final List<ValueGetter> valueGetters;

		public static boolean supportsType(final Class<?> clazz) {
			return !SimpleTypeDecoder.supportsType(clazz)
					&& !MetafactureSourceTypeDecoder.supportsType(clazz)
					&& !CollectionTypeDecoder.supportsType(clazz)
					&& !ArrayTypeDecoder.supportsType(clazz)
					&& !MapTypeDecoder.supportsType(clazz);
		}

		public ComplexTypeDecoder(final Class<?> clazz,
				final TypeDecoderFactory typeDecoderFactury) {
			this.typeDecoderFactury = typeDecoderFactury;
			valueGetters = new ArrayList<ValueGetter>();
			// get all public fields of this class and all super classes
			final Field[] fields = clazz.getDeclaredFields();
			for (final Field field : fields) {
				if (FieldValueGetter.supportsField(field)) {
					valueGetters.add(new FieldValueGetter(field));
				}
			}
			// get all valid public methods of this class and all super classes
			final Method[] methods = clazz.getDeclaredMethods();
			for (final Method method : methods) {
				if (MethodValueGetter.supportsMethod(method)) {
					valueGetters.add(new MethodValueGetter(method));
				}
			}
		}

		@Override
		public void decodeToStream(final StreamReceiver streamReceiver,
				final String name, final Object object) {

			if (name != null) {
				streamReceiver.startEntity(name);
			}
			for (final ValueGetter valueGetter : valueGetters) {
				final Object value = valueGetter.getValue(object);
				final Class<?> valueType = valueGetter.getValueType();
				final String valueName = valueGetter.getName();
				final TypeDecoder typeDecoder = typeDecoderFactury
						.create(valueType);
				typeDecoder.decodeToStream(streamReceiver, valueName, value);
			}
			if (name != null) {
				streamReceiver.endEntity();
			}
		}
	}

	private static class CollectionTypeDecoder implements TypeDecoder {

		private final TypeDecoderFactory typeDecoderFactury;

		public CollectionTypeDecoder(final TypeDecoderFactory typeDecoderFactury) {
			this.typeDecoderFactury = typeDecoderFactury;
		}

		public static boolean supportsType(final Class<?> clazz) {
			return Collection.class.isAssignableFrom(clazz);
		}

		@Override
		public void decodeToStream(final StreamReceiver streamReceiver,
				final String name, final Object object) {
			final Collection<?> collection = (Collection<?>) object;
			for (final Object element : collection) {
				final TypeDecoder typeDecoder = typeDecoderFactury
						.create(element.getClass());
				typeDecoder.decodeToStream(streamReceiver, name, element);
			}
		}
	}

	private static class ArrayTypeDecoder implements TypeDecoder {

		private final TypeDecoderFactory typeDecoderFactury;

		public ArrayTypeDecoder(final TypeDecoderFactory typeDecoderFactury) {
			this.typeDecoderFactury = typeDecoderFactury;
		}

		public static boolean supportsType(final Class<?> clazz) {
			return clazz.isArray();
		}

		@Override
		public void decodeToStream(final StreamReceiver streamReceiver,
				final String name, final Object object) {
			final Object[] array = (Object[]) object;
			for (final Object element : array) {
				final TypeDecoder typeDecoder = typeDecoderFactury
						.create(element.getClass());
				typeDecoder.decodeToStream(streamReceiver, name, element);
			}
		}

	}

	private static class MapTypeDecoder implements TypeDecoder {

		private final TypeDecoderFactory typeDecoderFactury;

		public MapTypeDecoder(final TypeDecoderFactory typeDecoderFactury) {
			this.typeDecoderFactury = typeDecoderFactury;
		}

		public static boolean supportsType(final Class<?> clazz) {
			return Map.class.isAssignableFrom(clazz);
		}

		@Override
		public void decodeToStream(final StreamReceiver streamReceiver,
				final String name, final Object object) {
			final Map<?, ?> map = (Map<?, ?>) object;
			if (name != null) {
				streamReceiver.startEntity(name);
			}
			for (final Entry<?, ?> entry : map.entrySet()) {
				final String key = entry.getKey().toString();
				final Object value = entry.getValue();
				final TypeDecoder typeDecoder = typeDecoderFactury.create(value
						.getClass());
				typeDecoder.decodeToStream(streamReceiver, key, value);
			}
			if (name != null) {
				streamReceiver.endEntity();
			}
		}
	}

	@Override
	public void process(final T obj) {
		if (obj == null) {
			return;
		}
		assert !isClosed();
		final TypeDecoder typeDecoder = typeDecoderFactory.create(obj
				.getClass());
		getReceiver().startRecord("");
		typeDecoder.decodeToStream(getReceiver(), null, obj);
		getReceiver().endRecord();
	}

}
