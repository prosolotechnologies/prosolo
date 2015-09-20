/**
 * 
 */
package org.prosolo.web.general;

import java.io.Serializable;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.web.goals.cache.ActionDisabledReason;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name="tooltips")
@Component("tooltips")
@Scope("request")
public class TooltipsBean implements Serializable {
	
	private static final long serialVersionUID = 3664841651715385783L;

	private static Logger logger = Logger.getLogger(TooltipsBean.class);
	
	
	private Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
	 
	public String getTooltip(String position) {
		try {
			return ResourceBundleUtil.getMessage(
					"tooltip.info."+position, 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e.getMessage());
		}
		return "";
	}

	 
	public String getActionDisabledText(ActionDisabledReason actionDisabledReason) {
		try {
			return ResourceBundleUtil.getMessage(
					"tooltip.actionDisabled."+actionDisabledReason, 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e.getMessage());
		}
		return "";
	}
		
	public String getTutorialStepText(String step, String page) {
		try {
			return ResourceBundleUtil.getMessage(
					"tooltip.tutorial." + page.toLowerCase() + "." + step, 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e.getMessage());
		}
		return "";
	}
	
	public String getAllTutorialStepTexts(String page, int steps) {
		StringBuffer buffer = new StringBuffer();
		try {
			for (int i = 1; i <= steps; i++) {
				String stepText = ResourceBundleUtil.getMessage(
						"tooltip.tutorial." + page.toLowerCase() + ".step" + i, 
						locale);
				
				buffer.append(stepText + (i < steps ? "|" : ""));
			}
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e.getMessage());
		}
		return buffer.toString();
	}
}
