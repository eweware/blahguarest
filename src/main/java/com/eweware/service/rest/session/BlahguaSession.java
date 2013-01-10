package main.java.com.eweware.service.rest.session;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 12/28/12 Time: 1:59 PM
 */
public class BlahguaSession {

    /**
     * The session state. Initially, anonymous
     */
    private SessionState state;

    /**
     * Maps an external blah id to an internal id
     */
    private Map<String, String> externalToInternalBlahIdMap = new HashMap<String, String>();

    /**
     * Maps an internal blah id to an external id
     */
    private Map<String, String> internalToExternalBlahIdMap = new HashMap<String, String>();

    /**
     * Maps an external comment id to an internal id
     */
    private Map<String, String> externalToInternalCommentIdMap = new HashMap<String, String>();

    /**
     * Maps an internal comment id to an external id
     */
    private Map<String, String> internalToExternalCommentIdMap = new HashMap<String, String>();

    public BlahguaSession(SessionState state) {
        super();
        this.state = state;
    }

    /**
     * not used
     */
    private BlahguaSession() {}

    /**
     * Convenience method.
     * @param externalBlahId    An external (client-side) blah id
     * @param httpSession   An http session object
     * @return String   The internal (server db) blah id
     * @throws SystemErrorException Thrown if the http session doesn't exist or doesn't
     * contain a blahgua session object
     * @throws InvalidRequestException Thrown if there is no internal blah id corresponding
     * to the specified external id
     */
    public static String getInternalBlahId(String externalBlahId, HttpSession httpSession) throws SystemErrorException, InvalidRequestException {
        final String internalBlahId = getBlahguaSession(httpSession).getInternalBlahId(externalBlahId);
        if (internalBlahId == null) {
            throw new InvalidRequestException("No such blahId=" + externalBlahId, ErrorCodes.INVALID_SESSION_STATE);
        }
        externalBlahId = internalBlahId;
        return externalBlahId;
    }

    public String getInternalBlahId(String externalBlahId) {
        return externalToInternalBlahIdMap.get(externalBlahId);
    }

    public String getExternalBlahId(String internalBlahId) {
        return internalToExternalBlahIdMap.get(internalBlahId);
    }

    public String getInternalCommentId(String externalCommentId) {
        return externalToInternalCommentIdMap.get(externalCommentId);
    }

    public String getExternalCommentId(String internalCommentId) {
        return internalToExternalCommentIdMap.get(internalCommentId);
    }

    // Static

    /**
     * @param session   The Tomcat session object from the context
     * @return BlahSession  Returns the corresponding Blahgua Session object for this Tomcat session or null
     * if no Blahgua session is associated with it
     * @throws SystemErrorException
     */
    public static final BlahguaSession getBlahguaSession(HttpSession session) throws SystemErrorException {
        BlahguaSession blahguaSession = null;
        if (session == null || (blahguaSession = (BlahguaSession) session.getAttribute(SessionAttributes.BLAH_SESSION_ATTRIBUTE_NAME)) == null) {
            throw new SystemErrorException("Missing session and/or blah session", ErrorCodes.INVALID_SESSION);
        }
        return blahguaSession;
    }

    /**
     * Associates a new BlahguaSession with the specified Http session. A new BlahguaSession
     * object is created. If no SessionState is provided, the default (anonymous)
     * session state will be used to initialize the BlahguaSession object.
     * @param session The Http session object
     * @param sessionState If not null, this session state will be used to initialize
     *                     the new BlahguaSession object, else the object will initialize to the default state.
     * @return BlahguaSession   The created blahgua session object
     * @throws SystemErrorException
     */
    public static final BlahguaSession setBlahguaSession(HttpSession session, SessionState sessionState) throws SystemErrorException {
        if (session == null) {
            throw new SystemErrorException("Mission session", ErrorCodes.INVALID_SESSION);
        }
        BlahguaSession blahguaSession = (BlahguaSession) session.getAttribute(SessionAttributes.BLAH_SESSION_ATTRIBUTE_NAME);
        if (blahguaSession != null) {
            throw new SystemErrorException("BlahSession already exists=" + blahguaSession, ErrorCodes.SESSION_ERROR);
        }
        blahguaSession = new BlahguaSession((sessionState == null) ? sessionState : SessionState.A);
        session.setAttribute(SessionAttributes.BLAH_SESSION_ATTRIBUTE_NAME, blahguaSession);
        return blahguaSession;
    }

}
