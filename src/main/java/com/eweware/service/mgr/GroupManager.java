package com.eweware.service.mgr;

import com.eweware.service.base.CommonUtilities;
import com.eweware.service.base.error.*;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.mgr.ManagerInterface;
import com.eweware.service.base.mgr.ManagerState;
import com.eweware.service.base.payload.*;
import com.eweware.service.base.store.StoreManager;
import com.eweware.service.base.store.dao.*;
import com.eweware.service.base.store.dao.tracker.TrackerOperation;
import com.eweware.service.base.store.dao.type.DAOUpdateType;
import com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import com.eweware.service.base.CommonUtilities.*;
import com.eweware.service.rest.session.BlahguaSession;
import com.eweware.service.user.validation.DefaultUserValidationMethod;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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

    public static enum GroupAction {
        ACTION_JOIN,
        ACTION_COMMENT,
        ACTION_POST,
        ACTION_MODERATE
    };

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
        //groupDAO.setState(AuthorizedState.A.toString());
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

        final List<GroupDAO> results = (List<GroupDAO>)groupDAO._findMany(start, count, sortFieldName);

        final List<GroupPayload> groups = new ArrayList<GroupPayload>(results.size());
        for (GroupDAO dao : results) {
            if (dao.getDescriptor().compareTo("x") != 0)
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


    public Boolean PostNeedsModeration(String groupId, String userId) throws SystemErrorException {
        final GroupPayload groupPayload = new GroupPayload(groupId);
        final UserDAO userDAO = (UserDAO) getStoreManager().createUser(userId)._findByPrimaryId();

        Boolean moderated = groupPayload.getModerated();

        if ((moderated != null) && moderated) {
            // group is moderated
            List<String> adminList = groupPayload.getAdmin();

            if ((adminList != null) && (adminList.size() > 0) && (userDAO.getIsAdmin() == false)) {
                // the group has an admin list and the user is not a global admin.
                if (adminList.contains(userId))
                    return false; // admins can post anything
                else
                    return true;
            } else {
                // the group has no admin - still open
                return false;
            }
        }
        else {
            // group is not moderated
            return false;
        }

    }

    public Boolean CommentNeedsModeration(String groupId, String userId, String authorId) throws SystemErrorException {
        final GroupPayload groupPayload = new GroupPayload(groupId);
        final UserDAO userDAO = (UserDAO) getStoreManager().createUser(userId)._findByPrimaryId();

        Integer modStyle = groupPayload.getCommentModerationStyle();

        if ((userDAO.getIsAdmin() == true) || (modStyle == null) || (modStyle == GroupDAO.CommentModerationStyle.NO_MODERATION.getValue()))
            return false;
        else {  // group is moderated for comments - only an admin can post directly

            if ((modStyle == GroupDAO.CommentModerationStyle.AUTHOR_MODERATION.getValue()) && userId.equalsIgnoreCase(authorId))
                return false; // author is the blah author
            else {
                // see if user is an admin
                List<String> adminList = groupPayload.getAdmin();

                if ((adminList == null) || (adminList.size() > 0))
                    return false; // no admin set
                else
                    return !adminList.contains(userId); // non-admins need moderation
            }
        }
    }

    /** check if the user has a badge in the badgeList
     *
     * @param userId the id of the user to check
     * @param badgeList  the list of badge names to change
     * @return true if the user has a matching badge, false otherwise
     * @throws SystemErrorException
     */

    public Boolean userHasBadges(String userId, List<String> badgeList) throws SystemErrorException {
        final UserDAO userDAO = (UserDAO)getStoreManager().createUser(userId)._findByPrimaryId();
        List<String>    userBadgeList = userDAO.getBadgeIds();

        if (userBadgeList != null) {
            Date    curDate = new Date();
            for (String curBadgeId : userBadgeList) {
                final BadgeDAO curBadgeDAO = (BadgeDAO)getStoreManager().createBadge(curBadgeId)._findByPrimaryId();
                if ((curBadgeDAO != null) &&
                        (curBadgeDAO.getExpirationDate().after(curDate)) &&
                        (badgeList.contains(curBadgeDAO.getDisplayName())))
                    return true;
            }
        }


        return false;
    }

    public List<ChannelImportPayload>   getImportRecords(String userId, String groupId) throws SystemErrorException {
        final GroupDAO groupDAO = (GroupDAO)getStoreManager().createGroup(groupId)._findByPrimaryId();
        if (groupDAO == null)
            return null;

        Map<String, Boolean> perm = getGroupPermissionById(groupId, userId);

        if (!perm.get("admin"))
            return null;    // only admins can mess with import records

        // find if there are any records
        final ChannelImportDAO searchImport = getStoreManager().createChannelImport(groupId);
        final List<ChannelImportDAO> channelImportDAOs;

        channelImportDAOs = (List<ChannelImportDAO>) searchImport._findManyByCompositeId(null, null, null, null, ChannelImportDAO.TARGET_GROUP);

        final List<ChannelImportPayload> importers = new ArrayList<ChannelImportPayload>(channelImportDAOs.size());
        for (ChannelImportDAO dao : channelImportDAOs) {
            final ChannelImportPayload importPayload = new ChannelImportPayload(dao);
            importers.add(importPayload);
        }
        return importers;
    }


    public ChannelImportDAO addImportRecord(String userId, ChannelImportPayload importRecord) throws SystemErrorException {
        final String groupId = importRecord.getTargetGroup();
        final GroupDAO groupDAO = (GroupDAO)getStoreManager().createGroup(groupId)._findByPrimaryId();
        if (groupDAO == null)
            return null;

        Map<String, Boolean> perm = getGroupPermissionById(groupId, userId);

        if (!perm.get("admin"))
            return null;    // only admins can mess with import records

        final ChannelImportDAO importDAO = getStoreManager().createChannelImport(groupId);
        importDAO.addFromMap(importRecord, true);

        importDAO._insert();

        return importDAO;

    }

    public Boolean deleteImportRecord(String userId, String importRecordId) throws SystemErrorException {
        final ChannelImportDAO importDAO = (ChannelImportDAO)getStoreManager().createChannelImport(importRecordId)._findByPrimaryId();
        if (importDAO == null)
            return false;

        final String groupId = importDAO.getTargetGroup();
        final GroupDAO groupDAO = (GroupDAO)getStoreManager().createGroup(groupId)._findByPrimaryId();
        if (groupDAO == null)
            return false;

        Map<String, Boolean> perm = getGroupPermissionById(groupId, userId);

        if (!perm.get("admin"))
            return false;    // only admins can mess with import records

        // ok, we are good, actually delete it
        importDAO._deleteByPrimaryId();

        return true;

    }

    public ChannelImportDAO updateImportRecord(String userId, ChannelImportPayload importPayload) throws SystemErrorException {
        final String importRecordId = importPayload.getId();
        final ChannelImportDAO searchDAO = getStoreManager().createChannelImport("");
        searchDAO.setId(importRecordId);
        final ChannelImportDAO importDAO = (ChannelImportDAO)searchDAO._findByPrimaryId();

        if (importDAO == null)
            return null;

        final String groupId = importDAO.getTargetGroup();
        final GroupDAO groupDAO = (GroupDAO)getStoreManager().createGroup(groupId)._findByPrimaryId();
        if (groupDAO == null)
            return null;

        Map<String, Boolean> perm = getGroupPermissionById(groupId, userId);

        if (!perm.get("admin"))
            return null;    // only admins can mess with import records

        // ok, we are good, actually do the update;
        final Integer feedType = importPayload.getFeedType();

        if (feedType != null)
            importDAO.setFeedType(importPayload.getFeedType());

        // import the general fields
        Boolean autoImport = importPayload.getAutoImport();
        if (autoImport != null)
            importDAO.setAutoImport(autoImport);

        Integer importFrequency = importPayload.getImportFrequency();
        if (importFrequency != null)
            importDAO.setImportFrequency(importFrequency);

        Date lastImportDate = importPayload.getLastImportDate();
        if (lastImportDate != null)
            importDAO.setLastImportDate(lastImportDate);

        String importUsername = importPayload.getImportUsername();
        if (importUsername != null)
            importDAO.setImportUsername(importUsername);

        String importPassword = importPayload.getImportPassword();
        if (importPassword != null)
            importDAO.setImportPassword(importPassword);

        Boolean importAsUser = importPayload.getImportAsUser();
        if (importAsUser != null)
            importDAO.setImportAsUser(importAsUser);


        if (importDAO.getFeedType() == ChannelImportDAOConstants.ImportFeedType.RSS_FEED.getValue()) {
            // import the RSS feed fields
            String rssurl = importPayload.getRSSURL();
            if (rssurl != null)
                importDAO.setRSSURL(rssurl);

            Boolean summarizeURLPage = importPayload.getSummarizeURLPage();
            if (summarizeURLPage != null)
                importDAO.setSummarizeURLPage(summarizeURLPage);

            String titleField = importPayload.getTitleField();
            if (titleField != null)
                importDAO.setTitleField(titleField);

            String imageField = importPayload.getImageField();
            if (imageField != null)
                importDAO.setImageField(imageField);

            String bodyField = importPayload.getBodyField();
            if (bodyField != null)
                importDAO.setBodyField(bodyField);

            String urlField = importPayload.getURLField();
            if (urlField != null)
                importDAO.setURLField(urlField);

            Boolean appendURL = importPayload.getAppendURL();
            if (appendURL != null)
                importDAO.setAppendURL(appendURL);

        }

        importDAO._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);

        return importDAO;
    }


    public Map<String, Boolean> getGroupPermissionById(String groupId, String userId) throws SystemErrorException {
        Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
        UserDAO userDAO = null;
        Boolean isAdmin = false;

        if (userId != null) {
            userDAO = (UserDAO)getStoreManager().createUser(userId)._findByPrimaryId();
            isAdmin = userDAO.getIsAdmin();
            if (isAdmin == null)
                isAdmin = false;
        }

        // defaults
        Boolean canJoin = true;
        Boolean canPost = true;
        Boolean canComment = true;
        Boolean canModerate = false;
        Boolean canAdmin = false;

        if (isAdmin) {
            // user is admin, they can do anything
            canJoin = true;
            canPost = true;
            canComment = true;
            canModerate = true;
            canAdmin = true;
        } else if (userId == null) {
            // signed out
            canJoin = false;
            canPost = false;
            canComment = false;
            canModerate = false;
            canAdmin = false;
        }
        else {
            // not an admin - need to check the group
            final GroupDAO groupDAO = (GroupDAO)getStoreManager().createGroup(groupId)._findByPrimaryId();

            List<String>    adminList = groupDAO.getAdmin();

            if ((adminList != null) && (adminList.size() > 0)) {
                // admined group
                if (adminList.contains(userId)) {
                    canJoin = true;
                    canPost = true;
                    canModerate = true;
                    canComment = true;
                    canAdmin = true;
                } else {
                    // check individual permissions
                    final List<String> joinList = groupDAO.getJoinBadgeList();
                    if ((joinList == null) || userHasBadges(userId, joinList))
                        canJoin = true;
                    else
                        canJoin = false;

                    final List<String> postList = groupDAO.getPostBadgeList();
                    if ((postList == null) || userHasBadges(userId, postList))
                        canPost = true;
                    else
                        canPost = false;

                    final List<String> commentList = groupDAO.getCommentBadgeList();
                    if ((commentList == null) || userHasBadges(userId, commentList))
                        canComment = true;
                    else
                        canComment = false;

                    Boolean isModerated = groupDAO.getModerated();
                    if (isModerated != null && isModerated) {
                        final List<String> moderateList = groupDAO.getModerateBadgeList();
                        if ((moderateList == null) || userHasBadges(userId, moderateList))
                            canModerate = true;
                        else
                            canModerate = false;
                    } else canModerate = false;

                }
            } else {
                canJoin = true;
                canPost = true;
                canComment = true;
                canModerate = false;
                canAdmin = false;
            }

        }

        resultMap.put("join", canJoin);
        resultMap.put("post", canPost);
        resultMap.put("comment", canComment);
        resultMap.put("moderate", canModerate);
        resultMap.put("admin", canAdmin);

        return resultMap;
    }

    /** Given a group and a user and an action, returns TRUE if the user is allowed to perform
     * the action in the group.  Permission is granted if the group is not an admin group, or if
     * the user is either a group or system admin, or if the user has the needed badge(s) for the
     * action
     *
     * @param groupId the ID of the group
     * @param userId  the ID of the user
     * @param action the Action to perform
     * @return true if the action is allowed, otherwise false
     * @throws SystemErrorException
     */
    public Boolean CheckPermissions(String groupId, String userId, GroupAction action) throws SystemErrorException {
        final GroupDAO groupPayload = (GroupDAO) getStoreManager().createGroup(groupId)._findByPrimaryId();
        final UserDAO userDAO = (UserDAO) getStoreManager().createUser(userId)._findByPrimaryId();

        List<String> adminList = groupPayload.getAdmin();

        if ((adminList != null) && (adminList.size() > 0) && (userDAO.getIsAdmin() == false)) {
            // this channel is administrated, check permissions

            if (adminList.contains(userId))
                return true; // admins can do anything
            else {
                // if the user is not an admin, we should check permission
                List<String> badgeList = null;

                switch (action) {
                    case ACTION_JOIN:
                        badgeList = groupPayload.getJoinBadgeList();
                        break;
                    case ACTION_COMMENT:
                        badgeList = groupPayload.getCommentBadgeList();
                        break;
                    case ACTION_MODERATE:
                        badgeList = groupPayload.getModerateBadgeList();
                        break;
                    case ACTION_POST:
                        badgeList = groupPayload.getPostBadgeList();
                        break;
                }

                if (badgeList == null)
                    return true;    // no restrictions, anyone can do it
                else if (badgeList.size() == 0)
                    return false;  // locked down - only admins allowed
                else {
                    // see if the user has the needed badges
                    Boolean hasBadge = false;
                    Date currentTime = new Date();
                    List<String> userBadges = userDAO.getBadgeIds();

                    for (String curBadge : badgeList) {
                        for (String curUserBadgeId : userBadges) {
                            BadgeDAO curUserBadge = (BadgeDAO)getStoreManager().createBadge(curUserBadgeId)._findByPrimaryId();
                            if ((curUserBadge != null) &&
                                    (curUserBadge.getExpirationDate().after(currentTime)) &&
                                    (curUserBadge.getBadgeType().equalsIgnoreCase(curBadge))) {
                                hasBadge = true;
                                break;
                            }
                        }

                        if(hasBadge)
                            break;
                    }

                    return hasBadge;
                }
            }
        } else {
            // user is authorized
            return true;
        }
    }


    /**
      *
      * @return GroupDAOImpl    Creates a group and returns the dao.
      * @throws InvalidRequestException
      * @throws SystemErrorException
      *
      */
    public GroupPayload createGroup(LocaleId localeId,  String userId, GroupPayload groupPayload ) throws InvalidRequestException, SystemErrorException {
      final String groupTypeId = groupPayload.getGroupTypeId();
      final String displayName = groupPayload.getDisplayName();
      final String description = groupPayload.getDescription();

      if (isEmptyString(displayName)) {
          throw new InvalidRequestException("missing display name", ErrorCodes.MISSING_USERNAME);
      }
      if (isEmptyString(groupTypeId)) {
          throw new InvalidRequestException("missing group type id", ErrorCodes.MISSING_GROUP_TYPE_ID);
      }

      final GroupTypeDAO groupTypeDAO = getStoreManager().createGroupType(groupTypeId);
      if (!groupTypeDAO._exists()) {
          throw new InvalidRequestException("no group type exists with groupTypeId=", groupTypeId, ErrorCodes.NOT_FOUND_GROUP_TYPE_ID);
      }


        List<String>    adminList = new ArrayList<String>();
        adminList.add(userId);
        List<String> otherAdmins = groupPayload.getAdmin();
        if ((otherAdmins != null) && (otherAdmins.size() > 0)) {
            for(String curAdminName : otherAdmins) {
                final UserDAO curUser = getStoreManager().createUser();
                curUser.setUsername(curAdminName);
                final UserDAO curUserDAO = (UserDAO)curUser._findByCompositeId(new String[] {UserDAO.ID}, UserDAO.USERNAME);
                if (curUserDAO == null)
                    throw new InvalidRequestException("admin user does not exist", ErrorCodes.INVALID_USER_ID);
                final String curUserId = curUserDAO.getId();
                if (!adminList.contains(curUserId))
                    adminList.add(curUserId);
            }
        }

        // 3.  Make sure that all of the badges are real and convert to...??
        final List<String>  joinBadgeIdList = VerifyBadgeList(groupPayload.getJoinBadgeList());
        final List<String>  postBadgeIdList = VerifyBadgeList(groupPayload.getPostBadgeList());
        final List<String>  commentBadgeIdList = VerifyBadgeList(groupPayload.getCommentBadgeList());
        final List<String>  moderateIdList = VerifyBadgeList(groupPayload.getModerateBadgeList());

      final GroupDAO dao = getStoreManager().createGroup();
      dao.setDisplayName(CommonUtilities.scrapeMarkup(displayName));
      dao.setGroupTypeId(groupTypeId);

      if (dao._exists()) {
          throw new InvalidRequestException("a group with this name already exists in the given display name and group type", dao, ErrorCodes.ALREADY_EXISTS_GROUP_WITH_DISPLAY_NAME);
      }
        dao.setRank(1000); // all user channels are low rank
        dao.setUserCount(0L);
        dao.setBlahCount(0L);
        final Boolean isPrivate = groupPayload.getIsPrivate();
        final Boolean isMature = groupPayload.getIsMature();
        final Boolean isModerated = groupPayload.getModerated();
        final String adId = groupPayload.getAdBlahID();
        final String headerImage = groupPayload.getHeaderImage();

        if (description != null && description.length() != 0)
          dao.setDescription(CommonUtilities.scrapeMarkup(description));

        if (isPrivate != null && isPrivate)
            dao.setIsPrivate(true);
        if (isMature != null && isMature)
            dao.setIsMature(true);
        dao.setAdmin(adminList);
        if (joinBadgeIdList != null)
            dao.setJoinBadgeList(joinBadgeIdList);
        if (postBadgeIdList != null)
            dao.setPostBadgeList(postBadgeIdList);
        if (commentBadgeIdList != null)
            dao.setCommentBadgeList(commentBadgeIdList);
        if (isModerated != null && isModerated) {
            dao.setModerated(true);
            if (moderateIdList != null)
                dao.setModerateBadgeList(moderateIdList);
            Integer commentStyle = groupPayload.getCommentModerationStyle();
            if (commentStyle == null)
                commentStyle = 0;
            dao.setCommentModerationStyle(commentStyle);
        }

        if (adId != null && (!adId.isEmpty())) {
            // todo:  make sure blah exists
            dao.setAdBlahID(adId);
        }

        if (headerImage != null && (!headerImage.isEmpty()))
            dao.setHeaderImage(headerImage);


      dao._insert();
      // Bump group type count
      final GroupTypeDAO groupType = getStoreManager().createGroupType(groupTypeId);
      groupType.setGroupCount(1L);
      groupType._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
      final GroupPayload returnPayload = new GroupPayload(dao);
      // Updates local cache
      maybeRefreshGroupCache(true);
      return returnPayload;
  }

    private List<String>    VerifyBadgeList(List<String> badgeNameList) {
        if (badgeNameList == null) {
            return null;
        }
        List<String>    badgeIdList = new ArrayList<String>();

        for (String badgeName : badgeNameList) {
            // todo:  verify that the badge exists
            badgeIdList.add(badgeName.trim());
        }

        return badgeIdList;
    }

  /**
   * Used to update a group's display name, description, or state.
   *
   * @param localeId
   * @param groupId     The group id
   * @param userId The id of the current user
   * @throws InvalidRequestException
   *
   * @throws ResourceNotFoundException
   * @throws StateConflictException
   */

  public boolean deleteGroup(LocaleId localeId, String groupId, String userId) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException, StateConflictException {
      if (groupId == null) {
          throw new InvalidRequestException("missing group id", ErrorCodes.MISSING_GROUP_ID);
      }

      if (userId == null) {
          throw new InvalidRequestException("missing user id", ErrorCodes.MISSING_USER_ID);
      }

      final GroupDAO groupDAO = (GroupDAO) getStoreManager().createGroup(groupId)._findByPrimaryId();
      final UserDAO userDAO = (UserDAO) getStoreManager().createUser(userId)._findByPrimaryId();
      if (!groupDAO._exists()) {
          throw new ResourceNotFoundException("no group exists with requested id", ErrorCodes.NOT_FOUND_GROUP_ID);
      }
      List<String> adminList = groupDAO.getAdmin();

      if (!userDAO.getIsAdmin() || (adminList == null) || (adminList.size() == 0) || (!adminList.contains(userId)))
          return false; // this user cannot delete this group

      // user is either a system admin or a group admin
      groupDAO.setDescriptor("x");
      groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
      doRefreshGroupCache();

      // delete from all user's current lists
      MongoStoreManager mongo = (MongoStoreManager)getStoreManager();
      DBCollection userGroupCollection = mongo.getCollection(mongo.getUserGroupCollectionName());
      DBObject newObj = new BasicDBObject().append("G", groupId);
      userGroupCollection.remove(newObj);
      return true;

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



