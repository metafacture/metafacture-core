package org.culturegraph.mf.morph.functions.model;

/**
 * @author tgaengler
 */
public enum ValueType {

	/**
	 * Type for resources.
	 */
	Resource("RESOURCE"),

	/**
	 * Type for bnodes.
	 */
	BNode("BNODE"),

	/**
	 * Type for literals.
	 */
	Literal("LITERAL"),

	/**
	 * Type for converting errors.
	 */
	Error("ERROR");

	/**
	 * The name of the value type.
	 */
	private final String name;

	/**
	 * Gets the name of the value type.
	 *
	 * @return the name of the value type
	 */
	public String getName() {

		return name;
	}

	/**
	 * Creates a new value type with the given name.
	 *
	 * @param nameArg the name of the value type.
	 */
	private ValueType(final String nameArg) {

		this.name = nameArg;
	}

	/**
	 * Gets the node type by the given name.<br>
	 *
	 * @param name the name of the value type
	 * @return the appropriated value type
	 */
	public static ValueType getByName(final String name) {

		for (final ValueType functionType : ValueType.values()) {

			if (functionType.name.equals(name)) {

				return functionType;
			}
		}

		throw new IllegalArgumentException(name);
	}

	/**
	 * {@inheritDoc}<br>
	 * Returns the name of the node type.
	 *
	 * @see Enum#toString()
	 */
	@Override
	public String toString() {

		return name;
	}
}
