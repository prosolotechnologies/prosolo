/**
 * 
 */
package org.prosolo.core.spring.security;

import java.io.Serializable;

import org.springframework.security.authentication.ProviderManager;

/**
 * @author "Nikola Milikic"
 *
 */
@SuppressWarnings("deprecation")
public class SerializableProviderManager extends ProviderManager implements Serializable {

	private static final long serialVersionUID = -3909790712711070390L;

}
