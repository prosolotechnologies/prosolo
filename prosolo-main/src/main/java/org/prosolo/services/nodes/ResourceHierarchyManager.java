package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.services.general.AbstractManager;

public interface ResourceHierarchyManager extends AbstractManager {

	Node getTopLevelResource(Node resource);
	
//	Node getTopLevelResource(String resourceUri) throws ResourceCouldNotBeLoadedException;
	
	List<Node> getResourceHierarchyTree(Node resource, boolean includingResource);
}
