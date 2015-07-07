package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ResourceHierarchyManager;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.ResourceHierarchyManager")
public class ResourceHierarchyManagerImpl extends AbstractManagerImpl implements
		ResourceHierarchyManager {

	private static final long serialVersionUID = -8986009499787456413L;

	@Override
	// TODO to check if it works
	public Node getTopLevelResource(Node resource) {
		
		List<Node> hierarchyResources = (List<Node>) getResourceHierarchyTree(resource, true);
		
		if (hierarchyResources != null && !hierarchyResources.isEmpty()) {
			return hierarchyResources.get(hierarchyResources.size() - 1);
		}
		return null;
	}

	@Override
	// TODO to check if it works
	public List<Node> getResourceHierarchyTree(
			Node resource, boolean includingResource) {
		List<Node> hierarchyResources = new ArrayList<Node>();
		
		if (resource != null ) {
			
			if (includingResource)
				hierarchyResources.add(resource);
			
			Node currentRes = resource;

			while ((currentRes = getBasedOnResource(currentRes)) != null) {
				hierarchyResources.add(currentRes);
			}
		}

		return hierarchyResources;
	}
	
	private Node getBasedOnResource(Node currentRes) {
		if (currentRes != null) {
			if (currentRes instanceof Activity) {
				return  ((TargetActivity) currentRes).getBasedOn();
			} else if (currentRes instanceof TargetCompetence) {
				return  ((TargetCompetence) currentRes).getCompetence(); 
			}
		}
		return null;
	}

}
