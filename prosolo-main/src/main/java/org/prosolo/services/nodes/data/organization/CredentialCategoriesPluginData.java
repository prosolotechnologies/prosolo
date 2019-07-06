package org.prosolo.services.nodes.data.organization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.prosolo.common.domainmodel.organization.settings.CredentialCategoriesPlugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-03-19
 * @since 1.3
 */
@Getter
@Setter
public class CredentialCategoriesPluginData implements Serializable {

    private long pluginId;
    private boolean enabled;
    private List<CredentialCategoryData> credentialCategories;
    private List<CredentialCategoryData> credentialCategoriesForDeletion;

    public CredentialCategoriesPluginData() {
        this.credentialCategories = new ArrayList<>();
        this.credentialCategoriesForDeletion = new ArrayList<>();
    }

    public CredentialCategoriesPluginData(CredentialCategoriesPlugin credentialCategoriesPlugin) {
        this();
        this.pluginId = credentialCategoriesPlugin.getId();
        this.enabled = credentialCategoriesPlugin.isEnabled();
    }

    public void addCredentialCategory(CredentialCategoryData category) {
        credentialCategories.add(category);
    }

    public void addAllCredentialCategories(Collection<CredentialCategoryData> categories) {
        credentialCategories.addAll(categories);
    }

    public void addCredentialCategoryForDeletion(CredentialCategoryData category) {
        credentialCategoriesForDeletion.add(category);
    }

    public void resetCredentialCategories(List<CredentialCategoryData> categories) {
        credentialCategoriesForDeletion.clear();
        credentialCategories.clear();
        credentialCategories.addAll(categories);
    }

}
