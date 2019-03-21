package org.prosolo.services.nodes.data.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationCategoryData implements Serializable {

    private List<CredentialCategoryData> credentialCategories = new ArrayList<>();
    private List<CredentialCategoryData> credentialCategoriesForDeletion = new ArrayList<>();

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
