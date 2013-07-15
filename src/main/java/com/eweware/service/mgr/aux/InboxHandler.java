package main.java.com.eweware.service.mgr.aux;

import com.mongodb.*;
import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BaseDAOConstants;
import main.java.com.eweware.service.base.store.dao.BlahDAO;
import main.java.com.eweware.service.base.store.dao.GroupDAO;
import main.java.com.eweware.service.base.store.dao.InboxBlahDAOConstants;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.mgr.GroupManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Fetches and generates inbox items.</p>
 * <p>When a blah is created, it is inserted into the "recents" inbox
 * for the blah's group. The background analytics task will eventually
 * compute a strength for the blah and place it in one of the group's inboxes.</p>
 * <p>The "recents" inbox for each group is a capped (circular) collection
 * that keeps the most recently created blahs in the group, in creation order.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 7/9/12 Time: 1:45 PM
 */
public class InboxHandler extends Thread {

    private static final Logger logger = Logger.getLogger(InboxHandler.class.getName());

    private static final int MAX_NUMBER_INBOX_ITEMS_IN_RECENTS_INBOX = 1000;
    private static final int INBOX_ITEM_SIZE_IN_BYTES = 2048;
    private static final BasicDBObject REVERSED_NATURAL_SORT_ORDER = new BasicDBObject("$natural", -1);

    private MongoStoreManager _storeManager;
    /**
     * Constructor initializes the store manager and a random seed.
     * @throws SystemErrorException
     */
    public InboxHandler() throws SystemErrorException {
        _storeManager = MongoStoreManager.getInstance();
    }

