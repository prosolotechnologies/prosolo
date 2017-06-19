package org.prosolo.services.nodes.data.resourceAccess;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

import java.util.Collection;
import java.util.Date;

public class CompetenceUserAccessSpecification extends UserAccessSpecification {

	private final boolean resourcePublished;
	private final Date datePublished;
	private final LearningResourceType resourceType;
	
	private CompetenceUserAccessSpecification(Collection<UserGroupPrivilege> privileges, boolean resourceVisibleToAll,
											  boolean resourcePublished, Date datePublished,
			LearningResourceType resourceType) {
		super(privileges, resourceVisibleToAll);
		this.resourcePublished = resourcePublished;
		this.datePublished = datePublished;
		this.resourceType = resourceType;
	}
	
	/**
	 * Returns {@link CompetenceUserAccessSpecification} object based on provided data.
	 * 
	 * 
	 * @param privileges
	 * @param resourceVisibleToAll
	 * @param resourcePublished
	 * @param datePublished
	 * @param resourceType
	 * @return
	 * @throws IllegalArgumentException - when {@code privileges} collection is either null or empty, 
	 * or when {@code resourceType} is null
	 */
	public static CompetenceUserAccessSpecification of(Collection<UserGroupPrivilege> privileges, 
			boolean resourceVisibleToAll, boolean resourcePublished, Date datePublished,
			LearningResourceType resourceType) {
		if (privileges == null || privileges.isEmpty() || resourceType == null) {
			throw new IllegalArgumentException();
		}
		return new CompetenceUserAccessSpecification(
				privileges, resourceVisibleToAll, resourcePublished, datePublished, resourceType);
	}
	
	@Override
	public <T> T accept(UserAccessSpecificationVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public boolean isResourcePublished() {
		return resourcePublished;
	}

	public LearningResourceType getResourceType() {
		return resourceType;
	}

	public Date getDatePublished() {
		return datePublished;
	}
	
}
