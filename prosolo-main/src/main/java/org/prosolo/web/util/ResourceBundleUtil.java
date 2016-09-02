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
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.services.activityWall.impl.data.SocialActivityType;

/**
 * class ResourceBundleUtil handles retrieving values from UIResources.properties file.
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

   // private static final String UI_BUNDLE = "tdb.view.bundles.ui.UIResources";

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
     * Add a <code>FacesMessage</code> to the given <code>UIComponent</code>.
     * The <code>Severity</code> status will be passed internally as <code>FacesMessage.SEVERITY_INFO</code>.
     *
     * @param component The <code>UIComoponent</code> required to add a <code>FacesMessage</code> to.
     * @param key The string key in the loaded resource bundle.
     * @throws KeyNotFoundInBundleException 
     * @see ResourceBundleUtil#addFacesMessage(UIComponent, String, Severity)
     */
    public static void addSucessFacesMessage(String UI_BUNDLE, UIComponent component, String key) throws KeyNotFoundInBundleException {
        addFacesMessage(UI_BUNDLE,component, key, FacesMessage.SEVERITY_INFO);
    }
 
    /**
     * Add a <code>FacesMessage</code> to the given <code>UIComponent</code>.
     * The <code>Severity</code> status will be passed internally as <code>FacesMessage.SEVERITY_ERROR</code>.
     *
     * @param component The <code>UIComoponent</code> required to add a <code>FacesMessage</code> to.
     * @param key The summary key of the <code>FacesMessage</code>.
     * @throws KeyNotFoundInBundleException 
     */
    public static void addErrorFacesMessage(String UI_BUNDLE,UIComponent component, String key) throws KeyNotFoundInBundleException {
        addFacesMessage(UI_BUNDLE,component, key, FacesMessage.SEVERITY_ERROR);
    }
    
    /**
     * Add a <code>FacesMessage</code> to the given <code>UIComponent</code>, with a defined severity.
     *
     * @param component The <code>UIComoponent</code> required to add a <code>FacesMessage</code> to.
     * @param key The string key in the loaded resource bundle.
     * @param severity <code>Severity</code> defines the <code>FacesMessage</code> status of four options,
     * <code>FacesMessage.SEVERITY_INFO</code>, <code>FacesMessage.SEVERITY_FATAL</code>, 
     * <code>FacesMessage.SEVERITY_WARN</code>, <code>FacesMessage.SEVERITY_ERROR</code>
     * @throws KeyNotFoundInBundleException 
     * @see ResourceBundleUtil#getFacesMessage(String, Severity)
     */
    public static void addFacesMessage(String UI_BUNDLE, UIComponent component, String key, Severity severity) throws KeyNotFoundInBundleException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(component.getClientId(facesContext), getFacesMessage(UI_BUNDLE,key, severity));
    }
    
    /**
     * Get a <code>FacesMessage</code> object with the given summary.
     * The <code>Severity</code> status will be passed internally as <code>FacesMessage.SEVERITY_INFO</code>.
     *
     * @param key The string key in the loaded resource bundle.
     * @return <code>FacesMessage</code> object.
     * @throws KeyNotFoundInBundleException 
     */
    public static FacesMessage getSuccessFacesMessage(String UI_BUNDLE, String key) throws KeyNotFoundInBundleException {
        return getFacesMessage(UI_BUNDLE,key, FacesMessage.SEVERITY_INFO);
    }
    
    /**
     * Get a <code>FacesMessage</code> object with the given summary.
     * The Severity status will be passed internally as <code>FacesMessage.SEVERITY_ERROR</code>.
     *
     * @param key The string key in the loaded resource bundle.
     * @return <code>FacesMessage</code> object.
     * @throws KeyNotFoundInBundleException 
     */
    public static FacesMessage getErrorFacesMessage(String UI_BUNDLE, String key) throws KeyNotFoundInBundleException {
        return getFacesMessage(UI_BUNDLE,key, FacesMessage.SEVERITY_ERROR);
    }
    
    /**
     * Get a <code>FacesMessage</code> object with the given summary and passed severity status.
     *
     * @param key The string key in the loaded resource bundle.
     * @param severity <code>Severity</code> defines the FacesMessage status of four options,
     * <code>FacesMessage.SEVERITY_INFO</code>, <code>FacesMessage.SEVERITY_FATAL</code>, 
     * <code>FacesMessage.SEVERITY_WARN</code>, <code>FacesMessage.SEVERITY_ERROR</code>
     * @return <code>FacesMessage</code> object.
     * @throws KeyNotFoundInBundleException 
     */
    public static FacesMessage getFacesMessage(String UI_BUNDLE, String key, Severity severity) throws KeyNotFoundInBundleException {
        return new FacesMessage(severity, getString(UI_BUNDLE,key, getLocale(FacesContext.getCurrentInstance())), null);
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
    
    /**
     * Set the Locale in the <code>UIViewRoot</code> of the current <code>FacesContext</code>.
     *
     * @param context The current <code>FacesContext</code> object to reach the JSF pages <code>UIViewRoot</code>.
     * @param locale the required <code>Locale</code> value to set in the <code>UIViewRoot</code>.
     */
    public static void setLocale(FacesContext context, Locale locale) {
        context.getViewRoot().setLocale(locale);
    }
    
    public static String getResourceType(Class<?> clazz, Locale locale) {
		return getResourceType(getClassName(clazz), locale);
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

	public static String getActionName(EventType action, Locale locale) {
		return getActionName(action.toString(), locale);
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
    
    public static String getRelationBetweenResources(Locale locale, EventType action, 
    		Class<? extends BaseEntity> objectClass, Class<? extends BaseEntity> targetClass) {
		String relationToTarget = "";
		try {
			relationToTarget = ResourceBundleUtil.getMessage( 
					"activitywall.relationToTarget."+action.toString()+"."+
						objectClass.getSimpleName()+"."+
						targetClass.getSimpleName(), 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		return relationToTarget;
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
}