package main.java.com.eweware.service.base.store;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.store.dao.*;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 5:08 PM
 */
public interface StoreManager extends ManagerInterface {

    // FACTORY METHODS -------------------------------------------------------------------------------------

    /**
     * @return GroupTypeDAO An empty group type dao
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    public abstract GroupTypeDAO createGroupType() throws SystemErrorException;

    /**
     * @param id The group type id
     * @return GroupTypeDAO A group type dao populated with the id
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    public abstract GroupTypeDAO createGroupType(String id) throws SystemErrorException;
    public abstract GroupTypeDAO createGroupType(Map<String, Object> map) throws SystemErrorException;

    public abstract GroupDAO createGroup() throws SystemErrorException;
    /**
     * Creates a group with the specified id.
     *
     * @param id The group's id
     * @return GroupDAO A dao with the specified group id.
     */
    public abstract GroupDAO createGroup(String id) throws SystemErrorException;
    public abstract GroupDAO createGroup(Map<String, Object> map) throws SystemErrorException;

    public abstract UserDAO createUser();
    public abstract UserDAO createUser(String userId) throws SystemErrorException;
    public abstract UserDAO createUser(Map<String, Object> map) throws SystemErrorException;

    public abstract UserAccountDAO createUserAccount() throws SystemErrorException;
    public abstract UserAccountDAO createUserAccount(String id) throws SystemErrorException;

    public abstract UserProfileDAO createUserProfile();
    public abstract UserProfileDAO createUserProfile(String profileId) throws SystemErrorException;
    public abstract UserProfileDAO createUserProfile(Map<String, Object> map) throws SystemErrorException;

    public abstract UserGroupDAO createUserGroup();
    public abstract UserGroupDAO createUserGroup(String groupId) throws SystemErrorException;
    public abstract UserGroupDAO createUserGroup(String userId, String groupId) throws SystemErrorException;
    public abstract UserGroupDAO createUserGroup(Map<String, Object> map) throws SystemErrorException;

    public abstract BadgeAuthorityDAO createBadgeAuthority() throws SystemErrorException;
    public abstract BadgeAuthorityDAO createBadgeAuthority(String authorityId) throws SystemErrorException;

    public abstract BadgeDAO createBadge() throws SystemErrorException;
    public abstract BadgeDAO createBadge(String badgeId) throws SystemErrorException;

    public abstract MediaDAO createMedia();
    public abstract MediaDAO createMedia(String mediaId) throws SystemErrorException;

    public abstract BlahDAO createBlah();
    public abstract BlahDAO createBlah(String blahId) throws SystemErrorException;

    public abstract BlahDAO createBlah(Map<String, Object> map) throws SystemErrorException;
    public abstract PollOptionTextDAO createPollOption();

    public abstract PollOptionTextDAO createPollOption(String tagLine, String text);
    public abstract UserBlahInfoDAO createUserBlahInfo();
    public abstract UserBlahInfoDAO createUserBlahInfo(String userId, String blahId);

    public abstract UserBlahInfoDAO createUserBlahInfo(Map<String, Object> map) throws SystemErrorException;

    public abstract BlahTypeDAO createBlahType();
    public abstract UserCommentInfoDAO createUserCommentInfo(String userId, String commentId);

    public abstract UserCommentInfoDAO createUserCommentInfo(Map<String, Object> map) throws SystemErrorException;
    public abstract CommentDAO createComment();
    public abstract CommentDAO createComment(String commentId) throws SystemErrorException;

    public abstract CommentDAO createComment(Map<String, Object> map) throws SystemErrorException;
    public abstract UserTrackerDAO createUserTracker() throws SystemErrorException;
    public abstract UserTrackerDAO createUserTracker(String trackerId) throws SystemErrorException;

    public abstract UserTrackerDAO createUserTracker(Map<String, Object> map) throws SystemErrorException;

    public abstract InboxBlahDAO createInboxBlah();
    public abstract BlahTrackerDAO createBlahTracker();

    public abstract BlahTrackerDAO createBlahTracker(String trackerId) throws SystemErrorException;
    public abstract CommentTrackerDAO createCommentTracker();

    public abstract CommentTrackerDAO createCommentTracker(String trackerId) throws SystemErrorException;

    public abstract InboxStateDAO createInboxState();
}
