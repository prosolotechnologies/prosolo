package org.prosolo.bigdata.utils;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;
import org.prosolo.common.domainmodel.interfacesettings.LocaleSettings;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;

/**
 * @author Zoran Jeremic, Sep 19, 2015
 *
 */
public class ResourceBundleUtilTest {

	@Test
	public void testGetMessage() {
		String categoryName;
		try {
			categoryName = ResourceBundleUtil.getMessage("digest.filterName.title.myfeeds", new LocaleSettings("en", "US").createLocale());
			System.out.println("CATEGORY NAME:"+categoryName);
		} catch (KeyNotFoundInBundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	 
	}

}
