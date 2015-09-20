package org.prosolo.bigdata.utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
 

/**
 * @author Zoran Jeremic, Sep 19, 2015
 *
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
		return getString("lang.messages", key, locale, params);
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
}
