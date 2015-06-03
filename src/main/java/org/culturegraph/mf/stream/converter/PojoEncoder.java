package org.culturegraph.mf.stream.converter;

import java.beans.Introspector;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates and fills a new object instance with stream data and sends
 * the result to the given object receiver.
 * 
 * @author Thomas Seidel
 * 
 * @param <T>
 *            The type of the object to create.
 * 
 */
@Description("Creates a pojo (Plain Old Java Object) based on a record containing the member values")
@In(StreamReceiver.class)
@Out(Object.class)
public class PojoEncoder<T> extends DefaultStreamPipe<ObjectReceiver<T>> {

	private static final Logger LOG = LoggerFactory
			.getLogger(PojoEncoder.class);

	private static class ValueType {
		private final Class<?> rawClass;
		private Class<?> elementClass;

		public ValueType(final Class<?> clazz) {
			rawClass = clazz;
		}

		public ValueType(final Class<?> clazz, final Type type) {
			rawClass = clazz;
			if (type instanceof ParameterizedType) {
				elementClass = (Class<?>) (((ParameterizedType) type)
						.getActualTypeArguments()[0]);
			}
		}

		public Class<?> getRawClass() {
			return rawClass;
		}

		public Class<?> getElementClass() {
			return elementClass;
		}

	}

	/**
	 * A ValueSetter sets a pojos's member, via setter method or field access.
	 * Used by {@link ComplexTypeEncoder} only
	 * 
	 * @author Thomas Seidel
	 * 
	 */
	private interface ValueSetter {

		void setValue(final Object object, final Object value);

		String getName();

		ValueType getValueType();

	}

	private static class MethodValueSetter implements ValueSetter {

		private static final String METHOD_PREFIX = "set";

		private final String name;
		private final Method method;

		public static boolean supportsMethod(final Method m) {
			return Modifier.isPublic(m.getModifiers())
					&& m.getName().length() > METHOD_PREFIX.length()
					&& m.getName().startsWith(METHOD_PREFIX)
					&& m.getParameterTypes().length == 1;
		}

		public MethodValueSetter(final Method method) {
			assert supportsMethod(method);
			this.method = method;
			// remove prefix then lower case first character
			name = Introspector.decapitalize(method.getName().substring(
					METHOD_PREFIX.length()));
		}