    /**
     * <p>Spreads blah to the group's recents inbox.</p>
     *
     * @param localeId
     * @param blahDAO
     * @param groupId
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    public void spreadBlahToRecents(LocaleId localeId, BlahDAO blahDAO, String groupId) throws SystemErrorException {

        // TODO need a static method in baselib for the inbox item creation so that it's shared with stats app, see stats InboxBuilder.buildInboxes()

        final DBObject dao = new BasicDBObject(InboxBlahDAOConstants.BLAH_ID, blahDAO.getId());
        dao.put(BaseDAOConstants.CREATED, blahDAO.getCreated());
//        dao.put(InboxBlahDAOConstants.BLAH_TEXT, blahDAO.getText());
        dao.put(InboxBlahDAOConstants.TYPE, blahDAO.getTypeId());
//        dao.put(InboxBlahDAOConstants.GROUP_ID, groupId);
//        dao.put(InboxBlahDAOConstants.AUTHOR_ID, blahDAO.getAuthorId());

//        Long tmp = blahDAO.getPromotedCount();
//        if (tmp != null) {
//            dao.put(InboxBlahDAOConstants.UP_VOTES, tmp);
//        }
//        tmp = blahDAO.getDemotedCount();
//        if (tmp != null) {
//            dao.put(InboxBlahDAOConstants.DOWN_VOTES, tmp);
//        }
//        tmp = blahDAO.getOpens();
//        if (tmp != null) {
//            dao.put(InboxBlahDAOConstants.OPENS, tmp);
//        }
//        tmp = blahDAO.getViews();
//        if (tmp != null) {
//            dao.put(InboxBlahDAOConstants.VIEWS, tmp);
//        }
        List<String> tmpList = blahDAO.getImageIds();
        if (tmpList != null && tmpList.size() > 0) {
            dao.put(InboxBlahDAOConstants.IMAGE_IDS, tmpList);
        }
        tmpList = blahDAO.getBadgeIds();
        if (tmpList != null && tmpList.size() > 0) {
            dao.put(InboxBlahDAOConstants.IMAGE_IDS, "b");
        }
//        final String nickname = CommonUtilities.maybeGetUserNickname(_storeManager, false, blahDAO.getAuthorId());
//        if (nickname != null) {
//            dao.put(InboxBlahDAOConstants.AUTHOR_NICKNAME, nickname);
//        }

        dao.put(InboxBlahDAOConstants.BLAH_STRENGTH, 0.99D);

        // Insert into db after state has been successfully updated
//        final GroupPayload group = GroupManager.getInstance().getCachedGroup(groupId);
//        final String inboxName = group.randomInboxCollectionName();
//        final DBCollection inboxCollection = _storeManager.getBlahDb().getCollection(inboxName);


        final String inboxCollectionName = CommonUtilities.makeRecentsInboxCollectionName(groupId);
        final DBCollection inboxCollection = getRecentsInboxCollection(_storeManager.getInboxDB(), inboxCollectionName);

        inboxCollection.insert(dao); // This is a capped and circular collection
    }

    private DBCollection getRecentsInboxCollection(DB inboxDB, String inboxCollectionName) {
        if (inboxDB.collectionExists(inboxCollectionName)) {   // TODO: could be cached
             return inboxDB.getCollection(inboxCollectionName);
        }
        // The next piece of code should never be reached as the capped collection are created at DB initialization
        final BasicDBObject options = new BasicDBObject("capped", true);
        options.put("max", MAX_NUMBER_INBOX_ITEMS_IN_RECENTS_INBOX);
        options.put("size", MAX_NUMBER_INBOX_ITEMS_IN_RECENTS_INBOX * INBOX_ITEM_SIZE_IN_BYTES);
        try {
            return inboxDB.createCollection(inboxCollectionName, options);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ignored race condition creating recents inbox collection. Delegate this to DB initialization sequence?", e);
            return inboxDB.getCollection(inboxCollectionName);
        }
    }

    /**
     * <p>Returns the data in an inbox for the specified group.</p>
     * <p>The client may request a specific inbox number, or the client may hint with the
     * last fetched inbox number to request the next one. In either case, if the
     * inbox does not exist, the first inbox in the group, if any, will be returned.</p>
     *
     * @param groupId The group id
     * @param inboxNumber   If specified, lastInboxNumber is ignored and a best attempt
     *                      is made to fetch the specified inbox number. If there is no
     *                      such inbox, the first inbox in the group will be returned.
     * @param lastInboxNumber   If specified and inboxNumber is not specified, the next
     *                          inbox after the lastInboxNumber will be fetched.
     * @param limit
     * @return  An inbox or null if there is no inbox matching the criteria.
     */
    public InboxData getNextInbox(String groupId, Integer inboxNumber, Integer lastInboxNumber, Integer limit) throws SystemErrorException {
        final GroupDAO group = GroupManager.getInstance().getCachedGroup(groupId);
        if (group != null) {
            Integer first = group.getFirstInboxNumber();
            if (first == null) {
                first = 0;
            }
            Integer last = group.getLastInboxNumber();
            if (last == null) {
                last = 0;
            }
            Integer nextBoxNumber = null;
            if (inboxNumber != null) {
                if (inboxNumber >= first && inboxNumber <= last) {
                    nextBoxNumber = inboxNumber;
                } else {
                    nextBoxNumber = first;
                }
            } else if (lastInboxNumber != null) {
                if (lastInboxNumber >= first && lastInboxNumber < last) {
                    nextBoxNumber = lastInboxNumber + 1;
                } else {
                    nextBoxNumber = first; // wrap around
                }
            } else {
                nextBoxNumber = first;
            }
            final List<Map<String, Object>> inboxItems = getInboxItems(CommonUtilities.makeInboxCollectionName(groupId, nextBoxNumber), false, limit);

            // TODO: remove experiment: adds some recents to the top
            final int maxRecentsToAdd = 10;
            final List<Map<String, Object>> recentInboxItems = getInboxItems(CommonUtilities.makeRecentsInboxCollectionName(groupId), true, maxRecentsToAdd);
            if (recentInboxItems.size() > 0) {
                inboxItems.addAll(0, recentInboxItems);
            }

            if (inboxItems.size() == 0) {
                logger.warning("Empty inbox '" + CommonUtilities.makeInboxCollectionName(groupId, inboxNumber) + "'");
            }

            return new InboxData(nextBoxNumber, inboxItems);
        } else {
            throw new SystemErrorException("Invalid group id '" + groupId + "' (uncached)", ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    public InboxData getRecentsInbox(String groupId, Integer limit) {
        return new InboxData(getInboxItems(CommonUtilities.makeRecentsInboxCollectionName(groupId), true, limit));
    }

    /**
     * <p>Returns a list of inbox items in their natural order.</p>
     *
     *
     * @param collectionName   The name of the inbox collection
     * @param recents
     * @param limit
     * @return  Returns the inbox items or an empty list if there are no items.
     */
    private List<Map<String, Object>> getInboxItems(String collectionName, boolean recents, Integer limit) {
        final DBCollection col = _storeManager.getInboxDB().getCollection(collectionName);
        DBCursor cursor = recents ? col.find().sort(REVERSED_NATURAL_SORT_ORDER) : col.find();
        if (limit != null) {
            cursor = cursor.limit(limit);
        }
        final List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (DBObject dao : cursor) {
            dao.removeField(BaseDAOConstants.ID);
            items.add((Map<String, Object>) dao);
        }
        return items;
    }
}



//    private BlahCache blahCache;
//
//    private BlahCache getBlahCache() {
//        if (blahCache == null) {
//            blahCache = BlahCache.getInstance();
//        }
//        return blahCache;
//    }
////
////    /**
////     * Maps a group id to the maximum number of inboxes in the group
////     */
////    private final java.util.Map<String, Integer> groupIdToMaxInbox = new HashMap<String, Integer>();
//
//    private Random random; // thread safe

//
//    /**
//     * @param groupId The group id
//     * @return Integer  Returns the maximum inbox number (zero-origin) or 0 (the
//     *         minimum inbox number possible) if there are no known inboxes for this group.
//     */
//    public Integer getMaxInbox(String groupId) {
//        synchronized (groupIdToMaxInbox) {
//            final Integer maxInbox = groupIdToMaxInbox.get(groupId);
//            if (maxInbox == null) {
//                final Integer unknown = -1;
////                groupIdToMaxInbox.put(groupId, unknown);
//                return unknown;
//            }
//            return maxInbox;
//        }
//    }
//
//    /**
//     * Sets the maximum inbox number for the group.
//     *
//     * @param groupId The group id
//     * @param max     The maximum inbox number (zero-origin).
//     */
//    private void setMaxInbox(String groupId, Integer max) {
//        synchronized (groupIdToMaxInbox) {
//            groupIdToMaxInbox.put(groupId, max);
//        }
//    }
//
//    /**
//     * Returns the specified group's inbox.
//     *
//     * @param groupId       The id of the group for this inbox
//     * @param inbox         The inbox number
//     * @param type          Optionally, the blah's type (default: any blah type)
//     * @param start         Optionally the first index in the inbox
//     * @param count         Optionally the number of items to retrieve.
//     * @param sortFieldName Optionally the name of the field to sort against
//     * @param sortDirection
//     * @return Inbox    The inbox
//     * @throws SystemErrorException
//     */
//    public Inbox getInboxFromCache(String groupId, Integer inbox, String type, Integer start, Integer count, String sortFieldName, Integer sortDirection) throws SystemErrorException {
//        // TODO type parameter ignored!
//        final Inbox box = getBlahCache().getInbox(groupId, inbox, start, count, sortFieldName, sortDirection);
//        if (box != null) {
//            setMaxInbox(groupId, box.getTopInbox()); // update the max inbox for this group
//        }
//        return box;
//    }
//
//    /**
//     * Spreads the blah within the specified group.
//     * Places the blah in a random group input box.
//     * <p/>
//     * Blahs may be spread to other inboxes in this group
//     * and to other groups' inboxes via a separate mechanism (more offline).
//     *
//     * @param localeId
//     * @param blahDAO
//     * @param groupId
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     */
//    public void spreadBlahToRecents(LocaleId localeId, BlahDAO blahDAO, String groupId) throws SystemErrorException {
//
//        // TODO need a static method in baselib for the inbox blah creation so that it's shared with stats app, see stats InboxBuilder.buildInboxes()
//
//        InboxBlahDAO dao = _storeManager.createInboxBlah();
//        dao.initToDefaultValues(localeId);
//
//        dao.setBlahId(blahDAO.getId());
//        dao.setBlahText(blahDAO.getText());
//        dao.setType(blahDAO.getTypeId());
//        dao.setGroupId(groupId);
//        dao.setAuthorId(blahDAO.getAuthorId());
//        Long tmp = blahDAO.getPromotedCount();
//        if (tmp != null) {dao.setUpVotes(tmp);}
//        tmp = blahDAO.getDemotedCount();
//        if (tmp != null) {dao.setDownVotes(tmp);}
//        tmp = blahDAO.getOpens();
//        if (tmp != null) {dao.setOpens(tmp);}
//        tmp = blahDAO.getViews();
//        if (tmp != null) {dao.setViews(tmp);}
//        List<String> tmpList = blahDAO.getImageIds();
//        if (tmpList != null) {dao.setImageIds(tmpList);}
//        tmpList = blahDAO.getBadgeIds();
//        if (tmpList != null && tmpList.size() != 0) {dao.setBadgeIndicator("b");}
//        final String nickname = CommonUtilities.maybeGetUserNickname(_storeManager, false, blahDAO.getAuthorId());
//        if (nickname != null) {dao.setAuthorNickname(nickname);}
//
//        // TODO Speculative for now: put new ones in the 85 percentile
//        dao.setStrength(0.85);
//        dao.setRecentStrength(0.85);
//
//        final Integer maxInbox = getMaxInbox(groupId);
//        // maxinbox of -1 is unknown and 0 means no inboxes: in that case, start inbox 0
//        final Integer inbox = (maxInbox <= 0) ? 0 : random.nextInt(maxInbox + 1);
//
//        dao.setInboxNumber(inbox);
//
//        // Insert into db after state has been successfully updated
//        dao._insert();
//
//        // Update inbox state in db
//        updateInboxStateInDB(groupId, inbox, dao.getId());
//
//        // Insert into cache
//        getBlahCache().addInboxItem(dao.getId(), dao, inbox, groupId);
//    }
//
//    private void updateInboxStateInDB(String groupId, Integer inbox, String inboxItemId) throws SystemErrorException {
//        int retryCount = 0;
//        for (int i = 0; i < 4; i++) {
//            try {
//                updateInboxStateInDBTry(groupId, inbox, inboxItemId);
//                return;
//            } catch (SystemErrorException e) {
//                if (i > 2) {
//                    throw new SystemErrorException("Inbox #" + inbox + " for group id '" + groupId + "': failed to  update inbox state after retrying " + retryCount + " times", e, ErrorCodes.SERVER_DB_ERROR);
//                }
//                retryCount++;
//            } finally {
//                if (retryCount > 0) {
//                    logger.warning("Inbox #" + inbox + " for group id '" + groupId + "': Retried updating inbox state " + retryCount + " times");
//                }
//            }
//        }
//    }
//
//    private void updateInboxStateInDBTry(String groupId, Integer inbox, String inboxItemId) throws SystemErrorException {
//        try {
//            final String stateId = getBlahCache().makeInboxStateKey(groupId, inbox);
//            final DBObject query = new BasicDBObject(BaseDAOConstants.ID, stateId);
//            final DBCollection stateCol = _storeManager.getCollection(_storeManager.getInboxStateCollectionName());
//            final DBObject state = stateCol.findOne(query);
//            if (state == null) {
//                final DBObject insert = new BasicDBObject(BaseDAOConstants.ID, stateId);
//                insert.put(InboxStateDAOConstants.INBOX_ITEM_IDS, Arrays.asList(new ObjectId[]{new ObjectId(inboxItemId)}));
//                insert.put(InboxStateDAOConstants.INBOX_NUMBER_TOP, inbox);
//                stateCol.insert(insert);
//            } else {
//                final DBObject push = new BasicDBObject(InboxStateDAOConstants.INBOX_ITEM_IDS, new ObjectId(inboxItemId));
//                final BasicDBObject update = new BasicDBObject("$push", push);
//                stateCol.update(new BasicDBObject(BaseDAOConstants.ID, stateId), update);
//            }
//        } catch (Exception e) {
//            throw new SystemErrorException("Inbox #" + inbox + " for group id '" + groupId + "': Failed to update the inbox state for inbox item id '" + inboxItemId + "'", e, ErrorCodes.SERVER_DB_ERROR);
//        }
//    }
