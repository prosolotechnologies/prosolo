package org.prosolo.util.urigenerator;

import java.net.URI;
import java.util.UUID;

import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * This generator is based on a immutable universally unique identifier (UUID).
 * A UUID represents a 128-bit value. URI is created by appending randomly
 * generated UUID to the given namespace.
 * 
 */
public class UUIDBasedUriGenerator {

	public static <E extends BaseEntity> URI generateInstanceURI(E instance) {
		String uriString = "http://" + instance.getClass().getSimpleName() + "/" + UUID.randomUUID();
		return URI.create(uriString);
	}
	
	public static String getSimpleNameFromUri(URI uri) {
		return getSimpleNameFromUri(uri.toString());
	}
	
	public static String getSimpleNameFromUri(String uri) {
		// cutting off leading http://
		uri = uri.replace("http://", "");
		
		// taking text from the beginning to the first occurrence of slash (/)
		return uri.substring(0, uri.indexOf("/"));
	}

}
