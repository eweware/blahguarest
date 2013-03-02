package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.payload.AuthorizedState;
import main.java.com.eweware.service.base.payload.GroupPayload;
import main.java.com.eweware.service.base.payload.GroupTypePayload;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.rest.session.BlahguaSession;
import main.java.com.eweware.service.user.validation.DefaultUserValidationMethod;
import main.java.com.eweware.service.user.validation.UserValidationMethod;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         TODO add monitors for mutating variables or lists with mutating elements
 *         TODO add transaction-level management (rollbacks)
 */
public final class GroupManager implements ManagerInterface {

    private static GroupManager singleton;

    private final Integer returnedObjectLimit;

    /**
     * Maps a group id to its descriptor.
     * Currently loads only open groups.
     * A synchronized hash.
     */
    private final ConcurrentHashMap<String, GroupPayload> groupMap = new ConcurrentHashMap<String, GroupPayload>();


    public static GroupManager getInstance() throws SystemErrorException {
        if (GroupManager.singleton == null) {
            throw new SystemErrorException("group manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return GroupManager.singleton;
    }

    private StoreManager storeManager;
    private ManagerState state = ManagerState.UNINITIALIZED;

    public GroupManager(Integer returnedObjectLimit) {
        this.returnedObjectLimit = returnedObjectLimit;
        GroupManager.singleton = this;
        state = ManagerState.INITIALIZED;
        System.out.println("*** GroupManager initialized ***");
    }

    public void start() {
        try {
            this.storeManager = MongoStoreManager.getInstance();
            initializeGroupCache();
            state = ManagerState.STARTED;
            System.out.println("*** GroupManager started ***");
        } catch (SystemErrorException e) {
            e.printStackTrace();
            throw new WebServiceException(e);
        }
    }

    /**
     * Locally caches some group information.
     */
    private void initializeGroupCache() throws SystemErrorException {
        final GroupDAO groupDAO = storeManager.createGroup();
        groupDAO.setState(AuthorizedState.A.toString());
        final String descriptor = GroupDAOConstants.GroupDescriptor.VISIBILITY_OPEN.getCode();
        groupDAO.setDescriptor(descriptor);
        final List<GroupDAO> groups = (List<GroupDAO>) groupDAO._findMany();
        for (GroupDAO group : groups) {
            final GroupPayload g = new GroupPayload(group);
            groupMap.putIfAbsent(g.getId(), g);
        }
    }

    public void shutdown() {
        this.state = ManagerState.SHUTDOWN;
        System.out.println("*** GroupManager shut down ***");
    }

    /**
     * Creates a group type.
     * TODO: This should be a protected API: only PMs should be able to require a new group type. Can have drastic performance implications.
     * @param displayName The display name for this group. Note that this is not localized.
     * @return GroupTypeDAOImpl    The created group type dao
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws StateConflictException
     */
    public GroupTypePayload createGroupType(LocaleId localeId, String displayName) throws InvalidRequestException, SystemErrorException, StateConflictException {
        ensureReady();
        if (isEmptyString(displayName)) {
            throw new InvalidRequestException("missing display name", ErrorCodes.MISSING_DISPLAY_NAME);
        }
        final GroupTypeDAO dao = storeManager.createGroupType();
        dao.setDisplayName(displayName);
        if (dao._exists()) {
            throw new StateConflictException("group type with displayName already exists", displayName, ErrorCodes.ALREADY_EXISTS_GROUP_TYPE_WITH_DISPLAY_NAME);
        }
        dao.initToDefaultValues(localeId);
        dao.setDisplayName(displayName);
        dao._insert();

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_GROUP_TYPE);
//        tracker.setGroupId(dao.getId());
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);

        return new GroupTypePayload(dao);
    }

