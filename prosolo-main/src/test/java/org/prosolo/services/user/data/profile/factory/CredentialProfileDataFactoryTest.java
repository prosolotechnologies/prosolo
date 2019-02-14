package org.prosolo.services.user.data.profile.factory;

import org.junit.Test;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.user.data.profile.CategorizedCredentialsProfileData;
import org.prosolo.services.user.data.profile.CredentialProfileData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author stefanvuckovic
 * @date 2018-11-19
 * @since 1.2.0
 */
public class CredentialProfileDataFactoryTest {

    @Test
    public void groupCredentialsByCategory_NullList_shouldReturnNull() {
        CredentialProfileDataFactory credentialDataFactory = new CredentialProfileDataFactory();
        List<CategorizedCredentialsProfileData> result = credentialDataFactory.groupCredentialsByCategory(null);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void groupCredentialsByCategory_EmptyList_shouldReturnEmptyList() {
        CredentialProfileDataFactory credentialDataFactory = new CredentialProfileDataFactory();
        List<CategorizedCredentialsProfileData> result = credentialDataFactory.groupCredentialsByCategory(new ArrayList<>());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectNumberOfCategories() {
        CredentialProfileDataFactory credentialDataFactory = new CredentialProfileDataFactory();

        CredentialProfileData data = getCredentialProfileTestData(1, null);
        List<CredentialProfileData> oneCredentialListWithNullCategory = Arrays.asList(data);
        List<CategorizedCredentialsProfileData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.size(), is(1));

        List<CredentialProfileData> inputTestData = getCredentialProfileTestData();
        List<CategorizedCredentialsProfileData> result2 = credentialDataFactory.groupCredentialsByCategory(inputTestData);
        assertThat(result2.size(), is(3));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectNumberOfCredentialsPerCategory() {
        CredentialProfileDataFactory credentialDataFactory = new CredentialProfileDataFactory();

        CredentialProfileData cpd1 = getCredentialProfileTestData(1, null);
        List<CredentialProfileData> oneCredentialListWithNullCategory = Arrays.asList(cpd1);
        List<CategorizedCredentialsProfileData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.get(0).getCredentials().size(), is(1));

        CredentialProfileData cpd2 = getCredentialProfileTestData(1, null);
        CredentialProfileData cpd3 = getCredentialProfileTestData(2, null);
        List<CredentialProfileData> oneCategoryWithTwoCredentialsList = Arrays.asList(cpd2, cpd3);
        List<CategorizedCredentialsProfileData> result2 = credentialDataFactory.groupCredentialsByCategory(oneCategoryWithTwoCredentialsList);
        assertThat(result2.get(0).getCredentials().size(), is(2));

        List<CredentialProfileData> inputTestData = getCredentialProfileTestData();
        List<CategorizedCredentialsProfileData> result3 = credentialDataFactory.groupCredentialsByCategory(inputTestData);
        assertThat(result3.get(0).getCredentials().size(), is(2));
        assertThat(result3.get(1).getCredentials().size(), is(1));
        assertThat(result3.get(2).getCredentials().size(), is(2));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectCategoriesInCorrectOrder() {
        CredentialProfileDataFactory credentialDataFactory = new CredentialProfileDataFactory();

        CredentialProfileData cpd1 = getCredentialProfileTestData(1, null);
        List<CredentialProfileData> oneCredentialListWithNullCategory = Arrays.asList(cpd1);
        List<CategorizedCredentialsProfileData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.get(0).getCategory(), is(nullValue()));

        CredentialProfileData cpd2 = getCredentialProfileTestData(1, new CredentialCategoryData(1, "Category1", false));
        List<CredentialProfileData> oneCredentialListWithNotNullCategory = Arrays.asList(cpd2);
        List<CategorizedCredentialsProfileData> result2 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNotNullCategory);
        assertThat(result2.get(0).getCategory().getId(), is(1L));

        List<CredentialProfileData> inputTestData = getCredentialProfileTestData();
        List<CategorizedCredentialsProfileData> result3 = credentialDataFactory.groupCredentialsByCategory(inputTestData);
        assertThat(result3.get(0).getCategory(), is(nullValue()));
        assertThat(result3.get(1).getCategory().getId(), is(1L));
        assertThat(result3.get(2).getCategory().getId(), is(2L));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectCredentialsOrderInEachCategory() {
        CredentialProfileDataFactory credentialDataFactory = new CredentialProfileDataFactory();

        CredentialProfileData cpd1 = getCredentialProfileTestData(1, null);
        List<CredentialProfileData> oneCredentialListWithNullCategory = Arrays.asList(cpd1);
        List<CategorizedCredentialsProfileData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.get(0).getCredentials().get(0).getTargetCredentialId(), is(1L));

        List<CredentialProfileData> inputTestData = getCredentialProfileTestData();
        List<CategorizedCredentialsProfileData> result2 = credentialDataFactory.groupCredentialsByCategory(inputTestData);

        List<CredentialProfileData> firstCategoryCredentials = result2.get(0).getCredentials();
        assertThat(firstCategoryCredentials.get(0).getTargetCredentialId(), is(1L));
        assertThat(firstCategoryCredentials.get(1).getTargetCredentialId(), is(2L));

        List<CredentialProfileData> secondCategoryCredentials = result2.get(1).getCredentials();
        assertThat(secondCategoryCredentials.get(0).getTargetCredentialId(), is(3L));

        List<CredentialProfileData> thirdCategoryCredentials = result2.get(2).getCredentials();
        assertThat(thirdCategoryCredentials.get(0).getTargetCredentialId(), is(4L));
        assertThat(thirdCategoryCredentials.get(1).getTargetCredentialId(), is(5L));
    }

    private List<CredentialProfileData> getCredentialProfileTestData() {
        CredentialCategoryData cc1 = new CredentialCategoryData(1, "Category1", false);
        CredentialCategoryData cc2 = new CredentialCategoryData(2, "Category2", false);

        CredentialProfileData cpd1 = getCredentialProfileTestData(1, null);
        CredentialProfileData cpd2 = getCredentialProfileTestData(2, null);
        CredentialProfileData cpd3 = getCredentialProfileTestData(3, cc1);
        CredentialProfileData cpd4 = getCredentialProfileTestData(4, cc2);
        CredentialProfileData cpd5 = getCredentialProfileTestData(5, cc2);

        return Arrays.asList(cpd1, cpd2, cpd3, cpd4, cpd5);
    }

    private CredentialProfileData getCredentialProfileTestData(long id, CredentialCategoryData credentialCategory) {
        return new CredentialProfileData(0, id, 0, null, null, null, 0, null, null, credentialCategory);
    }
}