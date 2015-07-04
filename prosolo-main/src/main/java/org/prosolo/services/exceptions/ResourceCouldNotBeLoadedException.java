/**
 * 
 */
package org.prosolo.services.exceptions;

import org.prosolo.domainmodel.general.BaseEntity;

/**
 * @author Nikola Milikic
 *
 */
public class ResourceCouldNotBeLoadedException extends Exception {

	private static final long serialVersionUID = -4000762419897903741L;

	public ResourceCouldNotBeLoadedException(String resourceUri, Class<? extends BaseEntity> clazz) {
		this("Could not load resource with URI: "+resourceUri+" of a type: "+clazz.getCanonicalName());
	}
	
	public ResourceCouldNotBeLoadedException(String message) {
		super(message);
	}

	/**
	 * @param id
	 * @param clazz
	 */
	public ResourceCouldNotBeLoadedException(long id, Class<?> clazz) {
		this("Could not load resource with id: "+id+" of a type: "+clazz.getCanonicalName());
	}
}
