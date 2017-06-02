package org.prosolo.services.nodes.data.resourceAccess;

import java.io.Serializable;

public class RestrictedAccessResult<T> implements Serializable {

	private static final long serialVersionUID = -3718705470521304506L;
	
	private final T resource;
	private final ResourceAccessData access;
	
	private RestrictedAccessResult(T resource, ResourceAccessData access) {
		this.resource = resource;
		this.access = access;
	}

	public static <T> RestrictedAccessResult<T> of(T resource, ResourceAccessData access) {
		return new RestrictedAccessResult<T>(resource, access);
	}
	
	public T getResource() {
		return resource;
	}

	public ResourceAccessData getAccess() {
		return access;
	}

}
