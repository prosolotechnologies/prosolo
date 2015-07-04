/**
 * 
 */
package org.prosolo.services.event;

/**
 * Special kind of Exception thrown from within classes working with events.
 * 
 * @author Nikola Milikic
 * 
 */
public class EventException extends Exception {

	private static final long serialVersionUID = -7390723307662585914L;

	public EventException(String message) {
		super(message);
	}
}
