package com.eweware.service.mgr.auxiliary;

import com.eweware.service.base.error.InvalidRequestException;
import com.eweware.service.base.error.ResourceNotFoundException;
import com.eweware.service.base.payload.GroupPayload;
import com.mongodb.*;
import com.eweware.service.base.CommonUtilities;
import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.BaseDAOConstants;
import com.eweware.service.base.store.dao.BlahDAO;
import com.eweware.service.base.store.dao.GroupDAO;
import com.eweware.service.base.store.dao.InboxBlahDAOConstants;
import com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import com.eweware.service.mgr.GroupManager;

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
     * @throws com.eweware.service.base.error.SystemErrorException
     */
    public void spreadBlahToRecents(LocaleId localeId, BlahDAO blahDAO, String groupId) throws SystemErrorException {

        // TODO need a static method in baselib for the inbox item creation so that it's shared with stats app, see stats InboxBuilder.buildInboxes()

        final DBObject dao = new BasicDBObject(InboxBlahDAOConstants.BLAH_ID, blahDAO.getId());
        dao.put(BaseDAOConstants.CREATED, blahDAO.getCreated());
        dao.put(InboxBlahDAOConstants.BLAH_TEXT, blahDAO.getText());
        dao.put(InboxBlahDAOConstants.TYPE, blahDAO.getTypeId());
//        dao.put(InboxBlahDAOConstants.GROUP_ID, groupId);
        dao.put(InboxBlahDAOConstants.AUTHOR_ID, blahDAO.getAuthorId());

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
            dao.put(InboxBlahDAOConstants.BADGE_INDICATOR, "b");
        }
//        final String nickname = CommonUtilities.maybeGetUserNickname(_storeManager, false, blahDAO.getAuthorId());
//        if (nickname != null) {
//            dao.put(InboxBlahDAOConstants.AUTHOR_NICKNAME, nickname);
//        }

        dao.put(InboxBlahDAOConstants.BLAH_STRENGTH, 0.99D);

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
    public InboxData getNextInbox(String groupId, Integer inboxNumber, Integer lastInboxNumber, Integer limit, Boolean safe) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException {
        final GroupDAO group = GroupManager.getInstance().getCachedGroup(groupId);
        if (group != null) {
            Integer first;
            if (safe)
                first = group.getFirstSafeInboxNumber();
            else
                first = group.getFirstInboxNumber();
            if (first == null) {
                first = 0;
            }
            Integer last;

            if (safe)
                last = group.getLastSafeInboxNumber();
            else
                last = group.getLastInboxNumber();
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
            String inboxName = CommonUtilities.makeInboxCollectionName(groupId, nextBoxNumber, safe);

            if (!_storeManager.getInboxDB().collectionExists(inboxName)) {
                logger.warning("Inbox '" + inboxName + "' does not exist.");
                final GroupPayload localGroup = GroupManager.getInstance().getGroupById(LocaleId.en_us, groupId);
                nextBoxNumber = localGroup.getFirstInboxNumber();
                inboxName = CommonUtilities.makeInboxCollectionName(groupId, nextBoxNumber, safe);

                if (!_storeManager.getInboxDB().collectionExists(inboxName)) {
                    logger.warning("First Inbox '" + inboxName + "' also does not exist - searching...");
                    for (int i = nextBoxNumber - 1; i >= 1; i--) {
                        inboxName = CommonUtilities.makeInboxCollectionName(groupId, i, safe);
                        if (_storeManager.getInboxDB().collectionExists(inboxName)) {
                            break;
                        }
                    }
                }
            }

            final List<Map<String, Object>> inboxItems = getInboxItems(inboxName, false, limit);


            if (inboxItems.size() == 0) {
                logger.warning("Empty inbox '" + CommonUtilities.makeInboxCollectionName(groupId, nextBoxNumber, safe) + "'");
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
        logger.warning("Fetching inbox:  '" + collectionName + "'");

        if (_storeManager.getInboxDB().collectionExists(collectionName)) {
            final List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            final DBCollection col = _storeManager.getInboxDB().getCollection(collectionName);
            DBCursor cursor = recents ? col.find().sort(REVERSED_NATURAL_SORT_ORDER) : col.find();
            if (limit != null) {
                cursor = cursor.limit(limit);
            }

            for (DBObject dao : cursor) {
                dao.removeField(BaseDAOConstants.ID);
                items.add((Map<String, Object>) dao);
            }
            return items;
        } else {
            logger.warning("Inbox '" + collectionName + "' does not exist.");
            return null;
        }

    }
}

