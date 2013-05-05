package main.java.com.eweware.service.mgr;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.base.payload.BadgeAuthorityPayload;
import main.java.com.eweware.service.base.payload.BadgePayload;
import main.java.com.eweware.service.base.payload.BadgingNotificationEntity;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.dao.type.BadgeTransactionState;
import main.java.com.eweware.service.base.store.dao.type.DAOUpdateType;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 5:40 PM
 */
public final class BadgesManager {

    private static final Logger logger = Logger.getLogger("BadgesManager");

    private static final long YEAR_IN_MILLIS = (1000l * 60 * 60 * 24 * 365);

    private static final int PROTOCOL_ERROR_ACCESSING_AUTHORITY = 1;
    private static final int NETWORK_IO_ERROR_ACCESSING_AUTHORITY = 2;
    private static final int TIMEOUT_ACCESSING_AUTHORITY = 3;
    private static final int SEVERE_ERROR_ACCESSING_AUTHORITY = 4;


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
            logger.info("*** BadgesManager Started ***");
        } catch (SystemErrorException e) {
            throw new WebServiceException(e);
        }
    }

    public void shutdown() {
        logger.info("*** BadgesManager Shutdown ***");
    }


    /**
     * <p>Returns list of all authorities.</p>
     *
     * @return List of authorities.
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
     * @param resp        The response context
     * @param userId      The user id
     * @param authorityId The authority id
     * @param badgeTypeId
     * @return A map containing the badge information.
     */
    public Response createBadgeForUser(HttpServletResponse resp, String userId, String authorityId, String badgeTypeId) throws InvalidRequestException, SystemErrorException {
        // ignore badgeTypeId until we need to use it
        if (authorityId == null) {
            throw new InvalidRequestException("missing authority id", ErrorCodes.INVALID_INPUT);
        }
        authorityId = SystemManager.getInstance().isDevMode() ? "localhost:8081" : authorityId;
        final BadgeAuthorityDAO authDAO = (BadgeAuthorityDAO) storeManager.createBadgeAuthority(authorityId)._findByPrimaryId(BadgeAuthorityDAO.REST_ENDPOINT_URL);
        final String endpoint = makeBadgeAuthorityCreateBadgeEndpoint(authDAO);

        HttpEntity entity = null;
        HttpPost post = null;
        try {
            final BasicHttpContext httpContext = new BasicHttpContext(); // for this thread
            post = new HttpPost(endpoint);
            post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
//            System.out.println("*** POSTING TO ENDPOINT '" + endpoint + "'");

            // Set the entity
            final Map<String, Object> map = new HashMap<String, Object>(3);
            final String applicationName = "blahgua.com";
            final String password = "sheep";
            map.put("a", applicationName);
            map.put("p", password);
            final String jsonString = new ObjectMapper().writeValueAsString(map);
            final StringEntity stringEntity = new StringEntity(jsonString, "UTF-8");
            post.setEntity(stringEntity);

            // Execute remote method and get response
            final HttpResponse response = SystemManager.getInstance().getHttpClient().execute(post, httpContext);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.log(Level.SEVERE, "Failed to initiate badge tx for authority id '" +
                        authorityId + "' badgeTypeId '" + badgeTypeId + "'. Http Status = " + statusCode);
                return Response.status(statusCode).build();
            }

            // Record transaction & expectation of badge fulfillment
            entity = response.getEntity();
            final Map<String, Object> result = (Map<String, Object>) getAsBean(entity, Map.class);

            final String txToken = (String) result.get("tx");

            final DBObject transaction = new BasicDBObject(BadgeTransactionDAOConstants.ID, makeTransactionId(authorityId, txToken));
            transaction.put(BadgeTransactionDAOConstants.AUTHORITY_ID, authorityId);
            transaction.put(BadgeTransactionDAOConstants.STATE, BadgeTransactionState.PENDING.getCode());
            transaction.put(BadgeTransactionDAOConstants.USER_ID, userId);
            transaction.put(BadgeTransactionDAOConstants.CREATED, new Date());
            final DBCollection col = MongoStoreManager.getInstance().getCollection(MongoStoreManager.getInstance().getBadgeTransactionCollectionName());
            final WriteResult write = col.insert(transaction);
            if (write.getError() != null) {
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
            }

            return Response.ok(result.get("form")).build();

        } catch (ConnectionPoolTimeoutException e) {
            post.abort();
            logger.log(Level.SEVERE, "Timed out waiting for connection from authority id '" + authorityId + "'", e);
            return Response.status(Response.Status.NOT_FOUND).entity("error=" + TIMEOUT_ACCESSING_AUTHORITY).build();
        } catch (ClientProtocolException e) {
            post.abort();
            logger.log(Level.SEVERE, "Protocol error. Cannot access authority id '" + authorityId + "'", e);
            return Response.status(Response.Status.NOT_FOUND).entity("error=" + PROTOCOL_ERROR_ACCESSING_AUTHORITY).build();
        } catch (IOException e) {
            post.abort();
            logger.log(Level.SEVERE, "Network error accessing authority id '" + authorityId + "'", e);
            return Response.status(Response.Status.NOT_FOUND).entity("error=" + NETWORK_IO_ERROR_ACCESSING_AUTHORITY).build();
        } catch (Exception e) {
            post.abort();
            logger.log(Level.SEVERE, "Severe internal error accessing authority id '" + authorityId + "'", e);
            return Response.status(Response.Status.NOT_FOUND).entity("error=" + SEVERE_ERROR_ACCESSING_AUTHORITY).build();
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    throw new SystemErrorException("Error talking to badging authority", e, ErrorCodes.SERVER_SEVERE_ERROR);
                }
            }
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    /**
     * <p>Receives badge creation result from authority.</p>
     *
     * @param entity
     */
    public Response addBadge(Map<String, Object> entity) throws SystemErrorException {
        final String txId = (String) entity.get(BadgingNotificationEntity.TRANSACTION_ID_FIELDNAME);
        final String state = (String) entity.get(BadgingNotificationEntity.STATE_FIELDNAME);
        if (state.equals(BadgingNotificationEntity.STATE_GRANTED)) {
            return handleGrantedBadge(txId, entity);
        } else {
            return handleRefusedBadge(txId, state, entity);
        }
    }

    private Response handleRefusedBadge(String txId, String state, Map<String, Object> entity) throws SystemErrorException {
        final String authority = (String) entity.get(BadgingNotificationEntity.AUTHORITY_FIELDNAME);
        final DBCollection col = MongoStoreManager.getInstance().getCollection(MongoStoreManager.getInstance().getBadgeTransactionCollectionName());
        final BasicDBObject txQuery = new BasicDBObject(BadgeTransactionDAOConstants.ID, makeTransactionId(authority, txId));
        final DBObject tx = col.findOne(txQuery);
        if (tx == null) {
            return Response.status(HttpStatus.SC_CONFLICT).entity(makeError(BadgingNotificationEntity.ERROR_CODE_TRANSACTION_UNKNOWN, txId)).build();
        }
        BadgeTransactionState txState = null;
        if (state.equals(BadgingNotificationEntity.STATE_REFUSED)) {
            txState = BadgeTransactionState.REFUSED;
        } else if (state.equals(BadgingNotificationEntity.STATE_SERVER_ERROR)) {
            txState = BadgeTransactionState.SERVER_ERROR;
        } else if (state.equals(BadgingNotificationEntity.STATE_CANCELLED)) {
            txState = BadgeTransactionState.CANCELLED;
        } else {
            final String msg = "Unrecognized badging notification entity state '" + state + "'";
            logger.severe(msg);
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(makeError(BadgingNotificationEntity.ERROR_CODE_TRANSACTION_SERVER_ERROR, msg)).build();
        }
        final String authorityId = (String) tx.get(BadgeTransactionDAOConstants.AUTHORITY_ID);

        final BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(BadgeTransactionDAOConstants.STATE, txState.getCode()));
        final WriteResult result = col.update(txQuery, update);
        if (result.getError() != null) {
            logger.warning("Failed to update tx state for txId '" + makeTransactionId(authorityId, txId) + "'. DB error: " + result.getError());
            // fall through anyway
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    private Response handleGrantedBadge(String txId, Map<String, Object> entity) throws SystemErrorException {

        final String authority = (String) entity.get(BadgingNotificationEntity.AUTHORITY_FIELDNAME);
        final List<Map<String, Object>> badgeEntities = (List<Map<String, Object>>) entity.get("badges");
        logger.info("RECEIVED BADGES:\n" + entity);

        if (badgeEntities != null) {
            final DBCollection txCollection = MongoStoreManager.getInstance().getCollection(MongoStoreManager.getInstance().getBadgeTransactionCollectionName());
            final BasicDBObject txQuery = new BasicDBObject(BadgeTransactionDAOConstants.ID, makeTransactionId(authority, txId));
            DBObject tx = null;
            String userId = null;
            String authorityId = null;
            final List<String> badgeIds = new ArrayList<String>(badgeEntities.size());
            for (Map<String, Object> badgeEntity : badgeEntities) {

                if (tx == null) {
                    tx = txCollection.findOne(txQuery);
                    if (tx == null) {
                        return Response.status(HttpStatus.SC_CONFLICT).entity(makeError(BadgingNotificationEntity.ERROR_CODE_TRANSACTION_UNKNOWN, txId)).build();
                    }
                    userId = (String) tx.get(BadgeTransactionDAOConstants.USER_ID);
                    authorityId = (String) tx.get(BadgeTransactionDAOConstants.AUTHORITY_ID);
                }

                final String authorityBadgeId = (String) badgeEntity.get(BadgingNotificationEntity.BADGE_ID_FIELDNAME);
                final String badgeName = (String) badgeEntity.get(BadgingNotificationEntity.BADGE_NAME_FIELDNAME);
                final String badgeType = (String) badgeEntity.get(BadgingNotificationEntity.BADGE_TYPE_FIELDNAME);
                final String expires = (String) badgeEntity.get(BadgingNotificationEntity.EXPIRATION_DATETIME_FIELDNAME);
                final String iconUrl = (String) badgeEntity.get(BadgingNotificationEntity.ICON_URL_FIELDNAME);

                // Create badge
                final BadgeDAO badge = storeManager.createBadge();
                badge.setAuthorityId(authorityId);
                badge.setAuthorityBadgeId(authorityBadgeId);
                badge.setDisplayName(badgeName);
                badge.setBadgeType(badgeType);
                badge.setUserId(userId);
                if (expires != null) {
                    try {
                        badge.setExpirationDate(DateUtils.fromISODateTimeToUTC(expires));
                    } catch (ParseException e) {
                        logger.log(Level.SEVERE, "Parse error on expiration date '" + expires + "'", e);
                        badge.setExpirationDate(new Date(System.currentTimeMillis() + YEAR_IN_MILLIS));
                    }
                } else {
                    badge.setExpirationDate(new Date(System.currentTimeMillis() + YEAR_IN_MILLIS));
                }
                if (iconUrl != null) {
                    badge.setIconUrl(iconUrl);
                }
                badge._insert();
                badgeIds.add(badge.getId());
            }

            // Assign badge(s) to user record
            BadgeTransactionState newState = BadgeTransactionState.GRANTED;
            final UserDAO user = (UserDAO) storeManager.createUser(userId)._findByPrimaryId(UserDAO.BADGE_IDS);
            if (!storeManager.createUser(userId)._exists()) {
                logger.warning("User id '" + userId + "' for transaction id '" + txId + "' authority '" + authority + "'");
                newState = BadgeTransactionState.GRANTED_BUT_NO_USER_ID;
            } else {
                user.setBadgeIds(badgeIds);
                user._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
            }

            final BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(BadgeTransactionDAOConstants.STATE, newState.getCode()));
            final WriteResult result = txCollection.update(txQuery, update);
            if (result.getError() != null) {
                logger.warning("Failed to update tx state for txId '" + makeTransactionId(authorityId, txId) + "'. DB error: " + result.getError());
                // fall through anyway  TODO some background cleanup might be appropriate here
            }

            return Response.status(Response.Status.ACCEPTED).build();
        } else { // no badges actually received in payload
            return Response.status(HttpStatus.SC_CONFLICT).entity(makeError(BadgingNotificationEntity.ERROR_CODE_TRANSACTION_MISSING_BADGES, txId)).build();
        }
    }



    public static final Map<String, Object> makeError(int errorCode, String data) {
        Map<String, Object> error = new HashMap<String, Object>(2);
        error.put("error", errorCode);
        error.put("data", data);
        return error;
    }

    private String makeTransactionId(String authorityId, String token) {
        return (authorityId + token);
    }

    public static Object getAsBean(HttpEntity entity, Class beanClass) throws IOException {
        return new ObjectMapper().readValue(EntityUtils.toString(entity), beanClass);
    }

    private String makeBadgeAuthorityCreateBadgeEndpoint(BadgeAuthorityDAO authDAO) throws SystemErrorException {
        return (SystemManager.getInstance().isDevMode() ? "http://localhost:8081/v1" : authDAO.getRestEndpointUrl()) + "/badges/create";
    }

    public BadgePayload getBadgeById(String badgeId) throws SystemErrorException, InvalidRequestException {
        final BadgeDAO badge = (BadgeDAO) storeManager.createBadge(badgeId)._findByPrimaryId();
        if (badge == null) {
            throw new InvalidRequestException("Badge id '" + badgeId + "' doesn't exist", ErrorCodes.INVALID_INPUT);
        }
        return new BadgePayload(badge);
    }
}




//// Set up for the session cookie
//final CookieStore store = new BasicCookieStore();
//final HttpContext context = new BasicHttpContext();
//context.setAttribute(ClientContext.COOKIE_STORE, store);
//
// Get session id from cookie
//final Header header = response.getFirstHeader("Set-Cookie");
//final String sessionId = (header == null) ? null : header.getValue();
//if (sessionId != null) {
//        resp.setHeader("Set-Cookie", sessionId);
//} else {
//final String msg = "<p>Failed to establish session with authority</p>";
//logger.log(Level.SEVERE, msg);
//return msg;
//}