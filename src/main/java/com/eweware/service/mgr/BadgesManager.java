package main.java.com.eweware.service.mgr;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.mgr.ManagerState;
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

    private static final Logger logger = Logger.getLogger(BadgesManager.class.getName());

    private static final long YEAR_IN_MILLIS = (1000l * 60 * 60 * 24 * 365);

    private static final int PROTOCOL_ERROR_ACCESSING_AUTHORITY = 1;
    private static final int NETWORK_IO_ERROR_ACCESSING_AUTHORITY = 2;
    private static final int TIMEOUT_ACCESSING_AUTHORITY = 3;
    private static final int SEVERE_ERROR_ACCESSING_AUTHORITY = 4;


    private static BadgesManager singleton;
    private static StoreManager storeManager;

    private ManagerState state = ManagerState.UNKNOWN;

    public BadgesManager() {
        singleton = this;
        state = ManagerState.INITIALIZED;
    }

    public static BadgesManager getInstance() {
        return singleton;
    }

    public void start() {
        try {
            storeManager = MongoStoreManager.getInstance();
            state = ManagerState.STARTED;
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
        ensureReady();
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
    public Response createBadgeForUser(String userId, String authorityId, String badgeTypeId) throws InvalidRequestException, SystemErrorException {
        ensureReady();
        if (authorityId == null) {
            throw new InvalidRequestException("missing authority id", ErrorCodes.INVALID_INPUT);
        }
        if (userId == null) {
            throw new InvalidRequestException("missing user id", ErrorCodes.MISSING_USER_ID);
        }
//        if (activeBadgesForAuthorityExist(userId, authorityId, badgeTypeId)) {
//            return Response.status(Response.Status.ACCEPTED).build();
//        }
        authorityId = SystemManager.getInstance().isDevMode() ? "localhost:8081" : authorityId;
        final BadgeAuthorityDAO authDAO = (BadgeAuthorityDAO) storeManager.createBadgeAuthority(authorityId)._findByPrimaryId(BadgeAuthorityDAO.REST_ENDPOINT_URL, BadgeAuthorityDAO.DISPLAY_NAME);
        if (authDAO == null) {
            throw new InvalidRequestException("Invalid authority id '" + authorityId + "'", ErrorCodes.INVALID_INPUT);
        }
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
            transaction.put(BadgeTransactionDAOConstants.AUTHORITY_DISPLAY_NAME, authDAO.getDisplayName());
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

//    /**
//     * <p>Returns true if either (1) the user has already an unexpired badge of the
//     * specified badge type, or (2) when the badge type isn't specified, when
//     * there is at least one badge from the current authority (i.e., of any type).</p>
//     * @param userId
//     * @param authorityId
//     * @param badgeTypeId
//     * @return   <p>Returns true if there is at least one badge from the current authority.</p>
//     * @throws SystemErrorException
//     */
//    private boolean activeBadgesForAuthorityExist(String userId, String authorityId, String badgeTypeId) throws SystemErrorException {
//        final BadgeDAO badgeDAO = storeManager.createBadge();
//        // TODO create index for this at cost peril if that's the way we're eventually going to do this!
//        //  Alternately, create a composite key with user id, authority id, and blah type. But that complexity is not currently time-warranted.
//        badgeDAO.setUserId(userId);
//        badgeDAO.setAuthorityId(authorityId);
//        if (badgeTypeId != null) {
//            badgeDAO.setBadgeType(badgeTypeId);
//        }
//        // TODO check expiration date
//        return badgeDAO._exists();
//    }

    /**
     * <p>Receives badge creation result from authority.</p>
     *
     * @param entity
     */
    public Response addBadge(Map<String, Object> entity) throws SystemErrorException {
        ensureReady();
        final String txId = (String) entity.get(BadgingNotificationEntity.TRANSACTION_ID_FIELDNAME);
        final String state = (String) entity.get(BadgingNotificationEntity.STATE_FIELDNAME);
        if (state.equals(BadgingNotificationEntity.STATE_GRANTED)) {
            return handleGrantedBadge(txId, entity);
        } else {
            return handleRefusedBadge(txId, state, entity);
        }
    }

    public BadgePayload getBadgeById(String badgeId) throws SystemErrorException, InvalidRequestException {
        ensureReady();
        final BadgeDAO badge = (BadgeDAO) storeManager.createBadge(badgeId)._findByPrimaryId();
        if (badge == null) {
            throw new InvalidRequestException("Badge id '" + badgeId + "' doesn't exist", ErrorCodes.INVALID_INPUT);
        }
        return new BadgePayload(badge);
    }

    public void deleteBadgeForUser(String userId, String badgeId) throws SystemErrorException, ResourceNotFoundException, StateConflictException {
        ensureReady();
        final BadgeDAO badgeDAO = (BadgeDAO) storeManager.createBadge(badgeId)._findByPrimaryId(BadgeDAOConstants.USER_ID);
        if (badgeDAO == null) {
            throw new ResourceNotFoundException("Badge id '" + badgeId + "' not found", ErrorCodes.NOT_FOUND_BADGE_ID);
        }
        final String uid = badgeDAO.getUserId();
        if (uid != null) {
            if (uid.equals(userId)) {
                badgeDAO._deleteByPrimaryId();
            } else {
                throw new StateConflictException("Badge id '" + badgeId + "' is not owned by authenticated user id '" + userId + "'", ErrorCodes.BADGE_NOT_OWNED_BY_USER);
            }
        } else {
            logger.warning("Badge id '" + badgeId + "' had null user id field; expected user id '" + userId + "'");
            // fall through
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

    /**
     * <p>Called by badge authority when new badges have been granted for a user.</p>
     * @param txId   The transaction id. Transaction record contains history of transaction and current state.
     * @param entity The entity posted by the badging authority containing badges, etc.
     * @return
     * @throws SystemErrorException
     */
    private Response handleGrantedBadge(String txId, Map<String, Object> entity) throws SystemErrorException {

        final String authority = (String) entity.get(BadgingNotificationEntity.AUTHORITY_FIELDNAME);
        final List<Map<String, Object>> badgeEntities = (List<Map<String, Object>>) entity.get("badges");
        logger.finer("finer RECEIVED BADGES:\n" + entity);
        logger.info("info RECEIVED BADGES:\n" + entity);

        if (badgeEntities != null) {
            final DBCollection txCollection = MongoStoreManager.getInstance().getCollection(MongoStoreManager.getInstance().getBadgeTransactionCollectionName());
            final BasicDBObject txQuery = new BasicDBObject(BadgeTransactionDAOConstants.ID, makeTransactionId(authority, txId));
            DBObject tx = null;
            String userId = null;
            String authorityId = null;
            String authorityDisplayName = null;

            final List<String> badgeIds = new ArrayList<String>(badgeEntities.size());
            for (Map<String, Object> badgeEntity : badgeEntities) {

                if (tx == null) {
                    tx = txCollection.findOne(txQuery);
                    if (tx == null) {
                        logger.log(Level.SEVERE, "Unknown tx id '" + txId + "' entity=" + entity);
                        return Response.status(HttpStatus.SC_CONFLICT).entity(makeError(BadgingNotificationEntity.ERROR_CODE_TRANSACTION_UNKNOWN, txId)).build();
                    }
                    userId = (String) tx.get(BadgeTransactionDAOConstants.USER_ID);
                    authorityId = (String) tx.get(BadgeTransactionDAOConstants.AUTHORITY_ID);
                    authorityDisplayName = (String) tx.get(BadgeTransactionDAOConstants.AUTHORITY_DISPLAY_NAME);
                }

                final String authorityBadgeId = (String) badgeEntity.get(BadgingNotificationEntity.BADGE_ID_FIELDNAME);
                final String badgeName = (String) badgeEntity.get(BadgingNotificationEntity.BADGE_NAME_FIELDNAME);
                final String badgeTypeId = (String) badgeEntity.get(BadgingNotificationEntity.BADGE_TYPE_ID_FIELDNAME);
                final String expires = (String) badgeEntity.get(BadgingNotificationEntity.EXPIRATION_DATETIME_FIELDNAME);
                final String iconUrl = (String) badgeEntity.get(BadgingNotificationEntity.ICON_URL_FIELDNAME);

                // Create or update badge
                final BadgeDAO badge = storeManager.createBadge();
                final String blahguaAuthorityBadgeId = makeBadgeAuthorityBadgeId(authorityId, authorityBadgeId);
                badge.setAuthorityBadgeId(blahguaAuthorityBadgeId);
                badge.setUserId(userId);
                final BadgeDAO existingBadgeDAO = (BadgeDAO) badge._findByCompositeId(new String[]{BadgeDAO.ID}, BadgeDAO.AUTHORITY_BADGE_ID, BadgeDAO.USER_ID);
                final String existingBadgeID = (existingBadgeDAO == null) ? null : existingBadgeDAO.getId();
                if (existingBadgeDAO == null) {
                    logger.finer("New badge '" + badgeName + "' for user id '" + userId + "' for id '" + blahguaAuthorityBadgeId + "'");
                } else {
                    logger.finer("Updating badge '" + badgeName + "' for user id '" + userId + "' for id '" + blahguaAuthorityBadgeId + "'");
                }

                badge.setUserId(userId);
                badge.setAuthorityId(authorityId);
                badge.setAuthorityDisplayName(authorityDisplayName);
                badge.setBadgeType(badgeTypeId);
                badge.setDisplayName(badgeName);
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

                if (existingBadgeID != null) {
                    badge.setId(existingBadgeID);
                    badge._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
                } else {
                    badge._insert();
                    badgeIds.add(badge.getId());  // Add only new badges
                }
            }

            // Assign badge(s) to user record
            BadgeTransactionState newState = BadgeTransactionState.GRANTED;
            final UserDAO user = (UserDAO) storeManager.createUser(userId)._findByPrimaryId(UserDAO.BADGE_IDS);
            if (!storeManager.createUser(userId)._exists()) {
                logger.severe("User id '" + userId + "' for transaction id '" + txId + "' authority '" + authority + "'");
                newState = BadgeTransactionState.GRANTED_BUT_NO_USER_ID;
            } else  {
                user.setBadgeIds(badgeIds);
                user._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
            }

            // Update transaction
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

    /**
     * <p>Given the original badge authority's badge id, it creates a blahgua-unique id for it
     * by suffixing the original badge id with the authority's id.</p>
     */
    private String makeBadgeAuthorityBadgeId(String authorityId, String originalBadgeAuthorityBadgeId) {
        final StringBuilder b = new StringBuilder(authorityId);
        b.append("|");
        b.append(originalBadgeAuthorityBadgeId);
        return b.toString();
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


    private void ensureReady() throws SystemErrorException {
        if (state != ManagerState.STARTED) {
            throw new SystemErrorException("System not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
    }
}
