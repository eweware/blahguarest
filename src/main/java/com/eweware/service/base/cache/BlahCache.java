package com.eweware.service.base.cache;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.payload.InboxBlahPayload;
import com.eweware.service.base.store.dao.BaseDAOConstants;
import com.eweware.service.base.store.dao.InboxStateDAOConstants;
import com.eweware.service.base.store.dao.schema.InboxBlahSchema;
import com.eweware.service.base.store.dao.schema.SchemaSpec;
import com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.CASMutation;
import net.spy.memcached.CASMutator;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.ops.OperationStatus;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/6/12 Time: 8:50 PM
 * @deprecated
 */
public final class BlahCache {

    private static final Logger logger = Logger.getLogger(BlahCache.class.getName());

    private static BlahCache singleton;
    private static MongoStoreManager storeManager;
    private DBCollection inboxStateCollection;
    private DBCollection inboxCollection;
    private boolean memcachedEnabled = false; // By default, memcached is not used.


    /**
     * This is the namespace for an inbox status record
     */
    private static final String inboxStateNamespace = "i:";

    /**
     * This is the namespace for an inbox item
     */
    private static final String inboxItemNamespace = "I:";
    private static final int inboxItemIdStartIndex = inboxItemNamespace.length();


    private MemcachedClient client;
    private final BlahCacheConfiguration config;
    private Map<String, SchemaSpec> fieldNameToSpecMap;

    public static final BlahCache getInstance() {
        return singleton;
    }

    private DBCollection getInboxStateCollection() throws SystemErrorException {
        if (inboxStateCollection == null) {
            inboxStateCollection = getStoreManager().getCollection(storeManager.getInboxStateCollectionName());
        }
        return inboxStateCollection;
    }

    private DBCollection getInboxCollection() throws SystemErrorException {
        if (inboxCollection == null) {
            inboxCollection = getStoreManager().getCollection(storeManager.getBlahInboxCollectionName());
        }
        return inboxCollection;
    }


    public BlahCache(BlahCacheConfiguration config) throws SystemErrorException {
        if (singleton != null) {
            throw new SystemErrorException("Cache singleton already exists", ErrorCodes.SERVER_CACHE_ERROR);
        }
        try {
            if (memcachedEnabled) {
                initializeClient(config);
            }
            this.config = config;
            singleton = this;
            System.out.println("*** BlahCache initialized: " + config + " ***");
        } catch (IOException e) {
            throw new SystemErrorException("Cache cannot be created", e, ErrorCodes.SERVER_CACHE_ERROR);
        }
    }

