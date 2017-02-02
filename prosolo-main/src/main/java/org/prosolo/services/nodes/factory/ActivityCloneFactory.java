package org.prosolo.services.nodes.factory;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.ResourceLink;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityCloneFactory {
	
	private static Logger logger = Logger.getLogger(ActivityCloneFactory.class);
	
	@Inject 
	private DefaultManager defaultManager;
	
	/**
	 * Creates a new {@link CompetenceActivity1} instance that is a duplicate of the given original. This method internally calls 
	 * the method {@link #clone(Activity1)} that clones the activity. Newly created CompetenceActivity1 is not persisted as it 
	 * does not have a reference to a competence at this point.
	 * 
	 * @param original
	 * @return newly created {@link CompetenceActivity1} instance
	 */
	@Transactional (readOnly = false)
	public CompetenceActivity1 clone(CompetenceActivity1 original) {
		try {
			Activity1 activity = clone(original.getActivity());
			
			CompetenceActivity1 competenceActivity = new CompetenceActivity1();
			competenceActivity.setActivity(activity);
			competenceActivity.setOrder(original.getOrder());
			return competenceActivity;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while cloning competence activity");
		}
	}

	/**
	 * Creates a new {@link Activity1} instance that is a clone of the given instance.
	 * 
	 * @param original the original activity to be cloned
	 * @return newly created activity
	 */
	@Transactional (readOnly = false)
	public Activity1 clone(Activity1 original) {
		try {
			original = HibernateUtil.initializeAndUnproxy(original);
			Activity1 activity = cloneActivityBasedOnType(original);
			
			if (activity == null) {
				throw new Exception("Can not clone activity of a type " + original.getClass());
			}
			
			activity.setTitle(original.getTitle());
			activity.setDescription(original.getDescription());
			activity.setDuration(original.getDuration());
			activity.setLinks(cloneLinks(original.getLinks()));
			activity.setFiles(cloneLinks(original.getFiles()));
			activity.setResultType(original.getResultType());
			activity.setType(original.getType());
			activity.setMaxPoints(original.getMaxPoints());
			activity.setStudentCanSeeOtherResponses(original.isStudentCanSeeOtherResponses());
			activity.setStudentCanEditResponse(original.isStudentCanEditResponse());
			activity.setCreatedBy(original.getCreatedBy());
			activity.setVisibleForUnenrolledStudents(original.isVisibleForUnenrolledStudents());
			return defaultManager.saveEntity(activity);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while cloning activity");
		}
	}

	private Set<ResourceLink> cloneLinks(Set<ResourceLink> links) {
		Set<ResourceLink> cloneLinks = new HashSet<ResourceLink>();
		
		for (ResourceLink link : links) {
			ResourceLink linkClone = new ResourceLink();
			linkClone.setLinkName(link.getLinkName());
			linkClone.setUrl(link.getUrl());
			linkClone = defaultManager.saveEntity(linkClone);
			
			cloneLinks.add(linkClone);
		}
		
		return cloneLinks;
	}

	private Activity1 cloneActivityBasedOnType(Activity1 original) {
		if (original instanceof TextActivity1) {
			TextActivity1 originalTa = (TextActivity1) original;
			
			TextActivity1 ta = new TextActivity1();
			ta.setText(originalTa.getText());
			return ta;
		} else if (original instanceof UrlActivity1) {
			UrlActivity1 originalUrlAct = (UrlActivity1) original;

			UrlActivity1 urlAct = new UrlActivity1();
			urlAct.setUrlType(originalUrlAct.getUrlType());
			urlAct.setUrl(originalUrlAct.getUrl());
			urlAct.setLinkName(originalUrlAct.getLinkName());
			return urlAct;
		} else if (original instanceof ExternalToolActivity1) {
			ExternalToolActivity1 originalExtAct = (ExternalToolActivity1) original;
			
			ExternalToolActivity1 extAct = new ExternalToolActivity1();
			extAct.setLaunchUrl(originalExtAct.getLaunchUrl());
			extAct.setSharedSecret(originalExtAct.getSharedSecret());
			extAct.setConsumerKey(originalExtAct.getConsumerKey());
			extAct.setAcceptGrades(originalExtAct.isAcceptGrades());
			extAct.setOpenInNewWindow(originalExtAct.isOpenInNewWindow());
			extAct.setScoreCalculation(originalExtAct.getScoreCalculation());
			return extAct;
		} else { 
			return null;
		}
	}
	
}
