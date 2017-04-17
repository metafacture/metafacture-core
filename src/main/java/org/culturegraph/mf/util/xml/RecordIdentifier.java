package org.culturegraph.mf.util.xml;

/**
 * This interface declares methods for setting variables used by all record
 * sinks.
 * 
 * @author dr0i
 * 
 */
public interface RecordIdentifier {

	/**
	 * Sets the name property which will be used to create the name of the
	 * record of the sink. The value of this property should lead to a unique
	 * name because it will override existing ones.
	 * 
	 * @param property
	 *            the property which will be used to extract a record name.
	 */
	public void setProperty(final String property);

}
