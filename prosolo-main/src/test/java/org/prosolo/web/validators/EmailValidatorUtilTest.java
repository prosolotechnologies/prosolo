package org.prosolo.web.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Nikola Milikic
 * @date 2019-07-26
 * @since 1.3.2
 */
public class EmailValidatorUtilTest {

    private static EmailValidatorUtil validator;

    @Test
    public void testValidEmail() throws Exception {
        isValidEmail( "emmanuel@hibernate.org" );
        isValidEmail( "emma+nuel@hibernate.org" );
        isValidEmail( "emma=nuel@hibernate.org" );
        isValidEmail( "emmanuel@[123.12.2.11]" );
        isValidEmail( "*@example.net" );
        isValidEmail( "fred&barny@example.com" );
        isValidEmail( "---@example.com" );
        isValidEmail( "foo-bar@example.net" );
        isValidEmail( "prettyandsimple@example.com" );
        isValidEmail( "very.common@example.com" );
        isValidEmail( "disposable.style.email.with+symbol@example.com" );
        isValidEmail( "other.email-with-dash@example.com" );
        isValidEmail( "x@example.com" );
        isValidEmail( "\"much.more unusual\"@example.com" );
        isValidEmail( "example-indeed@strange-example.com" );
        isValidEmail( "#!$%&'*+-/=?^_`{}|~@example.org" );
        isValidEmail( "example@s.solutions" );
        isValidEmail( "nothing@xn--fken-gra.no" );
    }

    @Test
    public void testInValidEmail() throws Exception {
        isInvalidEmail( "emmanuel.hibernate.org" );
        isInvalidEmail( "emma nuel@hibernate.org" );
        isInvalidEmail( "emma(nuel@hibernate.org" );
        isInvalidEmail( "emmanuel@" );
        isInvalidEmail( "emma\nnuel@hibernate.org" );
        isInvalidEmail( "emma@nuel@hibernate.org" );
        isInvalidEmail( "emma@nuel@.hibernate.org" );
        isInvalidEmail( "Just a string" );
        isInvalidEmail( "string" );
        isInvalidEmail( "me@" );
        isInvalidEmail( "@example.com" );
        isInvalidEmail( "me.@example.com" );
        isInvalidEmail( ".me@example.com" );
        isInvalidEmail( "me@example..com" );
        isInvalidEmail( "me\\@example.com" );
        isInvalidEmail( "Abc.example.com" ); // (no @ character)
        isInvalidEmail( "A@b@c@example.com" ); // (only one @ is allowed outside quotation marks)
        isInvalidEmail( "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com" ); // (none of the special characters in this local-part are allowed outside quotation marks)
        isInvalidEmail( "just\"not\"right@example.com" ); // (quoted strings must be dot separated or the only element making up the local-part)
        isInvalidEmail( "this is\"not\\allowed@example.com" ); // (spaces, quotes, and backslashes may only exist when within quoted strings and preceded by a backslash)
        isInvalidEmail( "john..doe@example.com" ); // (double dot before @) with caveat: Gmail lets this through, Email address#Local-part the dots altogether
        isInvalidEmail( "john.doe@example..com" );
    }

    private void isValidEmail(CharSequence email, String message) {
        assertTrue(String.format(message, email), validator.isEmailValid(email.toString()));
    }

    private void isValidEmail(CharSequence email) {
        isValidEmail( email, "Expected %1$s to be a valid email." );
    }

    private void isInvalidEmail(CharSequence email, String message) {
        assertFalse(String.format(message, email), validator.isEmailValid(email.toString()));
    }

    private void isInvalidEmail(CharSequence email) {
        isInvalidEmail( email, "Expected %1$s to be an invalid email." );
    }

}