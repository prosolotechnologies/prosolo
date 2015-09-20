package org.prosolo.web.home.util;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.hibernate.Hibernate;
import org.prosolo.common.domainmodel.featuredNews.FeaturedNews;
import org.prosolo.common.domainmodel.featuredNews.LearningGoalFeaturedNews;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.home.data.FeaturedNewsData;
import org.prosolo.web.util.ResourceBundleUtil;

/*
 * @author Zoran Jeremic 2013-05-23
 */
public class FeaturedNewsConverter {
 
	public static FeaturedNewsData convertFeaturedNewsToFeaturedNewsData(LearningGoalFeaturedNews featuredNews){
		FeaturedNewsData fNewsData=null;
		
		if (featuredNews != null) {
			fNewsData = new FeaturedNewsData();
			fNewsData.setFeaturedNews(featuredNews);
			fNewsData.setActionLabel(createActionLabel(featuredNews));
			fNewsData.setCreatorAvatar(createCreatorsAvatar(featuredNews));
			fNewsData.setPersonName(createPersonName(featuredNews));
			fNewsData.setPrettyDateCreated(createPrettyDateCreated(featuredNews));
			fNewsData.setResourceName(createResourceName(featuredNews));
			fNewsData.setActor(featuredNews.getActor());
			fNewsData.setResource(featuredNews.getResource());
		}
		return fNewsData;
	}

	public static LinkedList<FeaturedNewsData> convertFeaturedNewsToFeaturedNewsData(Collection<LearningGoalFeaturedNews> featuredNews) {
		LinkedList<FeaturedNewsData> fNewsDataList = new LinkedList<FeaturedNewsData>();
		
		for (LearningGoalFeaturedNews fNews : featuredNews) {
			FeaturedNewsData fNewsData = convertFeaturedNewsToFeaturedNewsData(fNews);
			if (fNewsData != null) {
				fNewsDataList.add(fNewsData);
			}
		}
		return fNewsDataList;
	}
	
	private static String createActionLabel(LearningGoalFeaturedNews featuredNews) {
		String key = "featurednews.activity_" + featuredNews.getAction().toString()
				+ "_" + Hibernate.getClass(featuredNews.getResource()).getSimpleName();

		Locale locale = null;
		if (FacesContext.getCurrentInstance() != null) {
			locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		} else {
			locale = new Locale("en");
		}
		
		String value = "";
		try {
			value = ResourceBundleUtil.getMessage(key, locale);
		} catch (KeyNotFoundInBundleException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	private static String createPrettyDateCreated(FeaturedNews featuredNews) {
		Date date = featuredNews.getDate();
		if (date != null) {
			return DateUtil.getPrettyDate(date);
		}
		return "";
	}
	
	private static String createPersonName(LearningGoalFeaturedNews featuredNews) {
		User person = featuredNews.getActor();
		return createPersonName(person);
	}
	private static String createPersonName(User person){
		return person.getName() + " " + person.getLastname();
	}

	private static String createResourceName(LearningGoalFeaturedNews featuredNews) {
		return featuredNews.getResource().getTitle();
	}

	private static String createCreatorsAvatar(LearningGoalFeaturedNews featuredNews) {
		return createAvatarUrl(featuredNews.getActor(), "30");
	}
	
	private static String createAvatarUrl(User user, String size) {
		for (ImageFormat imgFormat : ImageFormat.values()) {
			if (imgFormat.toString().contains(size)) {
				return AvatarUtils.getAvatarUrlInFormat(user, imgFormat);
			}
		}
		return null;
	}
}