    /**
     * Updates the group type.
     * TODO: This should be a protected API: only PMs should be able to require a new group type. Can have drastic performance implications.
     * <p/>
     * Transaction cost:
     * 1. check that group type exists
     * 2. update group type
     *
     * @param localeId
     * @param groupTypeId The group type id
     * @param displayName     The new display name
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     */
    public void updateGroupTypeDisplayName(LocaleId localeId, String groupTypeId, String displayName) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException {
        ensureReady();
        if (CommonUtilities.isEmptyString(displayName)) {
            throw new InvalidRequestException("group type display name must have more than one character", ErrorCodes.MISSING_DISPLAY_NAME);
        }
        if (groupTypeId == null) {
            throw new InvalidRequestException("groupTypeId is required", ErrorCodes.MISSING_GROUP_TYPE_ID);
        }
        final GroupTypeDAO dao = storeManager.createGroupType(groupTypeId);
        if (!dao._exists()) {
            throw new ResourceNotFoundException("no group type exists with groupTypeId ", groupTypeId, ErrorCodes.NOT_FOUND_GROUP_TYPE_ID);
        }
        dao.setDisplayName(displayName);
        dao._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
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

        final GroupTypeDAO groupTypeDAO = storeManager.createGroupType();
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
        final GroupTypeDAO dao = (GroupTypeDAO) storeManager.createGroupType(groupTypeId)._findByPrimaryId();
        if (dao == null) {
            throw new ResourceNotFoundException("not found: group type id=" + groupTypeId, ErrorCodes.NOT_FOUND_GROUP_TYPE_ID);
        }

        return new GroupTypePayload(dao);
    }

    /**
     * TODO: should this be a protected API or can any user do it without further ado? Might have performance implications.
     *
     * @return GroupDAOImpl    Creates a group and returns the dao.
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public GroupPayload createGroup(LocaleId localeId, GroupPayload group) throws InvalidRequestException, SystemErrorException {
        ensureReady();
        final String displayName = group.getDisplayName();
        if (isEmptyString(displayName)) { // TODO maybe force to a global group, but preferably not: client should provide reasonable options
            throw new InvalidRequestException("missing display name", group, ErrorCodes.MISSING_USERNAME);
        }
        final String groupTypeId = group.getGroupTypeId();
        if (isEmptyString(groupTypeId)) {
            throw new InvalidRequestException("missing group type id", group, ErrorCodes.MISSING_GROUP_TYPE_ID);
        }
        if (GroupDAOConstants.GroupDescriptor.findDescriptor(group.getDescriptor()) == null) {
            throw new InvalidRequestException("missing group descriptor", group, ErrorCodes.MISSING_GROUP_DESCRIPTOR);
        }
        final GroupTypeDAO groupTypeDAO = storeManager.createGroupType(groupTypeId);
        if (groupTypeDAO == null) {
            throw new InvalidRequestException("no group type exists with groupTypeId=", groupTypeId, ErrorCodes.NOT_FOUND_GROUP_TYPE_ID);
        }
        final String validationMethod = group.getValidationMethod();
        if (isEmptyString(validationMethod)) {
            throw new InvalidRequestException("Missing user validation method field " + GroupDAO.USER_VALIDATION_METHOD);
        }
        final UserValidationMethod vmeth = UserValidationMethod.getValidationMethod(validationMethod);
        if (vmeth == null) {
            throw new InvalidRequestException("invalid validation method " + GroupDAO.USER_VALIDATION_METHOD + "=" + validationMethod + ". Must be one of " + UserValidationMethod.validationMethodValues.keySet());
        }

        try {
            vmeth.checkParameters(group.getValidationParameters());
        } catch (InvalidUserValidationMethodParameters e) {
            throw new InvalidRequestException("invalid validation parameters for group", group, ErrorCodes.INVALID_USER_VALIDATION_PARAMS);
        }

        final GroupDAO dao = storeManager.createGroup();
        dao.setDisplayName(displayName);
        dao.setGroupTypeId(groupTypeId);
        if (dao._exists()) {
            throw new InvalidRequestException("a group with this name already exists in the given display name and group type", dao, ErrorCodes.ALREADY_EXISTS_GROUP_WITH_DISPLAY_NAME);
        }
        dao.addFromMap(group, true); // filters out injected material
        dao.setState((vmeth instanceof DefaultUserValidationMethod) ? AuthorizedState.A.toString() : AuthorizedState.getDefaultState());
        dao.initToDefaultValues(localeId);
        dao._insert();

        // Update group type count
        final GroupTypeDAO groupType = storeManager.createGroupType(groupTypeId);
        groupType.setGroupCount(1);
        groupType._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_GROUP);
//        tracker.setGroupId(dao.getId());
//        tracker.setGroupTypeId(groupTypeId);
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);

        final GroupPayload groupPayload = new GroupPayload(dao);

        // Updates local cache
        GroupManager.getInstance().registerGroup(groupPayload.getId(), groupPayload);

        return groupPayload;
    }

    /**
     * <p>Returns true if the group is open (e.g., open for
     * reading by anonymous users).</p>
     *
     * @param groupId The group id
     * @return  True if the group is open
     */
    public boolean isOpenGroup(String groupId) {
        if (groupId == null) {
            return false;
        }
        return groupMap.get(groupId) != null;
    }