    private void initializeClient(BlahCacheConfiguration config) throws IOException {
        try {
            if (client != null) {
                shutdownClient();
            }
            this.client = new MemcachedClient(AddrUtil.getAddresses(config.getHostname() + ":" + config.getPort()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize memcached client. Falling back on DB.", e);
        }
    }

    public void shutdown() {
        shutdownClient();
    }

    private void shutdownClient() {
        if (client != null) {
            try {
                client.shutdown();
                logger.info("Shut down memcached client");
            } finally {
                client = null;
            }
        }
    }

    // TODO provide wrapper calls to client.set, client.get, etc...

    // Inboxes -----------

    /**
     * Adds to the cache the specified blah's information as an inbox item.
     *
     * @param itemDBId  The unique database id for the inbox item
     * @param inboxItem A map with the inbox item's field/value pairs.
     * @param inbox     The inbox number. This is ignored if the groupId parameter is null;
     *                  otherwise, it is used, along with the groupId, to add a reference to
     *                  the new inboxItem in the inbox's state.
     * @param groupId   If this is not null, then (1) if there are no inboxes for the group,
     *                  a new inbox will be immediately created (inbox = 0), and
     *                  (2) the inbox's state will be updated to reference the new inboxItem.
     *                  That is, leaving this as null means that the new item will not be referenced
     *                  by the inbox state: this permits an application to add the
     *                  reference at a later time.
     * @throws SystemErrorException
     */
    public void addInboxItem(String itemDBId, final Map<String, Object> inboxItem, Integer inbox, final String groupId) {
        if (!memcachedEnabled || client == null) {
            return;
        }
        try {
            // Write the inbox item: this item is not referenced by the status
            final String itemCacheKey = makeInboxItemKey(itemDBId);

            final OperationStatus status = client.set(itemCacheKey, config.getInboxBlahExpiration(), inboxItem).getStatus();
            if (!status.isSuccess()) {
                logger.log(Level.SEVERE, "Inbox #" + inbox + ", groupId '" + groupId + ": Failed to add inbox item id '" + itemDBId + " to memcached. client.set status: '" + status.getMessage() + "'. Now backed by DB.");
                return; // don't throw error TODO should set an alarm
            }

            // Write a reference to the item in the status
            if (groupId != null) { // groupId may be null to defer this operation (e.g., by the stats application)
                final CASMutation<InboxState> casMutation = new CASMutation<InboxState>() {
                    @Override
                    public InboxState getNewValue(InboxState currentState) {
                        final List<String> update = new ArrayList<String>(currentState.getItemIds());
                        update.add(itemCacheKey);
                        return new InboxState(currentState.getTopInbox(), update);
                    }
                };
                // initial inbox state: the top inbox number is 0 (first inbox for group)
                final int topInbox = 0;
                final InboxState newState = new InboxState(topInbox, Arrays.asList(new String[]{itemCacheKey}));
                final String newStateKey = makeInboxStateKey(groupId, inbox);
                final CASMutator<InboxState> mutator = new CASMutator(client, client.getTranscoder());
                try {
                    mutator.cas(newStateKey, newState, 0, casMutation);
                } catch (Exception e) {
                    throw new SystemErrorException("failed cache write through mutator", e, ErrorCodes.SERVER_CACHE_ERROR);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Inbox #" + inbox + ", groupId '" + groupId + ": Failed to add inbox item id '" + itemDBId + " to memcached. Now backed by DB.", e);
            // don't throw it
            // TODO should set an alarm
        }
    }

//    final CASValue<Object> cas = client.getAndTouch(makeInboxStateKey(groupId, inbox), 0);

//    /**
//     * Sets the state of the specified group's inbox.
//     * This could be called, directly or indirectly, by the stats
//     * application (which uses the base library) or by the BlahManager.
//     * No attempt is made to synchronize this update: if
//     *
//     * @param groupId  The group id
//     * @param inbox    The inbox number
//     * @param topInbox The top inbox number (high watermark for this group (see notes in InboxStateDAOConstants for explanation)
//     * @param inboxIds A list of inbox item ids that belong to the group's inbox. Each id must have been
//     *                 generated using the makeInboxItemKey method of this class.
//     * @throws SystemErrorException
//     */
//    public void setInboxState(String groupId, Integer inbox, Integer topInbox, List<String> inboxIds) throws SystemErrorException {
//        // TODO this should be an atomic operation!
//        final InboxState state = new InboxState(topInbox, inboxIds);
//        final OperationStatus status = client.set(makeInboxStateKey(groupId, inbox), config.getInboxBlahExpiration(), state).getStatus();
//        if (!status.isSuccess()) {
//            throw new SystemErrorException("Failed to set inbox state: " + status.getMessage(), ErrorCodes.SERVER_CACHE_ERROR);
//        }
//    }

    /**
     * Returns the state of the specified group's inbox.
     *
     * @param groupId The group id
     * @param inbox   The inbox number
     * @return InboxState The state of the inbox
     */
    public InboxState getInboxState(String groupId, Integer inbox) throws SystemErrorException {
        Future<Object> future = null;
        try {
            if (memcachedEnabled && client != null) {
                future = client.asyncGet(makeInboxStateKey(groupId, inbox));
                return (InboxState) future.get(2, TimeUnit.SECONDS);  // 2 second timeout (default is 1)
            } else {
                return getInboxStateFromDB(groupId, inbox);
            }
        } catch (Exception e) {

            if (future != null) {
                future.cancel(false);
            }
            if (e instanceof TimeoutException) {
                logger.log(Level.WARNING, "Failed to get inbox state from memcached due to a timeout (set to 2 seconds)", e);
            }

            return getInboxStateFromDB(groupId, inbox);
        }
    }

    private InboxState getInboxStateFromDB(String groupId, Integer inbox) throws SystemErrorException {
        try {
//            logger.finer("Inbox #" + inbox + ", group id '" + groupId + "': Trying to get inbox state from DB...");
            final String stateId = makeInboxStateKey(groupId, inbox);
            final DBObject query = new BasicDBObject(BaseDAOConstants.ID, stateId);
            final DBObject state = getInboxStateCollection().findOne(query);
//            if (state != null) {
//                logger.finer("Inbox #" + inbox + ", group id '" + groupId + "': Successfully obtained inbox state from DB");
//            }
            return (state == null) ? null : toInboxState(state);
        } catch (SystemErrorException e1) {
            throw new SystemErrorException("DB error while trying to get inbox #" + inbox + " for group id '" + groupId + "'", e1, ErrorCodes.SERVER_CACHE_ERROR);
        }
    }

    private InboxState toInboxState(DBObject state) throws SystemErrorException {
        if (state == null) {
            return null;
        }
        final List<ObjectId> inboxItemObjectIds = (List<ObjectId>) state.get(InboxStateDAOConstants.INBOX_ITEM_IDS);
        if (inboxItemObjectIds == null) {
            throw new SystemErrorException("InboxState dao missing item object ids; dao=" + state, ErrorCodes.SERVER_CACHE_ERROR);
        }
        final Integer inbox = (Integer) state.get(InboxStateDAOConstants.INBOX_NUMBER_TOP);
        if (inbox == null) {
            throw new SystemErrorException("InboxState dao missing inbox number; dao=" + state, ErrorCodes.SERVER_CACHE_ERROR);
        }
        final List<String> inboxItemIds = new ArrayList<String>(inboxItemObjectIds.size());
        for (ObjectId id : inboxItemObjectIds) {
            inboxItemIds.add(id.toString());
        }
        return new InboxState(inbox, inboxItemIds);
    }

    private MongoStoreManager getStoreManager() throws SystemErrorException {
        if (storeManager == null) {
            storeManager = MongoStoreManager.getInstance();
        }
        return storeManager;
    }

    /**
     * Returns the cached inbox items for the specified group's inbox.
     *
     * @param groupId       The group id
     * @param inbox         The inbox number
     * @param start         The start index for the possibly sorted inbox
     * @param count         The number of inbox items to return (after optional sorting)
     * @param sortFieldName The name of a field by which to sort the inbox
     * @param sortDirection
     * @return Inbox    The inbox for the specified group and inbox number. Returns null
     *         if the specified inbox does not exist.
     */
    @SuppressWarnings("unchecked")
    public Inbox getInbox(String groupId, Integer inbox, Integer start, Integer count, final String sortFieldName, Integer sortDirection) throws SystemErrorException {

        // Get the inbox's cache state
        final InboxState state = getInboxState(groupId, inbox);
        if (state == null) { // no such inbox
            return null;
        }
        // Get inbox item references from the state
        final List<String> referencedItemKeys = state.getItemIds();
        if (referencedItemKeys.isEmpty()) { // The inbox exists, but it doesn't reference any items: return empty inbox
            return new Inbox(state.getTopInbox(), new ArrayList<InboxBlahPayload>(0));
        }

        // Bulk-fetch the referenced items
        final Map<String, Object> inboxReferenceItemIdToItemMap = doGetInboxItems(referencedItemKeys, groupId, inbox);

        final List<InboxBlahPayload> items = new ArrayList<InboxBlahPayload>(inboxReferenceItemIdToItemMap.size());
        for (Object value : inboxReferenceItemIdToItemMap.values()) {
            if (value instanceof BasicDBObject) {
                items.add(new InboxBlahPayload((BasicDBObject) value));
            } else {
                throw new SystemErrorException("inbox #" + inbox + " for group id '" + groupId + "': value should have been a BasicDBObject, but it was a '" + value.getClass().getSimpleName() + "'", value, ErrorCodes.SERVER_CACHE_ERROR);
            }
        }

        if (sortFieldName != null) { // sort first
            maybeCacheSchema();
            final SchemaSpec spec = fieldNameToSpecMap.get(sortFieldName);
            if (spec != null) { // ignore if there's no spec for it
                Collections.sort(items, new FieldMapComparator(sortFieldName, sortDirection, spec));
            }
        }

        if (count == null || count >= items.size()) {
            return new Inbox(state.getTopInbox(), items);
        }

        int index = 0;
        int counter = 0;
        boolean limit = (count != null);
        final List<InboxBlahPayload> selected = new ArrayList<InboxBlahPayload>(count);
        // TODO: refine paging to deal with content semantics; this is now dumb simple
        for (InboxBlahPayload item : items) {
            if (limit && counter >= count) {
                break;
            }
            index++;
            if (start == null || start < index) {
                selected.add(item);
                counter++;
            }
        }
        return new Inbox(state.getTopInbox(), selected);
    }

    /**
     * <p>Try to get the inbox items from memcached. If that fails, try to get them from the database.</p>
     *
     * @param keys    Inbox item ids
     * @param groupId
     * @param inbox   @return Map whose values are the DBObject instances for each inbox item
     */
    private Map<String, Object> doGetInboxItems(List<String> keys, String groupId, Integer inbox) throws SystemErrorException {
        BulkFuture<Map<String, Object>> future = null;
        try {
            if (memcachedEnabled && client != null) {
                future = client.asyncGetBulk(keys);
                return future.get(2, TimeUnit.SECONDS);  // 2 second timeout (default is 1)
            } else {
                return doGetInboxItemsFromDB(keys, groupId, inbox);
            }
        } catch (Exception e) {

            if (future != null) {
                future.cancel(false);
            }

            if (e instanceof TimeoutException) {
                logger.log(Level.WARNING, "Inbox #" + inbox + ", group id '" + groupId + ": Failed to get inbox state from memcached due to a timeout (set to 2 seconds)", e);
                // continue
            }

            return doGetInboxItemsFromDB(keys, groupId, inbox);
        }
    }

    private Map<String, Object> doGetInboxItemsFromDB(List<String> keys, String groupId, Integer inbox) throws SystemErrorException {
        final int keyCount = keys.size();
        try {
//            logger.finer("Inbox #" + inbox + ", group id '" + groupId + "': Trying to get inbox from DB for " + keyCount + " keys" + (keyCount > 0 ? (", starting with key " + keys.get(0)) : ""));
            // now retrieve the data from the database
            final List<ObjectId> oids = new ArrayList<ObjectId>(keyCount);
            final boolean memcachedKeyName = (keyCount > 0) && keys.get(0).startsWith(inboxItemNamespace);
            for (String key : keys) {
                final String id = memcachedKeyName ? getInboxItemIdFromItemKey(key) : key;
                try {
                    oids.add(new ObjectId(id));
                } catch (Exception e) {
                    throw new SystemErrorException("Failed to get inbox from DB due to an invalid object id '" + id + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
                }
            }

            final DBObject query = new BasicDBObject(BaseDAOConstants.ID, new BasicDBObject("$in", oids));
            final DBCursor cursor = getInboxCollection().find(query);
            final Map<String, Object> result = new HashMap<String, Object>(cursor.size()); // doesn't take limit into consideration!
            for (DBObject obj : cursor) {
                result.put(obj.get(BaseDAOConstants.ID).toString(), obj);
            }
//            logger.finer("Inbox #" + inbox + ", group id '" + groupId + "': successfully retrieved " + result.size() + " inbox items for " + keyCount + " keys");
            return result;
        } catch (Exception e1) {
            throw new SystemErrorException("Failed to get inbox from DB for " + keyCount + " inbox item keys " + (keyCount > 0 ? (", starting with key " + keys.get(0)) : ""), e1, ErrorCodes.SERVER_CACHE_ERROR);
        }
    }

    public void setMemcachedEnable(boolean enable) throws SystemErrorException {
        memcachedEnabled = enable;
        if (memcachedEnabled) {
            if (client != null) {
                try {
                    initializeClient(config);
                } catch (IOException e) {
                    throw new SystemErrorException("Failed to enable memcached", e, ErrorCodes.SERVER_CACHE_ERROR);
                }
            } else {
                logger.warning("While enabling memcached... client was already initialized");
            }
        } else {
            shutdownClient();
        }
        logger.warning("Memcached reads " + (enable ? "enabled" : "disabled"));
    }

    private void maybeCacheSchema() {
        final InboxBlahSchema schema = InboxBlahSchema.getSchema(LocaleId.en_us);
        this.fieldNameToSpecMap = schema.getFieldNameToSpecMap();
    }

    /**
     * Returns a key for the record containing a list of inbox item ids for the inbox number
     * belonging to the specified group id
     *
     * @param groupId The group id
     * @param inbox   The inbox number
     * @return String The id of the record in the inbox status collection
     *         that contains the inbox item ids in the inbox
     */
    public static final String makeInboxStateKey(String groupId, Integer inbox) {
        final StringBuilder b = new StringBuilder(inboxStateNamespace);
        b.append(groupId);
        b.append("-");
        b.append(inbox);
        return b.toString();
    }

    public static final String makeInboxItemKey(String itemId) {
        return inboxItemNamespace + itemId;
    }

    private static final String getInboxItemIdFromItemKey(String key) throws SystemErrorException {
        if (key == null || key.length() < 3) {
            throw new SystemErrorException("Inbox item key length is incorrect; key: '" + key + "'", ErrorCodes.INVALID_INBOX_ITEM_KEY);
        }
        return key.substring(inboxItemIdStartIndex);
    }
}
