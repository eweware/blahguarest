package com.eweware.service.mgr;

import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.error.InvalidRequestException;
import com.eweware.service.base.error.ResourceNotFoundException;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.mgr.ManagerInterface;
import com.eweware.service.base.mgr.ManagerState;
import com.eweware.service.base.payload.AuthorizedState;
import com.eweware.service.base.payload.GroupPayload;
import com.eweware.service.base.payload.GroupTypePayload;
import com.eweware.service.base.store.StoreManager;
import com.eweware.service.base.store.dao.BaseDAO;
import com.eweware.service.base.store.dao.GroupDAO;
import com.eweware.service.base.store.dao.GroupDAOConstants;
import com.eweware.service.base.store.dao.GroupTypeDAO;
import com.eweware.service.base.store.dao.type.DAOUpdateType;
import com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;

import javax.xml.ws.WebServiceException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         TODO add monitors for mutating variables or lists with mutating elements
 *         TODO add transaction-level management (rollbacks)
 */
public final class GroupManager implements ManagerInterface {

    private static GroupManager singleton;

    private final Integer _returnedObjectLimit;

    /**
     * <p>The initial duration in milliseconds after which the caches should be refreshed.</p>
     */
    private static final long MINIMUM_CACHE_REFRESH_INTERVAL_IN_MILLIS = 1000 * 60 * 2;

    /**
     * <p>Factor by which a new cache refresh interval should be multiplied.</p>
     * <p>Since it is possible for a new inbox generation to take only a few milliseconds
     * longer from the previous one, we don't want to refresh too soon. Hence, we pad
     * the duration this factor.</p>
     */
    private static final double CACHE_REFRESH_INTERVAL_FACTOR = 1.1d;

    /**
     * <p>The actual interval after which we should refresh the caches.</p>
     */
    private long _cacheRefreshIntervalInMillis = MINIMUM_CACHE_REFRESH_INTERVAL_IN_MILLIS;

    /**
     * <p>Last time (in millis) that the caches were refreshed.</p>
     */
    private AtomicLong _cachesLastRefreshed = new AtomicLong(System.currentTimeMillis());

    /**
     * <p>Acquire this lock to modify either _groupCacheMap or _openGroupCacheMap.
     * Even though the maps are concurrent-safe, we are also locking for
     * the refresh times.</p>
     */
    private final Object _cacheLock = new Object();

    /**
     * <p>Maps a group id to its dao.</p>
     */
    private final ConcurrentHashMap<String, GroupDAO> _groupCacheMap = new ConcurrentHashMap<String, GroupDAO>();

    /**
     * <p>Maps an open group id to its payload.</p>
     */
    private final ConcurrentHashMap<String, GroupPayload> _openGroupCacheMap = new ConcurrentHashMap<String, GroupPayload>();


    private StoreManager _storeManager;
    private ManagerState _state = ManagerState.UNKNOWN;

