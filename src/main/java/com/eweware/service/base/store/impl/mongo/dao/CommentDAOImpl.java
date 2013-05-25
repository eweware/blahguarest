package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.CommentDAO;
import main.java.com.eweware.service.base.store.dao.CommentTrackerDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.CommentSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public class CommentDAOImpl extends BaseDAOImpl implements CommentDAO {

    private static final long serialVersionUID = 1L;

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();
    static {   // TODO should be derived from schema
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_ID, MongoFieldTypes.STRING);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(TEXT, MongoFieldTypes.STRING);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(AUTHOR_ID, MongoFieldTypes.STRING);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_VOTE, MongoFieldTypes.NUMBER);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENT_VOTES, MongoFieldTypes.NUMBER);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENT_UP_VOTES, MongoFieldTypes.NUMBER);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENT_DOWN_VOTES, MongoFieldTypes.NUMBER);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENT_STRENGTH, MongoFieldTypes.NUMBER);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        CommentDAOImpl.FIELD_TO_TYPE_MAP.put(IMAGE_IDS, MongoFieldTypes.ARRAY);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return CommentDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (CommentDAOImpl.collectionName == null) {
            CommentDAOImpl.collectionName = MongoStoreManager.getInstance().getCommentCollectionName();
        }
        return CommentDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (CommentDAOImpl.collection == null) {
            try {
                CommentDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                e.printStackTrace();  // TODO catch
            }
        }
        return CommentDAOImpl.collection;
    }

    protected CommentDAOImpl() {
        super();
    }

    protected CommentDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    protected CommentDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return CommentSchema.getSchema(localeId);
    }

    @Override
    public String getText() {
        return (String) get(TEXT);
    }

    @Override
    public void setText(String text) {
        put(TEXT, text);
    }

    @Override
    public List<String> getImageIds() {
        return (List<String>) get(IMAGE_IDS);
    }

    @Override
    public void setImageIds(List<String> imageIds) {
        put(IMAGE_IDS, imageIds);
    }

    @Override
    public String getAuthorId() {
        return (String) get(AUTHOR_ID);
    }

    @Override
    public void setAuthorId(String authorId) {
        put(AUTHOR_ID, authorId);
    }

    @Override
    public String getBlahId() {
        return (String) get(BLAH_ID);
    }

    @Override
    public void setBlahId(String id) {
        put(BLAH_ID, id);
    }

    @Override
    public Long getBlahVote() {
        return (Long) get(BLAH_VOTE);
    }

    @Override
    public void setBlahVote(Long vote) {
        put(BLAH_VOTE, vote);
    }

    @Override
    public Long getCommentUpVotes() {
        return (Long) get(COMMENT_UP_VOTES);
    }

    @Override
    public void setCommentUpVotes(Long votes) {
        if (votes != null) {
            put(COMMENT_UP_VOTES, votes);
        } else {
            remove(COMMENT_UP_VOTES);
        }
    }

    @Override
    public Long getCommentDownVotes() {
        return (Long) get(COMMENT_DOWN_VOTES);
    }

    @Override
    public void setCommentDownVotes(Long votes) {
        if (votes != null) {
            put(COMMENT_DOWN_VOTES, votes);
        } else {
            remove(COMMENT_DOWN_VOTES);
        }
    }

    @Override
    public Long getCommentVotes() {
        return (Long) get(COMMENT_VOTES);
    }

    @Override
    public void setCommentVotes(Long votes) {
        put(COMMENT_VOTES, votes);
    }

    @Override
    public Double getStrength() {
        return (Double) get(COMMENT_STRENGTH);
    }

    @Override
    public void setStrength(Double strength) {
        if (strength != null) {
            put(COMMENT_STRENGTH, strength);
        } else {
            remove(COMMENT_STRENGTH);
        }
    }

    @Override
    public Long getViews() {
        return (Long) get(VIEWS);
    }

    @Override
    public void setViews(Long views) {
        put(VIEWS, views);
    }

    @Override
    public Long getOpens() {
        return (Long) get(OPENS);
    }

    @Override
    public void setOpens(Long opens) {
        put(OPENS, opens);
    }

    @Override
    public List<CommentTrackerDAO> getStats() {
        return (List<CommentTrackerDAO>) get(STATS);
    }

    @Override
    public void setStats(List<CommentTrackerDAO> stats) {
        put(STATS, stats);
    }
}
