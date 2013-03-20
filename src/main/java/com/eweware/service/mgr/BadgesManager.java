package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.payload.BadgeAuthorityPayload;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.BadgeAuthorityDAO;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.MediaType;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 5:40 PM
 */
public final class BadgesManager {

    private static final Logger logger = Logger.getLogger("BadgesManager");


    private static BadgesManager singleton;
    private static StoreManager storeManager;

    public BadgesManager() {
        singleton = this;
    }
    public static BadgesManager getInstance() {
        return singleton;
    }

    public void start() {
        try {
            storeManager = MongoStoreManager.getInstance();
        } catch (SystemErrorException e) {
            throw new WebServiceException(e);
        }
    }

    public void shutdown() {
    }



    /**
     * <p>Returns list of all authorities.</p>
     * @return  List of authorities.
     * @throws SystemErrorException
     */
    public List<BadgeAuthorityPayload> getAuthorities() throws SystemErrorException {
        final BadgeAuthorityDAO auth = storeManager.createBadgeAuthority();
        final List<? extends BaseDAO> baseDAOs = auth._findMany(0, null, null);
        final List<BadgeAuthorityPayload> entity = new ArrayList<BadgeAuthorityPayload>(baseDAOs.size());
        for (BaseDAO dao : baseDAOs) {
            entity.add(new BadgeAuthorityPayload(dao));
        }
        return entity;
    }

    /**
     * <p>Requests the specified authority to create a badge for the user.</p>
     * <p>The user might already have obtained a badge from this authority. If so,
     * the authority will return that badge.</p>
     *
     * @param userId      The user id
     * @param authorityId The authority id
     * @param badgeTypeId
     * @return A map containing the badge information.
     */
    public String createBadgeForUser(String userId, String authorityId, String badgeTypeId) throws SystemErrorException, InvalidRequestException {
        // ignore badgeTypeId until we need to use it
        if (authorityId == null) {
            throw new InvalidRequestException("missing authority id", ErrorCodes.INVALID_INPUT);
        }
        final BadgeAuthorityDAO authDAO = (BadgeAuthorityDAO) storeManager.createBadgeAuthority(authorityId)._findByPrimaryId(BadgeAuthorityDAO.ENDPOINT_URL);
        final String endpoint = (SystemManager.getInstance().isDevMode() ? "http://localhost:8081" : authDAO.getEndpointUrl()) + "/ref1/badges/" + userId;

        HttpEntity entity = null;
        HttpGet get = null;
        try {
            get = new HttpGet(endpoint);
            get.setHeader("Content-Type", MediaType.TEXT_HTML);
            final HttpResponse response = SystemManager.getInstance().getHttpClient().execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new SystemErrorException("Authority won't create badge (http status=" + statusCode + ")", ErrorCodes.SERVER_RECOVERABLE_ERROR);
            }
            entity = response.getEntity();
            final String data = EntityUtils.toString(entity);
            return data;
        } catch (ClientProtocolException e) {
            throw new SystemErrorException("Cannot access authority", e, ErrorCodes.SERVER_SEVERE_ERROR);
        } catch (IOException e) {
            throw new SystemErrorException("Network error accessing authority", e, ErrorCodes.SERVER_RECOVERABLE_ERROR);
        } finally { // TODO this resource should be managed for the application as a whole by the SystemManager
            if (entity != null) {
                try {
                    entity.getContent().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (get != null) {
                get.releaseConnection();
            }
        }
    }
}