    public static GroupManager getInstance() throws SystemErrorException {
        if (GroupManager.singleton == null) {
            throw new SystemErrorException("group manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return GroupManager.singleton;
    }

    public GroupManager(Integer returnedObjectLimit) {
        _returnedObjectLimit = returnedObjectLimit;
        GroupManager.singleton = this;
        _state = ManagerState.INITIALIZED;
        System.out.println("*** GroupManager initialized ***");
    }

    public void start() {
        try {
            this._storeManager = MongoStoreManager.getInstance();
            maybeRefreshGroupCache(true);
            _state = ManagerState.STARTED;
            System.out.println("*** GroupManager started ***");
        } catch (SystemErrorException e) {
            e.printStackTrace();
            throw new WebServiceException(e);
        }
    }

    /**
     * <p>Returns a cached group.</p>
     * @param groupId   The group's id
     * @return  Returns the group payload or null if no group with the specified id exists.
     */
    public GroupDAO getCachedGroup(String groupId) throws SystemErrorException {
        maybeRefreshGroupCache(false);
        return _groupCacheMap.get(groupId);
    }

    /**
     * <p>Refresh group caches if refresh time is up.</p>
     * @param force Do it unconditionally.
     * @throws SystemErrorException
     */
    public void maybeRefreshGroupCache(boolean force) throws SystemErrorException {
        final long lastTime = _cachesLastRefreshed.get();
        if (force || ( (System.currentTimeMillis() - lastTime) > _cacheRefreshIntervalInMillis) ) {
            synchronized (_cacheLock) { //
                if (lastTime == _cachesLastRefreshed.get()) { // check again
                    _cachesLastRefreshed.set(System.currentTimeMillis());
                    doRefreshGroupCache();
                }
            }
        }
    }

    /**
     * <p>Must be called under lock.</p>
     * <p>IMPORTANT: we assume that any added groups will be a strict
     * superset of existing groups!</p>
     */
    private void doRefreshGroupCache() throws SystemErrorException {
        final GroupDAO groupDAO = getStoreManager().createGroup();
        groupDAO.setState(AuthorizedState.A.toString());
        final List<GroupDAO> groups = (List<GroupDAO>) groupDAO._findMany();
        for (GroupDAO group : groups) {
            final Long duration = group.getLastInboxGeneratedDuration();
            if (duration != null) {  // adjust for the actual time it takes to generate inboxes
                _cacheRefreshIntervalInMillis = Math.max(Math.round(duration * CACHE_REFRESH_INTERVAL_FACTOR), _cacheRefreshIntervalInMillis);
//                System.out.println("_cacheRefreshIntervalInMillis=" + _cacheRefreshIntervalInMillis);
            }
            _groupCacheMap.put(group.getId(), group);
            final String desc = group.getDescriptor();
            if (desc != null && desc.equals(GroupDAOConstants.GroupDescriptor.VISIBILITY_OPEN.getCode())) {
                _openGroupCacheMap.put(group.getId(), new GroupPayload(group));
            }
        }
    }

    public void shutdown() {
        _state = ManagerState.SHUTDOWN;
        System.out.println("*** GroupManager shut down ***");
    }

    /**
     * @return List<GroupTypeDAOImpl>	Returns all group types or an empty list if there are none.
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<GroupTypePayload> getGroupTypes(LocaleId localeId, Integer start, Integer count, String sortFieldName) throws SystemErrorException {
        ensureReady();
        if (count == null || count > _returnedObjectLimit) {
            count = _returnedObjectLimit;
        }

        final GroupTypeDAO groupTypeDAO = getStoreManager().createGroupType();
        final List<? extends BaseDAO> groupTypes = groupTypeDAO._findMany(start, count, sortFieldName);

        final List<GroupTypePayload> payload = new ArrayList<GroupTypePayload>(groupTypes.size());
        for (BaseDAO dao : groupTypes) {
            payload.add(new GroupTypePayload(dao));
        }
        return payload;
    }

    /**
     * @return GroupTypeDAOImpl Returns a group type by id or null if it doesn't exist.
     * @throws InvalidRequestException
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    public GroupTypePayload getGroupTypeById(LocaleId localeId, String groupTypeId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        ensureReady();
        if (isEmptyString(groupTypeId)) {
            throw new InvalidRequestException("missing group type id", ErrorCodes.MISSING_GROUP_TYPE_ID);
        }
        final GroupTypeDAO dao = (GroupTypeDAO) getStoreManager().createGroupType(groupTypeId)._findByPrimaryId();
        if (dao == null) {
            throw new ResourceNotFoundException("not found: group type id=" + groupTypeId, ErrorCodes.NOT_FOUND_GROUP_TYPE_ID);
        }

        return new GroupTypePayload(dao);
    }


    /**
     * <p>Returns true if the group is open (e.g., open for
     * reading by anonymous users).</p>
     *
     * @param groupId The group id
     * @return True if the group is open
     */
    public boolean isOpenGroup(String groupId) throws SystemErrorException {
        if (groupId == null) {
            return false;
        }
        // This is fairly static (and is called often), so no need to check cache refresh
        return _openGroupCacheMap.containsKey(groupId);
    }


    /**
     * @param localeId
     * @param groupTypeId   A group type id (optional).
     * @param displayName   A group's display name (optional).
     * @param state         A group _state (optional: if null, all groups are returned).
     * @param start         Optional start index
     * @param count         Optional number of items to fetch
     * @param sortFieldName
     * @return List<GroupDAOImpl>	Returns a possibly empty list of groups of the
     *         given type and _state.
     * @throws InvalidRequestException
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<GroupPayload> getGroups(LocaleId localeId, String groupTypeId, String displayName, String state, Integer start, Integer count, String sortFieldName) throws SystemErrorException, InvalidRequestException {
        ensureReady();
        if (count == null) {
            count = _returnedObjectLimit;
        }
        if (state != null) {
            final AuthorizedState authorizedState = AuthorizedState.valueOf(state);
            if (authorizedState == null) {
                throw new InvalidRequestException("requested an invalid group state", state, ErrorCodes.INVALID_STATE_CODE);
            }
        }
        final GroupDAO groupDAO = getStoreManager().createGroup();
        if (!isEmptyString(displayName)) {
            groupDAO.setDisplayName(displayName);
        }
        if (!isEmptyString(groupTypeId)) {
            groupDAO.setGroupTypeId(groupTypeId);
        }
        if (state != null) {
            groupDAO.setState(state);
        }

        final List<? extends BaseDAO> results = groupDAO._findMany(start, count, sortFieldName);

        final List<GroupPayload> groups = new ArrayList<GroupPayload>(results.size());
        for (BaseDAO dao : results) {
            groups.add(new GroupPayload(dao));
        }
        return groups;
    }

    /**
     * Returns all active anonymous groups in the system.
     *
     * @param localeId The locale id
     * @param start    Start index into a page
     * @param count    Item count per page
     * @return All active anonymous groups in the system
     * @throws SystemErrorException
     */
    public Collection<GroupPayload> getOpenGroups(LocaleId localeId, Integer start, Integer count) throws SystemErrorException {
        ensureReady();
        return _openGroupCacheMap.values();
    }

    /**
     * @param localeId
     * @param groupId  A group id
     * @return GroupDAOImpl    Returns a dao for the group or null if the group doesn't exist.
     * @throws InvalidRequestException
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    public GroupPayload getGroupById(LocaleId localeId, String groupId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        ensureReady();
        if (groupId == null || groupId.length() == 0) {
            throw new InvalidRequestException("missing group id", ErrorCodes.MISSING_GROUP_ID);
        }
        final BaseDAO dao = getStoreManager().createGroup(groupId)._findByPrimaryId();
        if (dao == null) {
            throw new ResourceNotFoundException("no group exists with given id", groupId, ErrorCodes.NOT_FOUND_GROUP_ID);
        }
        return new GroupPayload(dao);
    }

    /**
     * Updates the viewer count for this group
     *
     * @param groupId The group id
     * @param added   If true, add one to the count, else subtract one
     */
    public void updateViewerCountInDB(String groupId, Boolean added) throws SystemErrorException, ResourceNotFoundException {
        ensureReady();
        final GroupDAO group = getStoreManager().createGroup(groupId);
        if (!group._exists()) {
            throw new ResourceNotFoundException("No such group id '" + groupId + "'", ErrorCodes.NOT_FOUND_GROUP_ID);
        }
        group.setCurrentViewerCount(added ? 1L : -1L);
        group._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
    }

    /**
     * Returns the number of current viewers in this channel
     *
     * @return A group payload with the current viewer count
     */
    public GroupPayload getViewerCount(String groupId) throws SystemErrorException {
        ensureReady();
        final GroupDAO groupDAO = (GroupDAO) getStoreManager().createGroup(groupId)._findByPrimaryId(GroupDAO.CURRENT_VIEWER_COUNT);
        if (groupDAO == null) {
            throw new SystemErrorException("Group id '" + groupId + "' not found", ErrorCodes.SERVER_SEVERE_ERROR);
        }
        final GroupPayload groupPayload = new GroupPayload(groupId);
        if (groupDAO.getCurrentViewerCount() == null) {
            groupPayload.setCurrentViewerCount(0L);
        }
        groupPayload.setCurrentViewerCount(groupDAO.getCurrentViewerCount());
        return groupPayload;
    }

    private boolean isEmptyString(String string) {
        return (string == null || string.length() == 0);
    }

    // Internal ------------------------------------------------------------------------------------------------

    public ManagerState getState() {
        return _state;
    }

    private StoreManager getStoreManager() {
        return _storeManager;
    }

    private void ensureReady() throws SystemErrorException {
        if (_state != ManagerState.STARTED) {
            throw new SystemErrorException("System not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
    }
}



