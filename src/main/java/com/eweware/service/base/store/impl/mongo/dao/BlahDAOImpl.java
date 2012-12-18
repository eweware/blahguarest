package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BlahDAO;
import main.java.com.eweware.service.base.store.dao.BlahTrackerDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author rk@post.harvard.edu
 */

public class BlahDAOImpl extends BaseDAOImpl implements BlahDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(TYPE_ID, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(TEXT, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(BODY, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_ID, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(AUTHOR_ID, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(VOTES, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(UP_VOTES, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(DOWN_VOTES, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENTS, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_STRENGTH, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(RECENT_BLAH_STRENGTH, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(STATS, MongoFieldTypes.ARRAY);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, MongoFieldTypes.DATE);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, MongoFieldTypes.DATE);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return BlahDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (BlahDAOImpl.collectionName == null) {
            BlahDAOImpl.collectionName = MongoStoreManager.getInstance().getBlahCollectionName();
        }
        return BlahDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (BlahDAOImpl.collection == null) {
            BlahDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return BlahDAOImpl.collection;
    }

    BlahDAOImpl() {
        super();
    }

    BlahDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    BlahDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return BlahSchema.getSchema(localeId);
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
    public String getText() {
        return (String) get(TEXT);
    }

    @Override
    public void setText(String text) {
        put(TEXT, text);
    }

    @Override
    public String getBody() {
        return (String) get(BODY);
    }

    @Override
    public void setBody(String body) {
        put(BODY, body);
    }

    @Override
    public String getGroupId() {
        return (String) get(GROUP_ID);
    }

    @Override
    public void setGroupId(String groupId) {
        put(GROUP_ID, groupId);
    }

    @Override
    public String getTypeId() {
        return (String) get(TYPE_ID);
    }

    @Override
    public void setTypeId(String type) {
        put(TYPE_ID, type);
    }

    @Override
    public Integer getViews() {
        return (Integer) get(VIEWS);
    }

    @Override
    public void setViews(Integer views) {
        put(VIEWS, views);
    }

    @Override
    public Integer getOpens() {
        return (Integer) get(OPENS);
    }

    @Override
    public void setOpens(Integer opens) {
        put(OPENS, opens);
    }

    @Override
    public Integer getComments() {
        return (Integer) get(COMMENTS);
    }

    @Override
    public void setComments(Integer comments) {
        put(COMMENTS, comments);
    }

    @Override
    public List<BlahTrackerDAO> getStats() {
        return (List<BlahTrackerDAO>) get(STATS);
    }

    @Override
    public void setStats(List<BlahTrackerDAO> stats) {
        put(STATS, stats);
    }

    @Override
    public Double getStrength() {
        return (Double) get(BLAH_STRENGTH);
    }

    @Override
    public void setStrength(Double strength) {
        put(BLAH_STRENGTH, strength);
    }

    @Override
    public Double getRecentStrength() {
        return (Double) get(RECENT_BLAH_STRENGTH);
    }

    @Override
    public void setRecentStrength(Double strength) {
        put(RECENT_BLAH_STRENGTH, strength);
    }

    @Override
    public Integer getVotes() {
        return (Integer) get(VOTES);
    }

    @Override
    public void setVotes(Integer votes) {
        put(VOTES, votes);
    }

    @Override
    public Integer getUpVotes() {
        return (Integer) get(UP_VOTES);
    }

    @Override
    public void setUpVotes(Integer votes) {
        put(UP_VOTES, votes);
    }

    @Override
    public Integer getDownVotes() {
        return (Integer) get(DOWN_VOTES);
    }

    @Override
    public void setDownVotes(Integer votes) {
        put(DOWN_VOTES, votes);
    }

}