		@Override
		public void setValue(final Object object, final Object value) {
			try {
				method.invoke(object, value);
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
		public ValueType getValueType() {
			return new ValueType(method.getParameterTypes()[0],
					method.getGenericParameterTypes()[0]);
		}

	}

	private static class FieldValueSetter implements ValueSetter {

		final Field field;

		public static boolean supportsField(final Field f) {
			return Modifier.isPublic(f.getModifiers());
		}

		public FieldValueSetter(final Field field) {
			assert supportsField(field);
			this.field = field;
		}

		@Override
		public void setValue(final Object object, final Object value) {
			try {
				field.set(object, value);
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
		public ValueType getValueType() {
			return new ValueType(field.getType(), field.getGenericType());
		}

	}

	/**
	 * A TypeEncoder encodes a metafacture stream to a new object
	 * 
	 * @author Thomas Seidel
	 * 
	 */
	private interface TypeEncoder {

		void setValue(String name, Object value);

		ValueType getValueType(String name);

		Object getInstance();

	}

	private final TypeEncoderFactory typeEncoderFactory = new TypeEncoderFactory();

	private static class TypeEncoderFactory {
		private final Map<Class<?>, TypeEncoder> typeEncoders = new HashMap<Class<?>, TypeEncoder>();

		private TypeEncoder create(final ValueType valueType) {
			final TypeEncoder typeEncoder;
			// if (typeEncoders.containsKey(clazz)) {
			// return typeEncoders.get(clazz);
			// }
			final Class<?> rawClass = valueType.getRawClass();
			if (ListTypeEncoder.supportsType(rawClass)) {
				typeEncoder = new ListTypeEncoder(valueType);
			} else if (ComplexTypeEncoder.supportsType(rawClass)) {
				typeEncoder = new ComplexTypeEncoder(rawClass);
			} else {
				throw new MetafactureException("Can't encode type " + rawClass);
			}
			typeEncoders.put(rawClass, typeEncoder);
			LOG.debug("typeEncoders: {})", typeEncoders);
			return typeEncoder;
		}
	}

	private static class ComplexTypeEncoder implements TypeEncoder {

		private final Object instance;
		private final Map<String, ValueSetter> valueSetters;

		public ComplexTypeEncoder(final Class<?> clazz) {
			assert supportsType(clazz);
			instance = createInstance(clazz);
			valueSetters = new HashMap<String, ValueSetter>();
			// get all public fields of this class and all super classes
			final Field[] fields = clazz.getDeclaredFields();
			for (final Field field : fields) {
				if (FieldValueSetter.supportsField(field)) {
					final FieldValueSetter fieldValueSetter = new FieldValueSetter(
							field);
					valueSetters.put(fieldValueSetter.getName(),
							fieldValueSetter);
				}
			}
			// get all valid public methods of this class and all super classes
			final Method[] methods = clazz.getDeclaredMethods();
			for (final Method method : methods) {
				if (MethodValueSetter.supportsMethod(method)) {
					final MethodValueSetter methodValueSetter = new MethodValueSetter(
							method);
					valueSetters.put(methodValueSetter.getName(),
							methodValueSetter);
				}
			}
		}

		public static boolean supportsType(final Class<?> clazz) {
			return !clazz.isPrimitive() && !clazz.equals(String.class)
					&& !ListTypeEncoder.supportsType(clazz);
		}

		@Override
		public void setValue(final String name, final Object value) {
			final ValueSetter valueSetter = valueSetters.get(name);
			valueSetter.setValue(instance, value);
		}

		@Override
		public ValueType getValueType(final String name) {
			final ValueSetter valueSetter = valueSetters.get(name);
			return valueSetter.getValueType();
		}

		@Override
		public Object getInstance() {
			return instance;
		}

	}

	private static class ListTypeEncoder implements TypeEncoder {

		private final ValueType valueType;
		private final List<Object> objects;

		public ListTypeEncoder(final ValueType valueType) {
			this.valueType = valueType;
			objects = new ArrayList<Object>();
		}

		public static boolean supportsType(final Class<?> clazz) {
			return List.class.isAssignableFrom(clazz);
		}

		@Override
		public void setValue(final String name, final Object value) {
			objects.add(value);
		}

		@Override
		public ValueType getValueType(final String name) {
			return new ValueType(valueType.getElementClass());
		}

		@Override
		public Object getInstance() {
			return objects;
		}

	}

	private static Object createObjectFromString(final String value,
			final Class<?> targetType) {
		final PropertyEditor propertyEditor = PropertyEditorManager
				.findEditor(targetType);
		propertyEditor.setAsText(value);
		return propertyEditor.getValue();
	}

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

	private static Object createInstance(final Class<?> clazz) {
		Object object;
		try {
			object = clazz.newInstance();
		} catch (final Exception e) {
			throw new MetafactureException(
					"Can't instantiate object of class: " + clazz, e);
		}
		return object;
	}

	private final Class<T> pojoClass;
	private final Deque<TypeEncoder> typeEncoderStack;

	public PojoEncoder(final Class<T> pojoClass) {
		this.pojoClass = pojoClass;
		typeEncoderStack = new ArrayDeque<TypeEncoder>();
	}

	@Override
	public void startRecord(final String identifier) {
		typeEncoderStack.clear();
		typeEncoderStack.push(new ComplexTypeEncoder(pojoClass));
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
	public void literal(final String name, final String value) {
		final TypeEncoder currentTypeEncoder = typeEncoderStack.peek();
		final Class<?> targetType = currentTypeEncoder.getValueType(name)
				.getRawClass();
		currentTypeEncoder.setValue(name,
				createObjectFromString(value, targetType));
	}

	@Override
	public void endEntity() {
		typeEncoderStack.pop();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void endRecord() {
		assert typeEncoderStack.size() == 1;
		final ObjectReceiver<T> objectReceiver = getReceiver();
		objectReceiver.process((T) typeEncoderStack.peek().getInstance());
		typeEncoderStack.clear();
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
