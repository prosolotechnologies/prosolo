package org.prosolo.services.nodes.data.credential;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author stefanvuckovic
 * @date 2018-08-16
 * @since 1.2.0
 */
public class CredentialIdDataTest {

    @Test
    public void getFormattedOrder_oneDigitNumber_shouldAddOneLeadingZero() {
        CredentialIdData data = new CredentialIdData(false);
        data.setOrder(1);
        assertThat(data.getFormattedOrder(), is("01"));
        data.setOrder(9);
        assertThat(data.getFormattedOrder(), is("09"));
    }

    @Test
    public void getFormattedOrder_twoDigitNumber_shouldReturnUnchangedNumber() {
        CredentialIdData data = new CredentialIdData(false);
        data.setOrder(10);
        assertThat(data.getFormattedOrder(), is("10"));
        data.setOrder(55);
        assertThat(data.getFormattedOrder(), is("55"));
    }

    @Test
    public void getFormattedOrder_moreThanTwoDigitNumber_shouldReturnUnchangedNumber() {
        CredentialIdData data = new CredentialIdData(false);
        data.setOrder(100);
        assertThat(data.getFormattedOrder(), is("100"));
        data.setOrder(987496);
        assertThat(data.getFormattedOrder(), is("987496"));
    }
}