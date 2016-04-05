package org.prosolo.services.common.observable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.prosolo.services.common.comparable.Comparable;


public class StandardObservable extends Observable {

	protected Map<String, Object> changedAttributes = new HashMap<>();
	/** when true, boolean flags in setter
	 *  methods will be used to determine if
	 *  attribute value is changed */
	protected boolean listenChanges;
	
	@Override
	public boolean hasObjectChanged() {
		return !changedAttributes.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Stores attribute in a collection of changed attributes 
	 * if its value is changed, removes it from a collection
	 * if value has been changed earlier, but updated again to initial value.
	 * 
	 * @param compareFunction {@inheritDoc} If null, default equality check is used
	 * {@link Objects#equals(Object, Object)} 
	 */
	@Override
	final <T> void observeChange(String fieldName, T oldValue, T newValue, 
			Comparable<T> compareFunction) {
		if(listenChanges) {
			Comparable<T> compFunctionFinal = compareFunction == null ? 
					Objects::equals : compareFunction;
			UpdateAction action = getActionForAttribute(fieldName, oldValue, newValue, compFunctionFinal);
			switch(action) {
				case ADD:
					changedAttributes.put(fieldName, oldValue);
					return;
				case REMOVE:
					changedAttributes.remove(fieldName);
					return;
				case NOTHING:
					return;
			}
		}
	}

	/**
	 * Determines which action should be taken with attribute 
	 * in a collection of changed attributes
	 * @param fieldName
	 * @param oldValue
	 * @param newValue
	 * @param compareFunction
	 * @return
	 */
	private <T> UpdateAction getActionForAttribute(String fieldName, T oldValue, T newValue,
			Comparable<T> compareFunction) {
		if(!compareFunction.equals(oldValue, newValue)) {
			@SuppressWarnings("unchecked")
			T val = (T) changedAttributes.get(fieldName);
			if(val == null) {
				boolean keyExists = changedAttributes.containsKey(fieldName);
				if(!keyExists) {
					return UpdateAction.ADD;
				}
			}
			if(compareFunction.equals(val, newValue)) {
				return UpdateAction.REMOVE;
			}
		}
		return UpdateAction.NOTHING;
	}
	
	@Override
	public void startObservingChanges() {
		this.changedAttributes.clear();
		this.listenChanges = true;
	}

	@Override
	public void stopObservingChanges() {
		this.changedAttributes.clear();
		this.listenChanges = false;
	}
	
	@Override
	boolean shouldObserveChanges() {
		return listenChanges;
	}
	
	public static void main(String [] args) {
		StandardObservable so = new StandardObservable();
	
		so.startObservingChanges();
		so.observeAttributeChange("stef", 1, 5);
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef1", null, "ste");
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef2", true, false);
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef", 5, 1);
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef1", "ste", "stef");
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef2", false, false);
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef2", false, true);
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef1", "stef", "st");
		System.out.println(so.changedAttributes);
		so.observeAttributeChange("stef1", "st", null);
		System.out.println(so.changedAttributes);
		
	}
	
}
