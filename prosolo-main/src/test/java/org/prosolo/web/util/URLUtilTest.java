package org.prosolo.web.util;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author stefanvuckovic
 * @date 2018-10-26
 * @since 1.2.0
 */
public class URLUtilTest {

    private static final String FORWARD_URI_PARAM = "javax.servlet.forward.request_uri";
    private static final String FORWARD_QUERY_STRING_PARAM = "javax.servlet.forward.query_string";

    @Test
    public void getForwardedUriWithoutContextWithAttachedQueryString_ExistingQueryStringEmptyQueryStringToAdd_shouldReturnCorrectUri() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String context = "/prosolo";
        String forwardUri = "/admin/organizations/x2e3";
        String forwardQueryString = "test1=test1Value&test2=test2Value";
        String queryStringToAdd = null;
        when(requestMock.getAttribute(FORWARD_URI_PARAM)).thenReturn(context + forwardUri);
        when(requestMock.getAttribute(FORWARD_QUERY_STRING_PARAM)).thenReturn(forwardQueryString);
        when(requestMock.getContextPath()).thenReturn(context);
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd), is(forwardUri + "?" + forwardQueryString));
    }

    @Test
    public void getForwardedUriWithoutContextWithAttachedQueryString_EmptyContext_shouldReturnCorrectUri() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String context = "";
        String forwardUri = "/admin/organizations/x2e3";
        String forwardQueryString = "test1=test1Value&test2=test2Value";
        String queryStringToAdd = null;
        when(requestMock.getAttribute(FORWARD_URI_PARAM)).thenReturn(context + forwardUri);
        when(requestMock.getAttribute(FORWARD_QUERY_STRING_PARAM)).thenReturn(forwardQueryString);
        when(requestMock.getContextPath()).thenReturn(context);
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd), is(forwardUri + "?" + forwardQueryString));
    }

    @Test
    public void getForwardedUriWithoutContextWithAttachedQueryString_ExistingQueryStringToAddEmptyQueryString_shouldReturnCorrectUri() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String context = "/prosolo";
        String forwardUri = "/admin/organizations/x2e3";
        String forwardQueryString = null;
        String queryStringToAdd = "test1=test1Value&test2=test2Value";
        when(requestMock.getAttribute(FORWARD_URI_PARAM)).thenReturn(context + forwardUri);
        when(requestMock.getAttribute(FORWARD_QUERY_STRING_PARAM)).thenReturn(forwardQueryString);
        when(requestMock.getContextPath()).thenReturn(context);
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd), is(forwardUri + "?" + queryStringToAdd));
    }

    @Test
    public void getForwardedUriWithoutContextWithAttachedQueryString_EmptyQueryStringEmptyQueryStringToAdd_shouldReturnCorrectUri() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String context = "/prosolo";
        String forwardUri = "/admin/organizations/x2e3";
        String forwardQueryString = null;
        String queryStringToAdd = "";
        when(requestMock.getAttribute(FORWARD_URI_PARAM)).thenReturn(context + forwardUri);
        when(requestMock.getAttribute(FORWARD_QUERY_STRING_PARAM)).thenReturn(forwardQueryString);
        when(requestMock.getContextPath()).thenReturn(context);
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd), is(forwardUri));
    }

    @Test
    public void getForwardedUriWithoutContextWithAttachedQueryString_ExistingQueryStringExistingQueryStringToAdd_shouldReturnCorrectUri() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String context = "/prosolo";
        String forwardUri = "/admin/organizations/x2e3";
        String forwardQueryString = "test1=test1Value&test2=test2Value";
        String queryStringToAdd = "test3=test3Value&test4=test4Value";
        when(requestMock.getAttribute(FORWARD_URI_PARAM)).thenReturn(context + forwardUri);
        when(requestMock.getAttribute(FORWARD_QUERY_STRING_PARAM)).thenReturn(forwardQueryString);
        when(requestMock.getContextPath()).thenReturn(context);
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd), is(forwardUri + "?" + forwardQueryString + "&" + queryStringToAdd));
    }

    @Test
    public void getForwardedUriWithoutContextWithAttachedQueryString_SameParametersInTwoQueryStrings_shouldReturnCorrectUri() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String context = "/prosolo";
        String forwardUri = "/admin/organizations/x2e3";
        String forwardQueryString = "test1=test1Value&test2=test2Value";
        String queryStringToAdd1 = "test3=test3Value&test4=test4Value&test1=test11Value";
        String queryStringToAdd2 = "test3=test3Value&test2=test22Value";
        String queryStringToAdd3 = "test3=test3Value&test4=test4Value&test2=test22Value&test1=test11Value";

        when(requestMock.getAttribute(FORWARD_URI_PARAM)).thenReturn(context + forwardUri);
        when(requestMock.getAttribute(FORWARD_QUERY_STRING_PARAM)).thenReturn(forwardQueryString);
        when(requestMock.getContextPath()).thenReturn(context);
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd1), is(forwardUri + "?test2=test2Value&" + queryStringToAdd1));
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd2), is(forwardUri + "?test1=test1Value&" + queryStringToAdd2));
        assertThat(URLUtil.getForwardedUriWithoutContextWithAttachedQueryString(requestMock, queryStringToAdd3), is(forwardUri + "?" + queryStringToAdd3));
    }

    @Test
    public void getParamNamesFromQueryString_NullQueryString_shouldReturnNull() {
        assertThat(URLUtil.getParamNamesFromQueryString(null), is(nullValue()));
    }

    @Test
    public void getParamNamesFromQueryString_OneParamQueryString_ReturnCorrectParamName() {
        assertThat(URLUtil.getParamNamesFromQueryString("test=testValue"), is(new String[] {"test"}));
    }

    @Test
    public void getParamNamesFromQueryString_ManyParamsQueryString_ReturnsAllParamsCorrectly() {
        assertThat(URLUtil.getParamNamesFromQueryString("test1=test1Value&test2=test2Value&test3=test3Value"), arrayContainingInAnyOrder("test1", "test2", "test3"));
    }

    @Test
    public void removeParamsFromQueryStringAndReturn_NullQueryString_ReturnNull() {
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn(null,"test1=test1Value&test2=test2Value&test3=test3Value"), is(nullValue()));
    }

    @Test
    public void removeParamsFromQueryStringAndReturn_EmptyParamsToRemove_ReturnQueryStringUnchanged() {
        String qs = "test1=test1Value&test2=test2Value&test3=test3Value";
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn(qs, null), is(qs));
    }

    @Test
    public void removeParamsFromQueryStringAndReturn_ExistingQueryStringOneParameterToRemove_ReturnCorrectQueryString() {
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value", "test1"), is("test2=test2Value&test3=test3Value"));
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value", "test2", "test4"), is("test1=test1Value&test3=test3Value"));
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value", "test3", "test5"), is("test1=test1Value&test2=test2Value"));
    }

    @Test
    public void removeParamsFromQueryStringAndReturn_ExistingQueryStringSeveralParametersToRemove_ReturnCorrectQueryString() {
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value&test4=test4Value", "test1", "test2"), is("test3=test3Value&test4=test4Value"));
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value&test4=test4Value", "test2", "test4"), is("test1=test1Value&test3=test3Value"));
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value&test4=test4Value", "test1", "test3", "test4"), is("test2=test2Value"));
    }

    @Test
    public void removeParamsFromQueryStringAndReturn_ExistingQueryStringAllParametersToRemove_ReturnEmptyQueryString() {
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value&test4=test4Value", "test1", "test2", "test3", "test4"), is(""));
        assertThat(URLUtil.removeParamsFromQueryStringAndReturn("test1=test1Value&test2=test2Value&test3=test3Value&test4=test4Value", "test1", "test2", "test3", "test4", "test5", "test6"), is(""));

    }
}