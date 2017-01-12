package org.prosolo.services.common.observable;

import org.prosolo.services.common.comparable.Comparable;

/**
 * Provides possibility to track attribute changes
 */
public abstract class Observable {

	/** 
	 * this method returns true if any of
	 * the attributes that are being tracked 
	 * is changed
	 */
	public abstract boolean hasObjectChanged();
	
	/** when attribute changes need to be
	 * tracked this method should be called */
	public abstract void startObservingChanges();
	
	/** when you want to stop tracking 
	 * attribute changes this method
	 * should be called
	 */
	public abstract void stopObservingChanges();
	
	/**
	 * Observes attribute change. This method should be called from a 
	 * attribute setter.
	 * @param fieldName name of the attribute that is being tracked
	 * @param oldValue attribute value before new value is set
	 * @param newValue new attribute value that should be set
	 * @param compareFunction object of a class that implements 
	 * {@link org.prosolo.services.common.comparable.Comparable}.
	 * If not null, it is used to determine equality of attribute
	 * values.
	 */
	abstract <T> void observeChange(String fieldName, T oldValue, T newValue, 
			Comparable<T> compareFunction);
	
	/**
	 * Returns true if attribute changes are being tracked
	 */
	abstract boolean shouldObserveChanges();
	
	/**
	 * If attributes are being tracked, observes attribute change.
	 * This method should be called from attribute setter.
	 * @param fieldName name of the attribute that is being tracked
	 * @param oldValue attribute value before new value is set
	 * @param newValue new attribute value that should be set
	 * @param compareFunction object of a class that implements 
	 * {@link org.prosolo.services.common.comparable.Comparable}.
	 * If not null, it is used to determine equality of attribute
	 * values.
	 */
	public final <T> void observeAttributeChange(String fieldName, T oldValue, T newValue, 
			Comparable<T> compareFunction) {
		if(shouldObserveChanges()) {
			observeChange(fieldName, oldValue, newValue, compareFunction);
		}
	}
	
	/**
	 * If attributes are being tracked, observes attribute change. 
	 * This method should be called from attribute setter.
	 * @param fieldName name of the attribute that is being tracked
	 * @param oldValue attribute value before new value is set
	 * @param newValue object of a class that implements
	 */
	public <T> void observeAttributeChange(String fieldName, T oldValue, T newValue) {
		observeAttributeChange(fieldName, oldValue, newValue, null);
	}

}
