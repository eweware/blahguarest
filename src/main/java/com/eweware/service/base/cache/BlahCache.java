package main.java.com.eweware.service.base.cache;

import com.mongodb.BasicDBObject;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.InboxBlahPayload;
import main.java.com.eweware.service.base.store.dao.schema.InboxBlahSchema;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.CASMutation;
import net.spy.memcached.CASMutator;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ops.OperationStatus;

import java.io.IOException;
import java.util.*;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/6/12 Time: 8:50 PM
 */
public final class BlahCache {

    private static BlahCache singleton;

    /**
     * This is the namespace for an inbox status record
     */
    private static final String inboxStateNamespace = "i:";

    /**
     * This is the namespace for an inbox item
     */
    private static final String inboxItemNamespace = "I:";


    private final MemcachedClient client;
    private final BlahCacheConfiguration config;
    private Map<String, SchemaSpec> fieldNameToSpecMap;

    public static final BlahCache getInstance() {
        return singleton;
    }

    /**
     * TODO remove call: used temporarily for testing clients...
     *
     * @return MemcachedClient Returns the memcached client implementation.
     */
    public final MemcachedClient getClient() {
        return client;
    }

    public BlahCache(BlahCacheConfiguration config) throws SystemErrorException {
        if (singleton != null) {
            throw new SystemErrorException("Cache singleton already exists", ErrorCodes.SERVER_CACHE_ERROR);
        }
        try {
            this.client = new MemcachedClient(AddrUtil.getAddresses(config.getHostname() + ":" + config.getPort()));
            this.config = config;
            singleton = this;
            System.out.println("*** BlahCache initialized: "+config+" ***");
        } catch (IOException e) {
            throw new SystemErrorException("Cache cannot be created", e, ErrorCodes.SERVER_CACHE_ERROR);
        }
    }

    public void shutdown() {
        if (client != null) {
            client.shutdown();
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
    public void addInboxItem(String itemDBId, final Map<String, Object> inboxItem, Integer inbox, final String groupId) throws SystemErrorException {

        // Write the inbox item: this item is not referenced by the status
        final String itemCacheKey = makeInboxItemKey(itemDBId);
        final OperationStatus status = client.set(itemCacheKey, config.getInboxBlahExpiration(), inboxItem).getStatus();
        if (!status.isSuccess()) {
            throw new SystemErrorException("Failed to add blah to inbox: " + status.getMessage(), ErrorCodes.SERVER_CACHE_ERROR);
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
    }

//    final CASValue<Object> cas = client.getAndTouch(makeInboxStateKey(groupId, inbox), 0);

    /**
     * Sets the state of the specified group's inbox.
     * This could be called, directly or indirectly, by the stats
     * application (which uses the base library) or by the BlahManager.
     * No attempt is made to synchronize this update: if
     *
     * @param groupId  The group id
     * @param inbox    The inbox number
     * @param topInbox The top inbox number (high watermark for this group (see notes in InboxStateDAOConstants for explanation)
     * @param inboxIds A list of inbox item ids that belong to the group's inbox. Each id must have been
     *                 generated using the makeInboxItemKey method of this class.
     * @throws SystemErrorException
     */
    public void setInboxState(String groupId, Integer inbox, Integer topInbox, List<String> inboxIds) throws SystemErrorException {
        // TODO this should be an atomic operation!
        final InboxState state = new InboxState(topInbox, inboxIds);
        final OperationStatus status = client.set(makeInboxStateKey(groupId, inbox), config.getInboxBlahExpiration(), state).getStatus();
        if (!status.isSuccess()) {
            throw new SystemErrorException("Failed to set inbox state: " + status.getMessage(), ErrorCodes.SERVER_CACHE_ERROR);
        }
    }

    /**
     * Returns the state of the specified group's inbox.
     *
     * @param groupId The group id
     * @param inbox   The inbox number
     * @return InboxState The state of the inbox
     */
    public InboxState getInboxState(String groupId, Integer inbox) {
        return (InboxState) client.get(makeInboxStateKey(groupId, inbox));
    }

    /**
     * Returns the cached inbox items for the specified group's inbox.
     *
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
        final Map<String, Object> inboxReferenceItemKeyToItemMap = client.getBulk(referencedItemKeys);
        final List<InboxBlahPayload> items = new ArrayList<InboxBlahPayload>(inboxReferenceItemKeyToItemMap.size());
        for (Object value : inboxReferenceItemKeyToItemMap.values()) {
            if (value instanceof BasicDBObject) {
                items.add(new InboxBlahPayload((BasicDBObject) value));
            } else {
                throw new SystemErrorException("inbox value from cache should have been a BasicDBObject, but it was a '" + value.getClass().getSimpleName() + "'", value, ErrorCodes.SERVER_SEVERE_ERROR);
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
    public final String makeInboxStateKey(String groupId, Integer inbox) {
        final StringBuilder b = new StringBuilder(inboxStateNamespace);
        b.append(groupId);
        b.append("-");
        b.append(inbox);
        return b.toString();
    }

    public final String makeInboxItemKey(String itemId) {
        return inboxItemNamespace + itemId;
    }
}
