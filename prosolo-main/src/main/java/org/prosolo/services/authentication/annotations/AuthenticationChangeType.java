package org.prosolo.services.authentication.annotations;

/**
 * Logical type of authentication change that can occur. Types defined are:
 * {@link #AUTHENTICATION_REFRESH} - authentication is changed but logically it only means authentication
 * for the same user is refreshed to reflect change in user principal data or granted authority list etc.;
 * {@link #USER_AUTHENTICATION_CHANGE} - authentication is changed which means user reauthenticated with the
 * same or different account but it is explicit reauthentication which has stronger meaning than authentication refresh.
 * Also, this authentication change type implies that user session is not ended logically (which does not have anything to do
 * with HttpSession object life, but only logical session end) but only authentication is changed. This can, for example, cover
 * the case when admin logs in as another user which means authentication is changed but session of admin user is not ended.
 * {@link #USER_SESSION_END} - this type means that authentication change means that user session is ended (logically) which
 * does not mean HttpSession is invalidated, HttpSession could still live but logically user session ends here.
 *
 * @author stefanvuckovic
 * @date 2018-11-06
 * @since 1.2.0
 */
public enum AuthenticationChangeType {

    AUTHENTICATION_REFRESH(1),
    USER_AUTHENTICATION_CHANGE(2),
    USER_SESSION_END(3);

    private int value;

    AuthenticationChangeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
