package org.prosolo.web.util;
 

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.services.activityWall.impl.data.SocialActivityType;

/**
 * Class ResourceBundleUtil handles retrieving values from UIResources.properties file.
 * Fixed bundle reference makes it easier using an always-used resource bundle.
 * This class also create a FacesMessage, return it, or add it to a desired UIComponent.
 * 
 * @author Qussay Najjar
 * @version 2.0 2011/08/29
 * @link http://qussay.me/2011/08/29/java-localization-with-propertyresourcebundle
 */
public class ResourceBundleUtil {
	
	private static Logger logger = Logger.getLogger(ResourceBundleUtil.class);

    private static Map<String, ResourceBundle> bundles = null;

    private ResourceBundleUtil() {
    	bundles = new HashMap<String, ResourceBundle>();
    }

    /**
     * Returns a string value from the <code>UIResources.properties</code> bundle associated with the given key,
     * localized, and formatted with the given parameters, if any.
     * @param key The string key in the loaded resource bundle.
     * @param locale Defines the localization file.
     * @param params Optional parameters to format the string using <code>MessageFormat</code>.
     * @return The string value from the resource bundle associated with the given key, formatted
     * with the given parameters, if any.
     * @throws KeyNotFoundInBundleException 
     * @see ResourceBundleUtil#getString(String, ResourceBundle)
     */
	public static String getString(String path, String key, Locale locale, Object... params) throws KeyNotFoundInBundleException {
		if (bundles == null) {
			bundles = new HashMap<String, ResourceBundle>();
		}
		
		ResourceBundle bundle = null;
		if (bundles.containsKey(path)) {
			bundle = bundles.get(path);
		} else {
			bundle = ResourceBundle.getBundle(path, locale);
			bundles.put(path, bundle);
		}
		String value = getString(key, bundle, params);
		
		if (value != null) {
			return value;
		} else {
			throw new KeyNotFoundInBundleException("Key '"+key+"' is not found in the bundle '"+path+"'");
		}
	}
	
	public static String getMessage(String key, Object... params) throws KeyNotFoundInBundleException {
		return getMessage(key, new Locale("en", "US"), params);
	}

	public static String getMessage(String key, Locale locale, Object... params) throws KeyNotFoundInBundleException {
		String bcName = Settings.getInstance().config.init.bcName;
		return getString("org.prosolo.web."+bcName+"_messages", key, locale, params);
	}

    /**
     * Retrieve a defined string from the given bundle file,
     * according to the passed key.
     *
     * @param key The string key in the loaded resource bundle.
     * @param bundle The <code>ResourceBundle</code> file whom data is needed.
     * @param params Optional parameters to format the message using <code>MessageFormat</code>.
     * @return The string value from the given bundle file matching the given key.
     */
    public static String getString(String key, ResourceBundle bundle, Object... params) {
    	if (bundle.containsKey(key)) {
    		return MessageFormat.format(bundle.getString(key), params);
    	} else {
    		return null;
    	}
    }
    
    /**
     * Retrieve the set <code>Locale</code> in the <code>UIViewRoot</code> of the current <code>FacesContext</code>.
     *
     * @param context The current <code>FacesContext</code> object to reach the JSF pages <code>UIViewRoot</code>.
     * @return Current set <code>Locale</code> in <code>UIViewRoot</code>.
     */
    public static Locale getLocale(FacesContext context) {
        Locale locale = null;
        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot != null)
            locale = viewRoot.getLocale();
        if (locale == null)
            locale = Locale.getDefault();
        return locale;
    }
    
    public static String getResourceType(String resourceName, Locale locale) {
		try {
			return ResourceBundleUtil.getMessage( 
				"resource.type." + resourceName,
				locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		return "";
    }
    
    /**
     * if proxy class remove _$ ... appended to class name
     */
    private static String getClassName(Class<?> clazz) {
    	String name = clazz.getSimpleName();
    	if(name.indexOf("_") != -1) {
    		name = name.substring(0, name.indexOf("_"));
    	}
    	return name;
	}

	public static String getActionName(String action, Locale locale) {
		try {
			return ResourceBundleUtil.getMessage( 
					"activitywall.actionname." + action, 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		return "";
	}
    
    public static String getRelationBetweenResources(Locale locale, SocialActivityType type,
    		ResourceType objectType, ResourceType targetType) {
		String relationToTarget = "";
		try {
			relationToTarget = ResourceBundleUtil.getMessage( 
					"activitywall.relationToTarget."+type.name()+"."+
						objectType.name()+"."+
						targetType.name(), 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		return relationToTarget;
	}

	public static String getLabel(String resource, Locale locale) {
		try {
			return ResourceBundleUtil.getMessage(
					"label." + resource, locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		return "";
	}

	public static String getLabel(String resource) {
    	return getLabel(resource, new Locale("en", "US"));
	}
}