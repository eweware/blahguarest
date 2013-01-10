package main.java.com.eweware.service.rest.session;

/**
 * @author rk@post.harvard.edu
 *         Date: 12/26/12 Time: 9:51 PM
 */
public enum SessionState {

    V("VALIDATED"), /** user has been validated (e.g., through username/password) **/
    A("ANONYMOUS");
    private final String name;

    SessionState(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
