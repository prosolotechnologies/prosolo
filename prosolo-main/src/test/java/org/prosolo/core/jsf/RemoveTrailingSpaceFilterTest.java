package org.prosolo.core.jsf;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author stefanvuckovic
 * @date 2019-05-30
 * @since 1.3.2
 */
public class RemoveTrailingSpaceFilterTest {

    @Test
    public void getStringWithoutTrailingWhitespaceIfExists_NoWhiteSpace_shouldReturnEmpty() {
        RemoveTrailingSpaceFilter filter = new RemoveTrailingSpaceFilter();
        String url = "http://localhost:8080/prosolo/admin/%E2%80%8Borganizations/4b7QxdXK/units/4b7QxdXK/credentials/4b7QxdXK";
        assertThat(filter.getStringWithoutTrailingWhitespaceIfExists(url).isEmpty(), is(true));
        String queryString = "test1=test1Value&test%E2%80%8B2=test2Value";
        assertThat(filter.getStringWithoutTrailingWhitespaceIfExists(queryString).isEmpty(), is(true));
    }

    @Test
    public void getStringWithoutTrailingWhitespaceIfExists_TrailingWhitespace_shouldReturnStringWithoutTrailingSpace() {
        RemoveTrailingSpaceFilter filter = new RemoveTrailingSpaceFilter();
        String url = "http://localhost:8080/prosolo/admin%E2%80%8B%E2%80%8B/organizations/4b7QxdXK/units/4b7QxdXK/credentials/4b7QxdXK%E2%80%8B";
        Optional<String> res = filter.getStringWithoutTrailingWhitespaceIfExists(url);
        assertThat(res.isPresent(), is(true));
        assertThat(res.get(), is("http://localhost:8080/prosolo/admin%E2%80%8B%E2%80%8B/organizations/4b7QxdXK/units/4b7QxdXK/credentials/4b7QxdXK"));
        String queryString = "test1=test1%E2%80%8BValue%E2%80%8B&test2=test2Value%E2%80%8B";
        Optional<String> res2 = filter.getStringWithoutTrailingWhitespaceIfExists(queryString);
        assertThat(res2.isPresent(), is(true));
        assertThat(res2.get(), is("test1=test1%E2%80%8BValue%E2%80%8B&test2=test2Value"));
    }

    @Test
    public void getStringWithoutTrailingWhitespaceIfExists_DifferentSpaces_shouldReturnStringWithoutTrailingSpace() {
        RemoveTrailingSpaceFilter filter = new RemoveTrailingSpaceFilter();
        RemoveTrailingSpaceFilter spyFilter = spy(filter);
        doReturn(new String[] {"whitespace1", "whitespace2"}).when(spyFilter).getWhitespaceCharacters();
        String url = "http://localhost:8080/prosolowhitespace1whitespace1/admin/organizations/4b7QxdXK/units/4b7QxdXK/credentials/4b7QxdXKwhitespace1";
        Optional<String> res = spyFilter.getStringWithoutTrailingWhitespaceIfExists(url);
        assertThat(res.isPresent(), is(true));
        assertThat(res.get(), is("http://localhost:8080/prosolowhitespace1whitespace1/admin/organizations/4b7QxdXK/units/4b7QxdXK/credentials/4b7QxdXK"));
        String queryString = "test1=test1Value&test2whitespace1=test2Valuwhitespace2ewhitespace2";
        Optional<String> res2 = spyFilter.getStringWithoutTrailingWhitespaceIfExists(queryString);
        assertThat(res2.isPresent(), is(true));
        assertThat(res2.get(), is("test1=test1Value&test2whitespace1=test2Valuwhitespace2e"));
    }

    @Test
    public void getStringWithoutTrailingWhitespaceIfExists_multipleTrailingSpaces_shouldReturnStringWithoutTrailingSpace() {
        RemoveTrailingSpaceFilter filter = new RemoveTrailingSpaceFilter();
        RemoveTrailingSpaceFilter spyFilter = spy(filter);
        doReturn(new String[] {"%E2%80%8B", "whitespace1", "whitespace2"}).when(spyFilter).getWhitespaceCharacters();
        String url = "http://localhost:8080/prosolo%E2%80%8Bwhitespace1/admin/organizations/4b7QxdXK/units/4b7QxdXK/credentials/4b7QxdXKwhitespace1%E2%80%8B%E2%80%8B";
        Optional<String> res = spyFilter.getStringWithoutTrailingWhitespaceIfExists(url);
        assertThat(res.isPresent(), is(true));
        assertThat(res.get(), is("http://localhost:8080/prosolo%E2%80%8Bwhitespace1/admin/organizations/4b7QxdXK/units/4b7QxdXK/credentials/4b7QxdXK"));
        String queryString = "test1=test1Value&test2=test2Valuewhitespace2whitespace1%E2%80%8Bwhitespace2";
        Optional<String> res2 = spyFilter.getStringWithoutTrailingWhitespaceIfExists(queryString);
        assertThat(res2.isPresent(), is(true));
        assertThat(res2.get(), is("test1=test1Value&test2=test2Value"));
    }
}