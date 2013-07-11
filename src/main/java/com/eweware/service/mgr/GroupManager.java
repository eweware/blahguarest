package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.payload.AuthorizedState;
import main.java.com.eweware.service.base.payload.GroupPayload;
import main.java.com.eweware.service.base.payload.GroupTypePayload;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.GroupDAO;
import main.java.com.eweware.service.base.store.dao.GroupDAOConstants;
import main.java.com.eweware.service.base.store.dao.GroupTypeDAO;
import main.java.com.eweware.service.base.store.dao.type.DAOUpdateType;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;

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

    private final Integer returnedObjectLimit;

    private static final long MINIMUM_CACHE_REFRESH_INTERVAL_IN_MILLIS = 1000 * 60 * 2;
    private long _cacheRefreshIntervalInMillis = MINIMUM_CACHE_REFRESH_INTERVAL_IN_MILLIS;
    private AtomicLong _cacheLastRefreshed = new AtomicLong(System.currentTimeMillis());

    /**
     * Acquire this lock to modify either _groupCacheMap or _openGroupCacheMap.
     * Even though the maps are concurrent-safe, we are also locking for
     * the refresh times.
     */
    private final Object _cacheLock = new Object();

    /**
     * Maps a group id to its payload.
     */
    private final ConcurrentHashMap<String, GroupDAO> _groupCacheMap = new ConcurrentHashMap<String, GroupDAO>();

    /**
     * Maps an open group id to its payload
     */
    private final ConcurrentHashMap<String, GroupPayload> _openGroupCacheMap = new ConcurrentHashMap<String, GroupPayload>();


    public static GroupManager getInstance() throws SystemErrorException {
        if (GroupManager.singleton == null) {
            throw new SystemErrorException("group manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return GroupManager.singleton;
    }

    private StoreManager storeManager;
    private ManagerState state = ManagerState.UNKNOWN;

    public GroupManager(Integer returnedObjectLimit) {
        this.returnedObjectLimit = returnedObjectLimit;
        GroupManager.singleton = this;
        state = ManagerState.INITIALIZED;
        System.out.println("*** GroupManager initialized ***");
    }

    public void start() {
        try {
            this.storeManager = MongoStoreManager.getInstance();
            maybeRefreshGroupCache(true);
            state = ManagerState.STARTED;
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
        final long lastTime = _cacheLastRefreshed.get();
        if (force || ( (System.currentTimeMillis() - lastTime) > _cacheRefreshIntervalInMillis) ) {
            synchronized (_cacheLock) { //
                if (lastTime == _cacheLastRefreshed.get()) { // check again
                    _cacheLastRefreshed.set(System.currentTimeMillis());
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
                _cacheRefreshIntervalInMillis = Math.max(duration, _cacheRefreshIntervalInMillis);
            }
            _groupCacheMap.putIfAbsent(group.getId(), group);
            final String desc = group.getDescriptor();
            if (desc != null && desc.equals(GroupDAOConstants.GroupDescriptor.VISIBILITY_OPEN.getCode())) {
                _openGroupCacheMap.putIfAbsent(group.getId(), new GroupPayload(group));
            }
        }
    }

    public void shutdown() {
        this.state = ManagerState.SHUTDOWN;
        System.out.println("*** GroupManager shut down ***");
    }

    /**
     * @return List<GroupTypeDAOImpl>	Returns all group types or an empty list if there are none.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<GroupTypePayload> getGroupTypes(LocaleId localeId, Integer start, Integer count, String sortFieldName) throws SystemErrorException {
        ensureReady();
        if (count == null || count > returnedObjectLimit) {
            count = returnedObjectLimit;
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
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
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
     * @param state         A group state (optional: if null, all groups are returned).
     * @param start         Optional start index
     * @param count         Optional number of items to fetch
     * @param sortFieldName
     * @return List<GroupDAOImpl>	Returns a possibly empty list of groups of the
     *         given type and state.
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<GroupPayload> getGroups(LocaleId localeId, String groupTypeId, String displayName, String state, Integer start, Integer count, String sortFieldName) throws SystemErrorException, InvalidRequestException {
        ensureReady();
        if (count == null) {
            count = returnedObjectLimit;
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
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
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
        return this.state;
    }

    private StoreManager getStoreManager() {
        return storeManager;
    }

    private void ensureReady() throws SystemErrorException {
        if (state != ManagerState.STARTED) {
            throw new SystemErrorException("System not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
    }
}

//    /**
//     *
//     * @return GroupDAOImpl    Creates a group and returns the dao.
//     * @throws InvalidRequestException
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     */
//    public GroupPayload createGroup(LocaleId localeId, String groupTypeId, String displayName, String description, String descriptor, String validationMethod) throws InvalidRequestException, SystemErrorException {
//
//        if (isEmptyString(displayName)) {
//            throw new InvalidRequestException("missing display name", ErrorCodes.MISSING_USERNAME);
//        }
//
//        if (isEmptyString(groupTypeId)) {
//            throw new InvalidRequestException("missing group type id", ErrorCodes.MISSING_GROUP_TYPE_ID);
//        }
//
//        if (GroupDAOConstants.GroupDescriptor.findDescriptor(descriptor) == null) {
//            throw new InvalidRequestException("missing/invalid group descriptor", ErrorCodes.MISSING_GROUP_DESCRIPTOR);
//        }
//
//        final GroupTypeDAO groupTypeDAO = getStoreManager().createGroupType(groupTypeId);
//        if (!groupTypeDAO._exists()) {
//            throw new InvalidRequestException("no group type exists with groupTypeId=", groupTypeId, ErrorCodes.NOT_FOUND_GROUP_TYPE_ID);
//        }
//
//        if (isEmptyString(validationMethod)) {
//            throw new InvalidRequestException("Missing user validation method field " + GroupDAO.USER_VALIDATION_METHOD);
//        }
//        final UserValidationMethod vmeth = UserValidationMethod.getValidationMethod(validationMethod);
//        if (vmeth == null) {
//            throw new InvalidRequestException("invalid validation method " + GroupDAO.USER_VALIDATION_METHOD + "=" + validationMethod + ". Must be one of " + UserValidationMethod.validationMethodValues.keySet());
//        }
//
//        try {
//            vmeth.checkParameters(group.getValidationParameters());
//        } catch (InvalidUserValidationMethodParameters e) {
//            throw new InvalidRequestException("invalid validation parameters for group", group, ErrorCodes.INVALID_USER_VALIDATION_PARAMS);
//        }
//
//        final GroupDAO dao = getStoreManager().createGroup();
//        dao.setDisplayName(CommonUtilities.scrapeMarkup(displayName));
//        dao.setGroupTypeId(groupTypeId);
//        if (dao._exists()) {
//            throw new InvalidRequestException("a group with this name already exists in the given display name and group type", dao, ErrorCodes.ALREADY_EXISTS_GROUP_WITH_DISPLAY_NAME);
//        }
//        dao.setGroupTypeId(groupTypeId);
//        dao.setDisplayName(CommonUtilities.scrapeMarkup(displayName));
//        if (description != null && description.length() != 0) {
//            dao.setDescription(CommonUtilities.scrapeMarkup(description));
//        }
//        dao.setDescriptor(descriptor);
//        dao.setValidationMethod(validationMethod);
//        dao.setState((vmeth instanceof DefaultUserValidationMethod) ? AuthorizedState.A.toString() : AuthorizedState.getDefaultState());
//        dao._insert();
//
//        // Bump group type count
//        final GroupTypeDAO groupType = getStoreManager().createGroupType(groupTypeId);
//        groupType.setGroupCount(1);
//        groupType._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
//
////        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_GROUP);
////        tracker.setGroupId(dao.getId());
////        tracker.setGroupTypeId(groupTypeId);
////        TrackingManager.getInstance().track(LocaleId.en_us, tracker);
//
//        final GroupPayload groupPayload = new GroupPayload(dao);
//
//        // Updates local cache
//        registerGroup(groupPayload.getId(), groupPayload);
//
//        return groupPayload;
//    }

//    /**
//     * Used to update a group's display name, description, or state.
//     *
//     * @param localeId
//     * @param groupId     The group id
//     * @param displayName The display name
//     * @param description The group description
//     * @param state       The new group state
//     * @throws InvalidRequestException
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     * @throws ResourceNotFoundException
//     * @throws StateConflictException
//     */
//    public void updateGroup(LocaleId localeId, String groupId, String displayName, String description, String state) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException, StateConflictException {
//        if (groupId == null) {
//            throw new InvalidRequestException("missing group id", ErrorCodes.MISSING_GROUP_ID);
//        }
//        if (state != null && AuthorizedState.find(state) == null) { // TODO should check for valid state transition
//            throw new InvalidRequestException("requested an invalid group state", ErrorCodes.INVALID_STATE_CODE);
//        }
//
//        final GroupDAO groupDAO = (GroupDAO) getStoreManager().createGroup(groupId);
//        if (!groupDAO._exists()) {
//            throw new ResourceNotFoundException("no group exists with requested id", ErrorCodes.NOT_FOUND_GROUP_ID);
//        }
//
//        final GroupDAO updateDAO = getStoreManager().createGroup(groupId);
//        if (displayName != null && displayName.length() != 0) {
//            updateDAO.setDisplayName(CommonUtilities.scrapeMarkup(displayName));
//        }
//        if (description != null && description.length() != 0) {
//            updateDAO.setDescription(CommonUtilities.scrapeMarkup(description));
//        }
//        if (state != null) {
//            updateDAO.setState(state);
//        }
//
//        updateDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
//
//        if (state != null) {
//            if (state.equals(AuthorizedState.A.toString())) {
//                registerGroup(groupId, new GroupPayload(groupDAO));
//            } else {
//                unregisterGroup(groupId);
//            }
//        }
//    }

//    /**
//     * Creates a group type.
//     *
//     * @param displayName The display name for this group. Note that this is not localized.
//     * @return GroupTypeDAOImpl    The created group type dao
//     * @throws InvalidRequestException
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     * @throws StateConflictException
//     */
//    public GroupTypePayload createGroupType(LocaleId localeId, String displayName) throws InvalidRequestException, SystemErrorException, StateConflictException {
//        if (isEmptyString(displayName)) {
//            throw new InvalidRequestException("missing display name", ErrorCodes.MISSING_DISPLAY_NAME);
//        }
//        displayName = CommonUtilities.scrapeMarkup(displayName);
//
//        final GroupTypeDAO dao = getStoreManager().createGroupType();
//        dao.setDisplayName(displayName);
//        if (dao._exists()) {
//            throw new StateConflictException("group type with displayName already exists", displayName, ErrorCodes.ALREADY_EXISTS_GROUP_TYPE_WITH_DISPLAY_NAME);
//        }
////        dao.initToDefaultValues(localeId);
//        dao.setDisplayName(displayName);
//        dao._insert();
//
////        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_GROUP_TYPE);
////        tracker.setGroupId(dao.getId());
////        TrackingManager.getInstance().track(LocaleId.en_us, tracker);
//
//        return new GroupTypePayload(dao);
//    }

//    /**
//     * Updates the group type.
//     * TODO: This should be a protected API: only PMs should be able to require a new group type. Can have drastic performance implications.
//     * <p/>
//     * Transaction cost:
//     * 1. check that group type exists
//     * 2. update group type
//     *
//     * @param localeId
//     * @param groupTypeId The group type id
//     * @param displayName The new display name
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     * @throws InvalidRequestException
//     */
//    public void updateGroupTypeDisplayName(LocaleId localeId, String groupTypeId, String displayName) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException {
//        if (CommonUtilities.isEmptyString(displayName)) {
//            throw new InvalidRequestException("group type display name must have more than one character", ErrorCodes.MISSING_DISPLAY_NAME);
//        }
//        if (groupTypeId == null) {
//            throw new InvalidRequestException("groupTypeId is required", ErrorCodes.MISSING_GROUP_TYPE_ID);
//        }
//        final GroupTypeDAO dao = getStoreManager().createGroupType(groupTypeId);
//        if (!dao._exists()) {
//            throw new ResourceNotFoundException("no group type exists with groupTypeId ", groupTypeId, ErrorCodes.NOT_FOUND_GROUP_TYPE_ID);
//        }
//
//        dao.setDisplayName(CommonUtilities.scrapeMarkup(displayName));
//        dao._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
//    }