    /**
     * Registers the group with the specified descriptor. Only open groups are actually registered.
     * @param groupId   The group's id (unchecked!)
     * @param payload    The group's payload (unchecked!)
     */
    public void registerGroup(String groupId, GroupPayload payload) {
        if (GroupDAOConstants.GroupDescriptor.VISIBILITY_OPEN.getCode().equals(payload.getDescriptor())) {
            if (groupMap.containsKey(groupId)) {
                groupMap.replace(groupId, payload);
            } else {
                groupMap.putIfAbsent(groupId, payload);
            }
        } else { /* ignore others for now */ }
    }

    public void unregisterGroup(String groupId) {
        groupMap.remove(groupId);
    }

    /**
     * TODO users should not be able to approve a group: need to discuss safe method for doing this
     * Used to update a group's display name and state.
     *
     * @param localeId
     * @param groupId  The group id
     * @param updates  A group DAO with the updates.
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public void updateGroup(LocaleId localeId, String groupId, GroupPayload updates) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException, StateConflictException {
        ensureReady();
        if (groupId == null) {
            throw new InvalidRequestException("missing group id", updates, ErrorCodes.MISSING_GROUP_ID);
        }
        final AuthorizedState state = AuthorizedState.find(updates.getState());
        if (state == null) {
            throw new InvalidRequestException("requested an invalid group state", updates, ErrorCodes.INVALID_STATE_CODE);
        }

        final GroupDAO found = (GroupDAO) storeManager.createGroup(groupId)._findByPrimaryId();
        if (found == null) {
            throw new ResourceNotFoundException("no group exists with requested id", updates, ErrorCodes.NOT_FOUND_GROUP_ID);
        }

        if (updates.getDescriptor() != null) {
            if (GroupDAOConstants.GroupDescriptor.findDescriptor(updates.getDescriptor()) == null) {
                throw new InvalidRequestException("Invalid group descriptor", updates, ErrorCodes.INVALID_GROUP_DESCRIPTOR);
            }
        }

        final GroupDAO updateDAO = storeManager.createGroup(groupId);

        updateDAO.addFromMap(updates, true);
        updateDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        if (state == AuthorizedState.A) {
            registerGroup(groupId, new GroupPayload(found));
        } else {
            unregisterGroup(groupId);
        }
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
        final GroupDAO groupDAO = storeManager.createGroup();
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
     *
     *
     * @param localeId The locale id
     * @param start    Start index into a page
     * @param count    Item count per page
     * @return All active anonymous groups in the system
     * @throws SystemErrorException
     */
    public Collection<GroupPayload> getOpenGroups(LocaleId localeId, Integer start, Integer count) throws SystemErrorException {
//        if (count == null) {
//            count = returnedObjectLimit;
//        }
        return groupMap.values();

//        final GroupDAO groupDAO = storeManager.createGroup();
//        groupDAO.setDescriptor(GroupDAOConstants.GroupDescriptor.VISIBILITY_OPEN.getCode());
//        final List<? extends BaseDAO> groups = groupDAO._findMany(start, count, null);
//        if (groups.isEmpty()) {
//            return new ArrayList<GroupPayload>(0);
//        }
//        final List<GroupPayload> result = new ArrayList<GroupPayload>(groups.size());
//        for (BaseDAO group : groups) {
//            final GroupDAO g = (GroupDAO) group;
//            if (g.getState().equals(AuthorizedState.A.toString())) {
//                result.add(new GroupPayload(g));
//            }
//        }
//        return result;
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
        final BaseDAO dao = storeManager.createGroup(groupId)._findByPrimaryId();
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
    public void updateViewerCount(String groupId, Boolean added) throws SystemErrorException, ResourceNotFoundException {
        final GroupDAO group = storeManager.createGroup(groupId);
        if (!group._exists()) {
            throw new ResourceNotFoundException("No such group id '" + groupId + "'", ErrorCodes.NOT_FOUND_GROUP_ID);
        }
        group.setCurrentViewerCount(added ? 1 : -1);
        group._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
    }

    /**
     * Returns the number of current viewers in this channel
     *
     * @return A group payload with the current viewer count
     */
    public GroupPayload getViewerCount(String groupId) throws SystemErrorException {
        final GroupDAO groupDAO = (GroupDAO) storeManager.createGroup(groupId)._findByPrimaryId(GroupDAO.CURRENT_VIEWER_COUNT);
        final GroupPayload groupPayload = new GroupPayload(groupId);
        if (groupDAO == null || groupDAO.getCurrentViewerCount() == null) {
            groupPayload.setCurrentViewerCount(0);
        }
        groupPayload.setCurrentViewerCount(groupDAO.getCurrentViewerCount());
        return groupPayload;
    }

    private boolean isEmptyString(String string) {
        return (string == null || string.length() == 0);
    }

//    /**
//     * TODO: Very expensive operation. It should be really be queued off for parallel processing.
//     *
//     * @throws StateConflictException
//     * @throws SystemErrorException
//     * @throws MongoException
//     */
//    private void maybeTransitionUsers(GroupDAO groupObj, AuthorizedState oldState,
//                                      AuthorizedState newState) throws StateConflictException, MongoException, SystemErrorException {
//        if (oldState == newState) {
//            return;
//        }
//        /* For any allowed transition, always change a group's state.
//           * If a user's only group is suspended or removed, reassign user to the Common group.
//           * * -> P: not allowed
//           * CREATED -> *: not allowed
//           * P -> S: not allowed
//           * P -> A: add approved users to this group & notify users that they are active in group
//           * P -> CREATED: remove records fromBlahDocument usergroups
//           * A -> S: remove approved users fromBlahDocument group & notify users that group has been suspended.
//           * A -> CREATED: remove all users fromBlahDocument group & remove group records in usergroups & notify users that group has been removed
//           * S -> A: add approved users to group &  notify users that group has been reactivated
//           * S -> CREATED: remove records fromBlahDocument usergroups
//           */
//        if ((newState == AuthorizedState.P) ||
//                ((oldState == AuthorizedState.P) && (newState == AuthorizedState.S))) {
//            throw new StateConflictException("state transition fromBlahDocument " + oldState + " to " + newState + " not allowed", groupObj);
//        } else if (oldState == AuthorizedState.P && newState == AuthorizedState.A) { // P -> A
//            String groupId = groupObj.getId();
//            String groupDisplayName = groupObj.getDisplayName();
//            DBCursor cursor = UserManager.getInstance().getUserToGroupsCollection().find(StoreUtils.makeBasicDBObjectWithId(groupId));
//            DBCollection usersCollection = UserManager.getInstance().getUsersCollection();
//            while (cursor.hasNext()) {
//                DBObject userGroupDB = cursor.next();
//                String userId = userGroupDB.get(BaseDAO.ID).toString();
//                AuthorizedState state = AuthorizedState.valueOf(userGroupDB.get(UserGroupDAO.STATE).toString());
//                if (state == AuthorizedState.A) {
//                    DBObject userObj = StoreUtils.findObjectWithId(usersCollection, userId);
//                    if (userObj == null) {
//                        // TODO remove this usergroup object (stale) or ignore and leave for some periodic cleanup job
//                    } else {
//                        notifyUsersGroupIsActive(groupDisplayName, new UserPayload(userObj).getDisplayName());
//                    }
//                }
//            }
//        } else {
//            throw new SystemErrorException("state transition for group not implemented: old state=" + oldState + " new state=" + newState, groupObj);
//        }
//    }

//    /**
//     * Send mail to users when a group has been (re)activated *
//     */
//    private void notifyUsersGroupIsActive(String groupDisplayName, String userDisplayName)
//            throws SystemErrorException {
//        StringBuilder b = new StringBuilder();
//        b.append(userDisplayName);
//        b.append(":\n\nThe user group '");
//        b.append(groupDisplayName);
//        b.append("' is now active.\n\nHave fun!\nBlahgua\n");
//        // MailManager..send(userDAO.getValidationEmailAddress(), "Joined Group "+groupDisplayName, b.toString());
//        // MailManager.getInstance().send(userDAO.getValidationEmailAddress(), "Joined Group "+groupDisplayName, b.toString());
//    }

    // Internal ------------------------------------------------------------------------------------------------

    public ManagerState getState() {
        return this.state;
    }

    private void ensureReady() throws SystemErrorException {
        if (getState() != ManagerState.STARTED) {
            throw new SystemErrorException("group mgr not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
    }
}
