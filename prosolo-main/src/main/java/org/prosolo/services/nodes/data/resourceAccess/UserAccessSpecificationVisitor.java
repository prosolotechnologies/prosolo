package org.prosolo.services.nodes.data.resourceAccess;

public interface UserAccessSpecificationVisitor<T> {

	T visit(CredentialUserAccessSpecification spec);
	T visit(CompetenceUserAccessSpecification spec);
}
