package main.java.com.eweware.service.rest.session;

/**
 * @author rk@post.harvard.edu
 *         Date: 12/26/12 Time: 9:51 PM
 */
public enum SessionState {

    AUTHENTICATED, /** user has been authenticated (e.g., through username/password) **/
    ANONYMOUS;     /** user is anonymous: has not passed any sort of authentication */
}
