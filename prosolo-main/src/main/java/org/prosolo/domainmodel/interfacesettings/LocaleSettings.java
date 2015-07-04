/**
 * 
 */
package org.prosolo.domainmodel.interfacesettings;

import java.util.Locale;

import javax.persistence.Entity;

import org.prosolo.domainmodel.general.BaseEntity;

/**
 * @author nikolamilikic
 * 
 */
@Entity
public class LocaleSettings extends BaseEntity {
	
	private static final long serialVersionUID = -2649654393616404718L;
	
	private String language;
	private String region;
	
	public LocaleSettings() {}
	
	public LocaleSettings(String language, String region) {
		this.language = language;
		this.region = region;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getRegion() {
		return region;
	}
	
	public void setRegion(String region) {
		this.region = region;
	}
	
	public Locale createLocale() {
		return new Locale(language, region);
	}
	
}
