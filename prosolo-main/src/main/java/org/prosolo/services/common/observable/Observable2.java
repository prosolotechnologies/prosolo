package org.prosolo.services.common.observable;

import org.prosolo.services.common.comparable.Comparable;

/**
 * Provides possibility to track attribute changes
 */
public interface Observable2 {

	/** 
	 * this method returns true if any of
	 * the attributes that are being tracked 
	 * is changed
	 */
	boolean hasObjectChanged();
	
	/** when attribute changes need to be
	 * tracked this method should be called */
	public void startObservingChanges();
	
	/** when you want to stop tracking 
	 * attribute changes this method
	 * should be called
	 */
	public void stopObservingChanges();
	
	/**
	 * Stores attribute in a collection of changed attributes 
	 * if its value is changed, removes it from a collection
	 * if value has been changed, but updated again to initial value.
	 * This method should be called from attribute setter.
	 * @param fieldName name of the attribute that is being tracked
	 * @param oldValue attribute value before new value is set
	 * @param newValue new attribute value that should be set
	 * @param compareFunction object of a class that implements 
	 * {@link org.prosolo.services.common.comparable.Comparable}.
	 * If not null, it is used to determine equality of attribute
	 * values.
	 */
	<T> void observeChange(String fieldName, T oldValue, T newValue, 
			Comparable<T> compareFunction);
	
	/**
	 * Observes attribute change. This method should be called from a 
	 * attribute setter.
	 * @param fieldName name of the attribute that is being tracked
	 * @param oldValue attribute value before new value is set
	 * @param newValue object of a class that implements
	 */
	<T> void observeChange(String fieldName, T oldValue, T newValue);

}
