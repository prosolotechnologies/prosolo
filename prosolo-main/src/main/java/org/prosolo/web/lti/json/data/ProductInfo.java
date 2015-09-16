package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;

public class ProductInfo {
	
	@SerializedName("product_name")
	private Description productName;
	@SerializedName("product_version")
	private String productVersion;
	private Description description;
	@SerializedName("product_family")
	private ProductFamily productFamily;
	
	public Description getProductName() {
		return productName;
	}
	public void setProductName(Description productName) {
		this.productName = productName;
	}
	public String getProductVersion() {
		return productVersion;
	}
	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}
	public Description getDescription() {
		return description;
	}
	public void setDescription(Description description) {
		this.description = description;
	}
	public ProductFamily getProductFamily() {
		return productFamily;
	}
	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}
	
}
