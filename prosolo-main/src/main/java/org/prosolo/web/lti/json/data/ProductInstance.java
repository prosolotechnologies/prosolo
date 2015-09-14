package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;

public class ProductInstance {

	//Globally unique, UUID
	private String guid;
	@SerializedName("product_info")
	private ProductInfo productInfo;
	private Contact support;
	@SerializedName("service_provider")
	private ServiceProvider serviceProvider;
	@SerializedName("service_owner")
	private ServiceOwner serviceOwner;
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public ProductInfo getProductInfo() {
		return productInfo;
	}
	public void setProductInfo(ProductInfo productInfo) {
		this.productInfo = productInfo;
	}
	public Contact getSupport() {
		return support;
	}
	public void setSupport(Contact support) {
		this.support = support;
	}
	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}
	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
	public ServiceOwner getServiceOwner() {
		return serviceOwner;
	}
	public void setServiceOwner(ServiceOwner serviceOwner) {
		this.serviceOwner = serviceOwner;
	}
	
	
}
