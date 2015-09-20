package org.prosolo.web.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.hibernate.Hibernate;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.reminders.EventReminder;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.RequestReminder;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.springframework.stereotype.Component;

@ManagedBean(name = "resourceUtilBean")
@Component("resourceUtilBean")
//@Scope("session")
public class ResourceUtilBean {
	
//	public static String getClassName(Node resource) {
//		if (resource != null) {
//			if (resource instanceof HibernateProxy) {
//				Class classWithoutInitializingProxy = HibernateProxyHelper.getClassWithoutInitializingProxy(resource);
//				return classWithoutInitializingProxy.getSimpleName();
//			}
//			return resource.getClass().getSimpleName();
//		}
//		return "";
//	}

	public static String findReminderResourceType(Reminder reminder) {
		 if(reminder instanceof RequestReminder){
			 return findResourceType(((RequestReminder) reminder).getRequestResource());
		 } else if(reminder instanceof EventReminder){
			 return findResourceType(reminder);
		 }else{
			 return findResourceType(reminder.getResource());
		 }
	}
	
	public static String findResourceType(BaseEntity resource) {
		String key = "node_" + Hibernate.getClass(resource).getSimpleName();
		Locale locale = null;
		if(FacesContext.getCurrentInstance()!=null){
			locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		}else{
			locale=new Locale("en");
		}
		String value = "";
		try {
			value = ResourceBundleUtil.getMessage(key, locale);
		} catch (KeyNotFoundInBundleException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static String convertToIdArrayString(Collection<? extends Node> resources) {
		if (resources != null && !resources.isEmpty()) {
			return Arrays.toString(convertToIdArray(resources));
		}
		return null;
	}
	
	public static long[] convertToIdArray(Collection<? extends Node> resources) {
		if (resources != null && !resources.isEmpty()) {
			long[] ids = new long[resources.size()];
			
			int index = 0;
			for (Node node : resources) {
				ids[index++] = node.getId();
			}
			
			return ids;
		}
		return null;
	}
	public static long getCurrentTimestamp(){
		return System.currentTimeMillis();
	}
}
