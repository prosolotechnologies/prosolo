package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;

public class Vendor {
	//globally unique, at best practice internet domain of a vendor
	private String code;
	//format 2011-12-31T12:00:00
	private String timestamp;
	@SerializedName("vendor_name")
	private Description vendorName;
	private Description description;
	private String website;
	private Contact contact;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public Description getVendorName() {
		return vendorName;
	}
	public void setVendorName(Description vendorName) {
		this.vendorName = vendorName;
	}
	public Description getDescription() {
		return description;
	}
	public void setDescription(Description description) {
		this.description = description;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public Contact getContact() {
		return contact;
	}
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	
	
	
}
