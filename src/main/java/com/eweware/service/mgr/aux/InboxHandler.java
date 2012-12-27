package main.java.com.eweware.service.mgr.aux;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import main.java.com.eweware.service.base.cache.BlahCache;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BaseDAOConstants;
import main.java.com.eweware.service.base.store.dao.BlahDAO;
import main.java.com.eweware.service.base.store.dao.InboxBlahDAO;
import main.java.com.eweware.service.base.store.dao.InboxStateDAOConstants;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.base.cache.Inbox;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/9/12 Time: 1:45 PM
 *         Distribute new blahs to separate inboxes.
 *         <p/>
 *         When a blah is created in a group, it is immediately placed in a random "input box"
 *         for that group. Each group has a set of inboxes of size N (currently 10).
 *         TODO N should be group-dependent and should depend on the number of users in the group.
 *         <p/>
 *         A separate, near-real-time service, constantly inspects inboxes
 *         and (1) "promotes" certain blahs in the inbox to other inboxes in the group
 *         and, in some cases, to inboxes in other groups, and (2) prunes blahs
 *         from the inboxes. This service ensures that each inbox has a good mix of blahs:
 *         Less active and older blahs may be pruned, the sort order of the inbox might
 *         be changed to reflect voting and other factors, blahs from other inboxes
 *         might be copied to other inboxes within and outside the group to increase
 *         the effectiveness of their spread, etc.
 *         <p/>
 *         When a user requests an inbox for a group, he is given the ith inbox in the group (0 <= i < N).
 *         Next time the user requests an inbox, he's given the (i+1)th inbox in that group, and so on,
 *         wrapping around to the 0th box when i >= N. This ensures that the user will eventually
 *         see all relevant blahs in the group.
 *         <p/>
 *         This handler inserts a new blah into a random inbox in a group
 *         and returns an inbox for a given group and inbox number.
 *         <p/>
 */
public class InboxHandler extends Thread {

    private MongoStoreManager storeManager;

    private final java.util.Map<String, Integer> groupIdToMaxInbox = new HashMap<String, Integer>();
    private Random random; // thread safe

    public InboxHandler() throws SystemErrorException {
        this.storeManager = MongoStoreManager.getInstance();
        this.random = new Random();
    }

    /**
     * @param groupId The group id
     * @return Integer  Returns the maximum inbox number (zero-origin) or 0 (the
     *         minimum inbox number possible) if there are no known inboxes for this group.
     */
    public Integer getMaxInbox(String groupId) {
        synchronized (groupIdToMaxInbox) {
            final Integer maxInbox = groupIdToMaxInbox.get(groupId);
            if (maxInbox == null) {
                final Integer minInbox = 0;
                groupIdToMaxInbox.put(groupId, minInbox);
                return minInbox;
            }
            return maxInbox;
        }
    }

    /**
     * Sets the maximum inbox number for the group.
     *
     * @param groupId The group id
     * @param max     The maximum inbox number (zero-origin).
     */
    private void setMaxInbox(String groupId, Integer max) {
        synchronized (groupIdToMaxInbox) {
            groupIdToMaxInbox.put(groupId, max);
        }
    }

    /**
     * Spreads the blah within the specified group.
     * Places the blah in a random group input box.
     * <p/>
     * Blahs may be spread to other inboxes in this group
     * and to other groups' inboxes via a separate mechanism (more offline).
     *
     * @param localeId
     * @param blahDAO
     * @param groupId
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public void spreadBlah(LocaleId localeId, BlahDAO blahDAO, String groupId) throws SystemErrorException {

        InboxBlahDAO dao = storeManager.createInboxBlah();
        dao.initToDefaultValues(localeId);

        dao.setBlahId(blahDAO.getId());
        dao.setBlahText(blahDAO.getText());
        dao.setType(blahDAO.getTypeId());
        dao.setGroupId(groupId);
        dao.setAuthorId(blahDAO.getAuthorId());
        Integer tmp = blahDAO.getUpVotes();
        if (tmp != null) {dao.setUpVotes(tmp);}
        tmp = blahDAO.getDownVotes();
        if (tmp != null) {dao.setDownVotes(tmp);}
        tmp = blahDAO.getOpens();
        if (tmp != null) {dao.setOpens(tmp);}
        tmp = blahDAO.getViews();
        if (tmp != null) {dao.setViews(tmp);}
//        dao.setStrength(0.85);
//        dao.setRecentStrength(0.85);

        final Integer maxInbox = getMaxInbox(groupId);
        final Integer inbox = (maxInbox == 0) ? 0 : random.nextInt(maxInbox + 1);

        dao.setInboxNumber(inbox);

        // Insert into db
        dao._insert();

        // Update inbox state in db
        updateInboxStateInDB(groupId, inbox, dao.getId());

        // Insert into cache
        BlahCache.getInstance().addInboxItem(dao.getId(), dao, inbox, groupId);
    }

    private void updateInboxStateInDB(String groupId, Integer inbox, String itemId) {
        final String stateId = BlahCache.getInstance().makeInboxStateKey(groupId, inbox);
        final DBObject query = new BasicDBObject(BaseDAOConstants.ID, stateId);
        final DBCollection stateCol = storeManager.getCollection(storeManager.getInboxStateCollectionName());
        final DBObject state = stateCol.findOne(query);
        if (state == null) {
            final DBObject insert = new BasicDBObject(BaseDAOConstants.ID, stateId);
            insert.put(InboxStateDAOConstants.INBOX_ITEM_IDS, Arrays.asList(new ObjectId[]{new ObjectId(itemId)}));
            insert.put(InboxStateDAOConstants.INBOX_NUMBER_TOP, inbox);
            stateCol.insert(insert);
        } else {
            final DBObject push = new BasicDBObject(InboxStateDAOConstants.INBOX_ITEM_IDS, new ObjectId(itemId));
            final BasicDBObject update = new BasicDBObject("$push", push);
            stateCol.update(new BasicDBObject(BaseDAOConstants.ID, stateId), update);
        }
    }

    /**
     * Returns the specified group's inbox.
     *
     * @param groupId       The id of the group for this inbox
     * @param inbox         The inbox number
     * @param type          Optionally, the blah's type (default: any blah type)
     * @param start         Optionally the first index in the inbox
     * @param count         Optionally the number of items to retrieve.
     * @param sortFieldName Optionally the name of the field to sort against
     * @param sortDirection
     * @return Inbox    The inbox
     * @throws SystemErrorException
     */
    public Inbox getInboxFromCache(String groupId, Integer inbox, String type, Integer start, Integer count, String sortFieldName, Integer sortDirection) throws SystemErrorException {
        // TODO type parameter ignored!
        final Inbox box = BlahCache.getInstance().getInbox(groupId, inbox, start, count, sortFieldName, sortDirection);
        if (box != null) {
            setMaxInbox(groupId, box.getTopInbox()); // update the max inbox for this group
        }
        return box;
    }
}