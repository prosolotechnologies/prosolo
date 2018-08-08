package org.prosolo.services.nodes.factory;

import org.junit.Test;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialCategory;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.nodes.data.credential.CategorizedCredentialsData;
import org.prosolo.services.nodes.data.credential.TargetCredentialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author stefanvuckovic
 * @date 2018-04-18
 * @since 1.2.0
 */
/*
test methods name convention: [UnitOfWork_StateUnderTest_ExpectedBehavior]

 */
public class CredentialDataFactoryTest {

    @Test
    public void groupCredentialsByCategory_NullList_shouldReturnNull() {
        CredentialDataFactory credentialDataFactory = new CredentialDataFactory();
        List<CategorizedCredentialsData> result = credentialDataFactory.groupCredentialsByCategory(null);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void groupCredentialsByCategory_EmptyList_shouldReturnEmptyList() {
        CredentialDataFactory credentialDataFactory = new CredentialDataFactory();
        List<CategorizedCredentialsData> result = credentialDataFactory.groupCredentialsByCategory(new ArrayList<>());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectNumberOfCategories() {
        CredentialDataFactory credentialDataFactory = new CredentialDataFactory();

        TargetCredentialData tc1 = getTargetCredentialTestData(1, null);
        List<TargetCredentialData> oneCredentialListWithNullCategory = Arrays.asList(tc1);
        List<CategorizedCredentialsData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.size(), is(1));

        List<TargetCredentialData> inputTestData = getTargetCredentialsTestData();
        List<CategorizedCredentialsData> result2 = credentialDataFactory.groupCredentialsByCategory(inputTestData);
        assertThat(result2.size(), is(3));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectNumberOfCredentialsPerCategory() {
        CredentialDataFactory credentialDataFactory = new CredentialDataFactory();

        TargetCredentialData tc1 = getTargetCredentialTestData(1, null);
        List<TargetCredentialData> oneCredentialListWithNullCategory = Arrays.asList(tc1);
        List<CategorizedCredentialsData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.get(0).getCredentials().size(), is(1));

        TargetCredentialData tc2 = getTargetCredentialTestData(1, null);
        TargetCredentialData tc3 = getTargetCredentialTestData(2, null);
        List<TargetCredentialData> oneCategoryWithTwoCredentialsList = Arrays.asList(tc2, tc3);
        List<CategorizedCredentialsData> result2 = credentialDataFactory.groupCredentialsByCategory(oneCategoryWithTwoCredentialsList);
        assertThat(result2.get(0).getCredentials().size(), is(2));

        List<TargetCredentialData> inputTestData = getTargetCredentialsTestData();
        List<CategorizedCredentialsData> result3 = credentialDataFactory.groupCredentialsByCategory(inputTestData);
        assertThat(result3.get(0).getCredentials().size(), is(2));
        assertThat(result3.get(1).getCredentials().size(), is(1));
        assertThat(result3.get(2).getCredentials().size(), is(2));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectCategoriesInCorrectOrder() {
        CredentialDataFactory credentialDataFactory = new CredentialDataFactory();

        TargetCredentialData tc1 = getTargetCredentialTestData(1, null);
        List<TargetCredentialData> oneCredentialListWithNullCategory = Arrays.asList(tc1);
        List<CategorizedCredentialsData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.get(0).getCategory(), is(nullValue()));

        CredentialCategory cc1 = new CredentialCategory();
        cc1.setId(1);
        cc1.setTitle("Category1");
        TargetCredentialData tc2 = getTargetCredentialTestData(1, cc1);
        List<TargetCredentialData> oneCredentialListWithNotNullCategory = Arrays.asList(tc2);
        List<CategorizedCredentialsData> result2 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNotNullCategory);
        assertThat(result2.get(0).getCategory().getId(), is(1L));

        List<TargetCredentialData> inputTestData = getTargetCredentialsTestData();
        List<CategorizedCredentialsData> result3 = credentialDataFactory.groupCredentialsByCategory(inputTestData);
        assertThat(result3.get(0).getCategory(), is(nullValue()));
        assertThat(result3.get(1).getCategory().getId(), is(1L));
        assertThat(result3.get(2).getCategory().getId(), is(2L));
    }

    @Test
    public void groupCredentialsByCategory_arbitraryList_shouldReturnCorrectCredentialsOrderInEachCategory() {
        CredentialDataFactory credentialDataFactory = new CredentialDataFactory();

        TargetCredentialData tc1 = getTargetCredentialTestData(1, null);
        List<TargetCredentialData> oneCredentialListWithNullCategory = Arrays.asList(tc1);
        List<CategorizedCredentialsData> result1 = credentialDataFactory.groupCredentialsByCategory(oneCredentialListWithNullCategory);
        assertThat(result1.get(0).getCredentials().get(0).getCredentialId(), is(1L));

        List<TargetCredentialData> inputTestData = getTargetCredentialsTestData();
        List<CategorizedCredentialsData> result2 = credentialDataFactory.groupCredentialsByCategory(inputTestData);

        List<TargetCredentialData> firstCategoryCredentials = result2.get(0).getCredentials();
        assertThat(firstCategoryCredentials.get(0).getCredentialId(), is(1L));
        assertThat(firstCategoryCredentials.get(1).getCredentialId(), is(2L));

        List<TargetCredentialData> secondCategoryCredentials = result2.get(1).getCredentials();
        assertThat(secondCategoryCredentials.get(0).getCredentialId(), is(3L));

        List<TargetCredentialData> thirdCategoryCredentials = result2.get(2).getCredentials();
        assertThat(thirdCategoryCredentials.get(0).getCredentialId(), is(4L));
        assertThat(thirdCategoryCredentials.get(1).getCredentialId(), is(5L));
    }

    private List<TargetCredentialData> getTargetCredentialsTestData() {
        CredentialCategory cc1 = new CredentialCategory();
        cc1.setId(1);
        cc1.setTitle("Category1");
        CredentialCategory cc2 = new CredentialCategory();
        cc2.setId(2);
        cc2.setTitle("Category2");

        TargetCredentialData tcd1 = getTargetCredentialTestData(1, null);
        TargetCredentialData tcd2 = getTargetCredentialTestData(2, null);
        TargetCredentialData tcd3 = getTargetCredentialTestData(3, cc1);
        TargetCredentialData tcd4 = getTargetCredentialTestData(4, cc2);
        TargetCredentialData tcd5 = getTargetCredentialTestData(5, cc2);

        return Arrays.asList(tcd1, tcd2, tcd3, tcd4, tcd5);
    }

    private TargetCredentialData getTargetCredentialTestData(long id, CredentialCategory credentialCategory) {
        Credential1 c1 = new Credential1();
        c1.setId(id);
        c1.setTitle("Credential" + id);
        TargetCredential1 tc1 = new TargetCredential1();
        tc1.setCredential(c1);
        return new TargetCredentialData(tc1, credentialCategory, 0);
    }
}