package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;

public class ProductFamily {

	@SerializedName("@id")
	private String id;
	//unique within the namespace of a vendor
	private String code;
	private Vendor vendor;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Vendor getVendor() {
		return vendor;
	}
	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}
	
	
}
