package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.BlahTypeDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.BlahTypeSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/23/12 Time: 2:46 PM
 */
public class BlahTypeDAOImpl extends BaseDAOImpl implements BlahTypeDAO {


    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(3);

    static {   // TODO should be derived from schema
        BlahTypeDAOImpl.FIELD_TO_TYPE_MAP.put(NAME, MongoFieldTypes.STRING);
        BlahTypeDAOImpl.FIELD_TO_TYPE_MAP.put(CATEGORY_ID, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (BlahTypeDAOImpl.collectionName == null) {
            BlahTypeDAOImpl.collectionName = MongoStoreManager.getInstance().getBlahTypeCollectionName();
        }
        return collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (BlahTypeDAOImpl.collection == null) {
            BlahTypeDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return BlahTypeDAOImpl.collection;
    }

     BlahTypeDAOImpl() {
        super();
    }

     BlahTypeDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

     BlahTypeDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return BlahTypeSchema.getSchema(localeId);
    }

    @Override
    public String getName() {
        return (String) get(NAME);
    }

    @Override
    public void setName(String name) {
        put(NAME, name);
    }

    @Override
    public Integer getCategoryId() {
        return (Integer) get(CATEGORY_ID);
    }

    @Override
    public void setCategoryId(Integer categoryId) {
        put(CATEGORY_ID, categoryId);
    }
}
