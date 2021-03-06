package org.prosolo.services.nodes.data.resourceAccess;

import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

import java.util.Collection;
import java.util.Date;

public class CredentialUserAccessSpecification extends UserAccessSpecification {

	private final CredentialType type;
	private final Date deliveryStart;
	private final Date deliveryEnd;
	
	private CredentialUserAccessSpecification(Collection<UserGroupPrivilege> privileges, boolean resourceVisibleToAll,
											  CredentialType type, Date deliveryStart, Date deliveryEnd) {
		super(privileges, resourceVisibleToAll);
		this.type = type;
		this.deliveryStart = deliveryStart;
		this.deliveryEnd = deliveryEnd;
	}
	 
	/**
	 * Returns {@link CredentialUserAccessSpecification} object based on provided data.
	 *
	 * @param privileges
	 * @param resourceVisibleToAll
	 * @param type
	 * @param deliveryStart
	 * @param deliveryEnd
	 * @return
	 * @throws IllegalArgumentException - when {@code privileges} collection is either null or empty, 
	 * or when {@code type} is null
	 */
	public static CredentialUserAccessSpecification of(Collection<UserGroupPrivilege> privileges,
			boolean resourceVisibleToAll, CredentialType type, Date deliveryStart,
			Date deliveryEnd) {
		if (privileges == null || privileges.isEmpty() || type == null) {
			throw new IllegalArgumentException();
		}
		return new CredentialUserAccessSpecification(privileges, resourceVisibleToAll, type, deliveryStart, deliveryEnd);
	}
	
	@Override
	public <T> T accept(UserAccessSpecificationVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public CredentialType getType() {
		return type;
	}

	public Date getDeliveryStart() {
		return deliveryStart;
	}

	public Date getDeliveryEnd() {
		return deliveryEnd;
	}
	
}
