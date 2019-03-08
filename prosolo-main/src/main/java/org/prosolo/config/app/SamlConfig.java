package org.prosolo.config.app;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class SamlConfig {

	@Element(name = "prosolo-metadata-file-name")
	public String prosoloMetadataFileName;
	@ElementList(inline = true, entry="provider")
	public List<SAMLIdentityProviderInfo> samlProviders;

	public List<SAMLIdentityProviderInfo> getSamlProviders() {
		return samlProviders;
	}

	public void setSamlProviders(List<SAMLIdentityProviderInfo> samlProviders) {
		this.samlProviders = samlProviders;
	}

}
