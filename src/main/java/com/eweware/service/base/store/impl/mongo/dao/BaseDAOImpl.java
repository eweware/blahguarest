package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.*;
import main.java.com.eweware.service.base.error.DuplicateKeyException;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import main.java.com.eweware.service.base.store.dao.schema.type.FieldValidator;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.type.DAOUpdateType;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;
import org.bson.types.ObjectId;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         TODO still need to comb for IllegalArgumentException and possibly other runtime exception in Mongo calls below
 */
abstract class BaseDAOImpl extends BasicDBObject implements BaseDAO {

    private static final Logger logger = Logger.getLogger(BaseDAOImpl.class.getName());

    // MongoException error code for duplicate keys
//    private static final int MONGO_DUPLICATE_KEY_ERROR_CODE = 11001;

    /**
     * Provides Mongo field types for BaseDAOImpl fields to subclasses.
     *
     * @param map A map to which to add the Mongo data types.
     */
    protected static void addInheritedFieldToTypeMapItems(Map<String, MongoFieldTypes> map) {
        map.put(CREATED, MongoFieldTypes.DATE);
        map.put(UPDATED, MongoFieldTypes.DATE);
        map.put(IS_DELETED, MongoFieldTypes.BOOLEAN);
    }

    /**
     * General constructor.
     */
    BaseDAOImpl() {
        super();
    }

    /**
     * Constructor for a dao with known id.
     *
     * @param id The id.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    BaseDAOImpl(String id) throws SystemErrorException {
        this();
        setId(id); // need ObjectId
    }

    /**
     * Constructor for a plain map
     *
     * @param map                The map of field names to values.
     * @param validateAndConvert If true, the map's entries will be validated
     *                           against the schema and the field values converted
     *                           to the DB internal data type format when necessary.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    BaseDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map);
        if (validateAndConvert) {
            validateAndConvertFields();
        }
    }

    /**
     * All subclasses must override this method.
     *
     * @return BaseSchema   The schema for this DAO.
     */
    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        throw new SystemErrorException("getSchema not implemented by BaseSchema class ", ErrorCodes.SERVER_SEVERE_ERROR);
    }

    private final static Map<Class, Method> classToGetSchemaMethodMap = new HashMap<Class, Method>();

    /**
     * Initializes all empty fields this object to their schema default values, if any.
     *
     * @param localeId
     * @throws SystemErrorException
     */
    public void initToDefaultValues(LocaleId localeId) throws SystemErrorException {
        final BaseSchema schema = getDAOSchema(localeId);
        final Map<String, SchemaSpec> fieldNameToSpecMap = schema.getFieldNameToSpecMap();
        for (Map.Entry<String, SchemaSpec> entry : fieldNameToSpecMap.entrySet()) {
            final String fieldName = entry.getKey();
            if (!containsField(fieldName)) {
                final SchemaSpec spec = entry.getValue();
                if (spec.hasDefaultValue()) {
                    put(fieldName, spec.getDefaultValue());
                }
            }
        }
    }

    /**
     * Verifies that each field in this instance is in the schema (to prevent an illegal insertion),
     * validates the field value, and converts the value (if necessary) to the
     * value type expected by DB schema (e.g., a String may be accepted as
     * a value for a date, but the DAO stores the date in an internal ISO date format).
     *
     * @throws SystemErrorException
     */
    private void validateAndConvertFields() throws SystemErrorException {
        validateAndConvertFields(null);
    }

    /**
     * Verifies that each field in the supplied map is in the schema (to prevent an illegal insertion),
     * validates the field value, and converts the value (if necessary) to the
     * value type expected by DB schema (e.g., a String may be accepted as
     * a value for a date, but the DAO stores the date in an internal ISO date format).
     *
     * @param map The field to value map to validate and convert. If null, all of
     *            this instances fields will be validated and, if necessary, converted.
     * @throws SystemErrorException
     */
    private void validateAndConvertFields(Map<String, Object> map) throws SystemErrorException {

        if (map == null) {
            return;
        }
//        map = new HashMap<String, Object>(map);

        // TODO support embedded schemas (SchemaDataType.E): this would make this method recursive
        // TODO sanity checks on string lengths
        final BaseSchema schema = getDAOSchema(LocaleId.en_us);
        if (schema == null) {
            System.out.println("validateAndConvertFields: Ignoring missing schema for " + this.getClass().getSimpleName()); // dbg
        } else {
            final Map<String, SchemaSpec> fieldNameToSpecMap = schema.getFieldNameToSpecMap();
            final Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
            for (Iterator<Map.Entry<String, Object>> it = iterator; it.hasNext(); ) {
                final Map.Entry<String, Object> entry = it.next();
                final String fieldName = entry.getKey();
                final Object value = entry.getValue();
                final SchemaSpec spec = fieldNameToSpecMap.get(fieldName);
                if (spec == null) { // not in schema: remove it
                    logger.warning(getClass().getSimpleName() + ": validateAndConvertFields removed fieldName=" + fieldName + " without a spec");
                    it.remove();
                } else {
                    final SchemaDataType dataType = spec.getDataType();
                    if (dataType == SchemaDataType.S) {
                        if (value != null && ((String) value).length() > 4000) {
                            throw new SystemErrorException(getClass().getSimpleName() + ": validateAndConvertFields; string field '" + fieldName + "'s length was " + ((String) value).length() + " but maximum allowed is 4000", ErrorCodes.MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED_4000_CHARS);
                        }
                    }
                    final FieldValidator converter = spec.getValidator() == null ? dataType.getConverter() : spec.getValidator();
                    if (!converter.isValid(value, spec)) {
                        logger.warning(getClass().getSimpleName() + ": validateAndConvertFields: Ignored invalid value '" + value + "' for fieldName=" + fieldName + " in: " + this.getClass().getSimpleName() + "\nspec=" + spec); // TODO dbg
                        it.remove(); // not a valid value: remove it
                    } else {
                        entry.setValue(converter.toValidValue(value, spec));
                    }
                }
            }
        }
    }

    private BaseSchema getDAOSchema(LocaleId localeId) throws SystemErrorException {
        Method getSchemaMeth = null;
        try {
            final Class<? extends BaseDAOImpl> clas = this.getClass();
            getSchemaMeth = classToGetSchemaMethodMap.get(clas);
            if (getSchemaMeth == null) {
                getSchemaMeth = clas.getMethod(BaseSchema.GET_SCHEMA_METHOD_NAME, LocaleId.class);
                if (getSchemaMeth == null) {
                    throw new SystemErrorException(getClass().getSimpleName() + ": getDAOSchema; server configuration error. No way to get schema using '" + BaseSchema.GET_SCHEMA_METHOD_NAME, ErrorCodes.SERVER_SEVERE_ERROR);
                }
                classToGetSchemaMethodMap.put(clas, getSchemaMeth);
            }
            return (BaseSchema) getSchemaMeth.invoke(null, localeId);
        } catch (Exception e) {
            final Throwable cause = e.getCause();
            String error = (cause != null && cause.getMessage() != null) ? cause.getMessage() : e.getMessage();
            throw new SystemErrorException(getClass().getSimpleName() + ": getDAOSchema; error for class " + getClass().getSimpleName() + " trying to get schema\nERROR: " + error + "\nMAP: " + this, e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    /**
     * <p>Returns the object's unique id as a string representation.</p>
     *
     * @return String Returns this object's unique id.
     */
    @Override
    public String getId() {
        final Object obj = get(ID); // ID is an ObjectId instance
        return (obj == null) ? null : obj.toString();
    }

    /**
     * Sets this object unique id.
     *
     * @param id A UUID string uniquely identifying this object. If null,
     *           the id is removed.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    @Override
    public void setId(String id) throws SystemErrorException {
        if (id != null) {
            put(ID, makeMongoId(id));
        } else {
            put(ID, null); // don't user remove: null => empty field for atomic/incremental updates!
        }
    }

    /**
     * @return Boolean  Returns true if this object has been marked as deleted.
     */
    @Override
    public Boolean getDeleted() {
        final Object o = get(IS_DELETED);
        return (o == null) ? Boolean.FALSE : (Boolean) o; // as if default were false
    }

    /**
     * Marks or unmarks this object as deleted.
     *
     * @param deleted
     */
    @Override
    public void setDeleted(Boolean deleted) {
        if (deleted != null) {
            put(IS_DELETED, deleted);
        } else {
            remove(IS_DELETED);
        }
    }

    @Override
//    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getCreated() {
        return (Date) get(CREATED);
    }

    @Override
    public void setCreated(Date created) {
        put(CREATED, created);
    }

    @Override
    public Date getUpdated() {
        return (Date) get(UPDATED);
    }

    @Override
    public void setUpdated(Date updated) {
        put(UPDATED, updated);
    }

    /**
     * Returns the appropriate id. Subclasses that need to use
     * an id that is not a Mongo ObjectId should override this class.
     *
     * @param id The id as a string
     * @return Object   A mongo ObjectId for the specified id
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *          Thrown if the specified id
     *          is invalid.
     */
    protected Object makeMongoId(String id) throws SystemErrorException {
        try {
            return new ObjectId(id);
        } catch (Exception e) {
            throw new SystemErrorException(getClass().getSimpleName() + ": makeMongoId could not generate id '"+id+"' because id is not an appropriate UUID string", e, ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
    }

    /**
     * @return Map<String, MongoFieldTypes> Provides a map from a field name to its data type.
     */
    protected abstract Map<String, MongoFieldTypes> _getFieldNameToTypeMap();

    /**
     * @return String                                Returns the name of the collection for the db object.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    protected abstract String _getCollectionName() throws SystemErrorException;

    /**
     * @return DBCollection                              Returns the collection for the db object.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    protected abstract DBCollection _getCollection() throws SystemErrorException;


    // Workhorse methods to access/update MongoDB daos -------------------------------------------------------------

    // Cache maps a BaseDAOImpl class to its default constructor for _findX operations
    private static final Map<Class<? extends BaseDAOImpl>, Constructor<? extends BaseDAOImpl>> classToConstructorMap = new HashMap<Class<? extends BaseDAOImpl>, Constructor<? extends BaseDAOImpl>>();

    @Override
    public BaseDAO _findByPrimaryId(String... fieldsToReturnHint) throws SystemErrorException {
        try {
            final DBObject id = new BasicDBObject(ID, get(ID)); // note that get(ID) retrieves the ObjectId object (don't use getId())
            DBObject fields = (fieldsToReturnHint.length == 0) ? null : makeFieldsToReturnMap(fieldsToReturnHint);
            final DBCollection collection = _getCollection();
            DBObject dao = null;
            try {
                dao = findOneRetry(id, fields, collection);
            } catch (SystemErrorException e) {
                throw e;
            } catch (Exception e) {
                throw new SystemErrorException("Failed to find by primary id for object=" + this, e, ErrorCodes.SERVER_DB_ERROR);
            }
            return (dao == null) ? null : findDAOConstructor().newInstance(dao, false);
        } catch (Exception e) {
            throw new SystemErrorException(makeErrorMessage("_findByPrimaryId", "find", null, e, null), e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    private DBObject findOneRetry(DBObject criteria, DBObject fields, DBCollection collection) throws SystemErrorException {
        DBObject obj = null;
        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                obj = collection.findOne(criteria, fields);  // getting SocketException inside here
                break;
            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("findOneRetry", "find", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning(getClass().getName() + ": findOneRetry failed and will retry in attempt #" + attempt + " in collection " + _getCollection());
                }
            }
        }
        return obj;
    }

    @Override
    public List<? extends BaseDAO> _findMany() throws SystemErrorException {
        return _findMany(null, null, null);
    }

    @Override
    public List<? extends BaseDAO> _findMany(Integer start, Integer count, String sortFieldName) throws SystemErrorException {
        try {
            final DBCursor found = findManyRetry(start, count, sortFieldName);
            if (found.count() == 0) {
                return new ArrayList<BaseDAO>(0);
            }
            final Constructor<? extends BaseDAO> constructor = findDAOConstructor();
            final List<BaseDAO> daos = new ArrayList<BaseDAO>(found.count());
            while (found.hasNext()) {
                daos.add(constructor.newInstance(found.next(), false));
            }
            return daos;
        } catch (SystemErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(makeErrorMessage("_findMany", "find", null, e, null), e, ErrorCodes.SERVER_DB_ERROR);
        }

    }

    private DBCursor findManyRetry(Integer start, Integer count, String sortFieldName) throws SystemErrorException {
        DBCursor cursor = null;
        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                if (sortFieldName == null) {
                    cursor = ((start != null && count != null) ? _getCollection().find(this).skip(start).limit(count) :
                            (start != null) ? _getCollection().find(this).skip(start) :
                                    (count != null) ? _getCollection().find(this).limit(count) :
                                            _getCollection().find(this));
                } else {
                    final DBObject sort = new BasicDBObject(sortFieldName, 1);
                    cursor = ((start != null && count != null) ? _getCollection().find(this).sort(sort).skip(start).limit(count) :
                            (start != null) ? _getCollection().find(this).sort(sort).skip(start) :
                                    (count != null) ? _getCollection().find(this).sort(sort).limit(count) :
                                            _getCollection().find(this).sort(sort));
                }
                break;
            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("findManyRetry", "find", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning(getClass().getSimpleName() + ": findManyRetry failed in attempt # " + attempt + " in collection " + _getCollection());
                }
            }
        }
        return cursor;
    }


    private DBCursor findManyByCriteriaRetry(Integer start, Integer count, String sortFieldName, DBObject criteria, DBObject fields) throws SystemErrorException {
        DBCursor cursor = null;
        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                if (sortFieldName == null) {
                    cursor = (start != null && count != null) ? _getCollection().find(criteria, fields).skip(start).limit(count) :
                            (start != null) ? _getCollection().find(criteria, fields).skip(start) :
                                    (count != null) ? _getCollection().find(criteria, fields).limit(count) :
                                            _getCollection().find(criteria, fields);
                } else {
                    final DBObject sort = new BasicDBObject(sortFieldName, 1);
                    cursor = (start != null && count != null) ? _getCollection().find(criteria, fields).sort(sort).skip(start).limit(count) :
                            (start != null) ? _getCollection().find(criteria, fields).sort(sort).skip(start) :
                                    (count != null) ? _getCollection().find(criteria, fields).sort(sort).limit(count) :
                                            _getCollection().find(criteria, fields).sort(sort);
                }
                break;
            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("findManyByCriteriaRetry", "find", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning(getClass().getSimpleName() + ": findManyByCriteriaRetry failed in attempt #" + attempt + " in collection '" + _getCollection() + "'");
                }
            }
        }
        return cursor;
    }

    public List<? extends BaseDAO> _findManyByCompositeId(Integer start, Integer count, String sortFieldName, String[] fieldsToReturnHint, String... idFieldNames) throws SystemErrorException {
        if (idFieldNames.length == 0) {
            throw new SystemErrorException(makeErrorMessage("_findManyByCompositeId", "find", null, null, "Missing id field names: must have at least one in object"), ErrorCodes.SERVER_DB_ERROR);
        }
        DBObject criteria = new BasicDBObject();
        try {
            for (String idFieldName : idFieldNames) {
                final Object obj = get(idFieldName);
                if (obj == null) {
                    throw new SystemErrorException(makeErrorMessage("_findManyByCompositeId", "find", null, null, "'. Missing compound id field '" + idFieldName + "'"), ErrorCodes.SERVER_DB_ERROR);
                }
                criteria.put(idFieldName, obj);
            }

            final DBObject fields = (fieldsToReturnHint == null || fieldsToReturnHint.length == 0) ? null : makeFieldsToReturnMap(fieldsToReturnHint);
            DBCursor cursor = null;
            try {
                cursor = findManyByCriteriaRetry(start, count, sortFieldName, criteria, fields);
            } catch (SystemErrorException e) {
                throw e;
            } catch (Exception e) {
                throw new SystemErrorException("_findManyByCompositeId failed", e, ErrorCodes.SERVER_DB_ERROR);
            }
            if (cursor.count() == 0) {
                return new ArrayList<BaseDAO>(0);
            }

            final Constructor<? extends BaseDAO> constructor = findDAOConstructor();
            List<BaseDAO> daos = new ArrayList<BaseDAO>(cursor.count());
            while (cursor.hasNext()) {
                daos.add(constructor.newInstance(cursor.next(), false));
            }
            return daos;
        } catch (Exception e) {
            throw new SystemErrorException(makeErrorMessage("_findManyByCompositeId", "find", null, e,  "' with compound id fields=" + idFieldNames), e, ErrorCodes.SERVER_DB_ERROR);
        }
    }

    @Override
    public BaseDAO _findByCompositeId(String[] fieldsToReturnHint, String... idFieldNames) throws SystemErrorException {
        if (idFieldNames.length == 0) {
            throw new SystemErrorException(makeErrorMessage("_findByCompositeId", "find", null, null, "'. Missing composite id field names: must have at least one composite id"), ErrorCodes.SERVER_DB_ERROR);
        }
        final DBObject criteria = new BasicDBObject();
        try {
            for (String idFieldName : idFieldNames) {
                final Object obj = get(idFieldName);
                if (obj == null) {
                    throw new SystemErrorException(makeErrorMessage("_findByCompositeId", "find", null, null, "null value for compound id field '"+idFieldName+"'"), ErrorCodes.SERVER_DB_ERROR);
                }
                criteria.put(idFieldName, obj);
            }
            final DBObject fields = (fieldsToReturnHint == null || fieldsToReturnHint.length == 0) ? null : makeFieldsToReturnMap(fieldsToReturnHint);
            DBObject dao = null;
            try {
                dao = findOneRetry(criteria, fields, _getCollection());
            } catch (SystemErrorException e) {
                throw e;
            } catch (Exception e) {
                throw new SystemErrorException("Failed to find by composite id '" + Arrays.asList(idFieldNames) + " for object=" + this, e, ErrorCodes.SERVER_DB_ERROR);
            }
            return (dao == null) ? null : findDAOConstructor().newInstance(dao, false);
        } catch (Exception e) {
            throw new SystemErrorException(makeErrorMessage("_findByCompositeId", "find", null, null, "' with compound id fields=" + idFieldNames + "'"), e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    @Override
    public List<? extends BaseDAO> _findRangeSingleField(boolean sort, String fieldName, String from, boolean fromInclusive, String to, boolean toInclusive) throws SystemErrorException {
        if (from == null) {
            throw new SystemErrorException(makeErrorMessage("_findRangeSingleField", "find", null, null, "'from' field is null"), ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        final String op1 = fromInclusive ? "$gte" : "$gt";
        final DBObject criteria = new BasicDBObject();
        final DBObject range = new BasicDBObject();
        criteria.put(fieldName, range);
        range.put(op1, from);
        if (to != null) {
            final String op2 = toInclusive ? "$lte" : "$lt";
            range.put(op2, to);
        }
        final List<BaseDAO> matches = new ArrayList<BaseDAO>();
        try {
            final Constructor<? extends BaseDAOImpl> constructor = findDAOConstructor();
            DBCursor cursor = null;
            try {
                cursor = findManyByCriteriaRetry(null, null, fieldName, criteria, null);
            } catch (SystemErrorException e) {
                throw e;
            } catch (Exception e) {
                throw new SystemErrorException("_findRangeSingleField failed", e, ErrorCodes.SERVER_DB_ERROR);
            }
//            for (DBObject obj : sort ? col.find(criteria).sort(new BasicDBObject(fieldName, 1)) : col.find(criteria)) {
            for (DBObject obj : cursor) {
                matches.add(constructor.newInstance(obj, false));
            }
        } catch (Exception e) {
            throw new SystemErrorException(makeErrorMessage("_findRangeSingleField", "find", null, e, "range from '" + from + "' to '" + to + "'"), e, ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }

        return matches;
    }

    @Override
    public void _insert() throws SystemErrorException, DuplicateKeyException {
        final DBCollection col = _getCollection();
        this.setCreated(new Date());
        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                final WriteResult result = col.insert(this, WriteConcern.SAFE);
                if (result.getError() != null) {
                    throw new SystemErrorException(makeErrorMessage("_insert","insert",attempt, null, result.getError()), ErrorCodes.SERVER_DB_ERROR);
                }
                break;
            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("_insert", "insert", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning(getClass().getSimpleName() + ": _insert failed in attempt #" + attempt + " in collection '" + _getCollectionName() + "'");
                }
            }
        }
    }

    @Override
    public void _updateByPrimaryId(DAOUpdateType updateType) throws SystemErrorException, DuplicateKeyException {
        if (this.getId() == null) {
            throw new SystemErrorException(makeErrorMessage("_updateByPrimaryId", "update", null, null, "primary id is null"), ErrorCodes.SERVER_DB_ERROR);
        }
        if (updateType == null) {
            throw new SystemErrorException(makeErrorMessage("_updateByPrimaryId", "update", null, null, "update type is null"), ErrorCodes.SERVER_DB_ERROR);
        }

        this.setUpdated(new Date());

        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                WriteResult result = _getCollection().update(
                        new BasicDBObject(ID, makeMongoId(this.getId())),
                        makeAtomicUpdateObject(updateType),
                        false, false, WriteConcern.SAFE);
                if (result.getError() != null) {
                    throw new SystemErrorException(makeErrorMessage("_updateByPrimaryId", "update", null, null, result.getError()), ErrorCodes.SERVER_DB_ERROR);
                }
                break;

            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("_updateByPrimaryId", "update", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning("_updateByPrimaryId failed in attempt #" + attempt + " in collection '" + _getCollection() + "'");
                }
            }
        }
    }

    @Override
    public void _updateByCompoundId(DAOUpdateType updateType, String... idFieldNames) throws SystemErrorException, DuplicateKeyException {
        if (idFieldNames.length == 0) {
            throw new SystemErrorException(makeErrorMessage("_updateByCompoundId", "update", null, null, "Missing compound id field names in object"), ErrorCodes.SERVER_SEVERE_ERROR);
        }
        if (updateType == null) {
            throw new SystemErrorException(makeErrorMessage("_updateByCompoundId", "update", null, null, "The update type was null"), ErrorCodes.SERVER_SEVERE_ERROR);
        }
        final DBObject criteria = new BasicDBObject();
        this.setUpdated(new Date());
        for (String idFieldName : idFieldNames) {
            final Object obj = get(idFieldName);
            if (obj == null) {
                throw new SystemErrorException(makeErrorMessage("_updateByCompoundId", "update", null, null, "One of the compound field values was null in object."), ErrorCodes.SERVER_SEVERE_ERROR);
            }
            criteria.put(idFieldName, obj);
        }
        updateRetry(updateType, criteria);
    }

    private void updateRetry(DAOUpdateType updateType, DBObject criteria) throws SystemErrorException {
        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                final WriteResult result = _getCollection().update(
                        criteria,
                        makeAtomicUpdateObject(updateType),
                        false, false, WriteConcern.SAFE);
                if (result.getError() != null) {
                    throw new SystemErrorException(makeErrorMessage("updateRetry", "update", attempt, null, result.getError()), ErrorCodes.SERVER_DB_ERROR);
                }
                break;
            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("updateRetry", "update", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning("updateRetry failed in attempt #" + attempt + " in collection '" + _getCollectionName() + "'");
                }
            }
        }
    }

    @Override
    public void _deleteByPrimaryId() throws SystemErrorException {
        if (this.getId() == null) {
            throw new SystemErrorException(makeErrorMessage("_deleteByPrimaryId", "delete", null, null, "Missing primary id"));
        }
        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                final WriteResult result = _getCollection().remove(new BasicDBObject(ID, makeMongoId(this.getId())), WriteConcern.SAFE);
                if (result.getError() != null) {
                    throw new SystemErrorException(makeErrorMessage("_deleteByPrimaryId", "delete", attempt, null, result.getError()), ErrorCodes.SERVER_DB_ERROR);
                }
                break;
            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("_deleteByPrimaryId", "delete", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning("_deleteByPrimaryId failed in attempt #" + attempt + " in collection '" + _getCollectionName() + "'");
                }
            }
        }
    }

    @Override
    public void _deleteByCompositeId(String... idFieldNames) throws SystemErrorException {
        if (idFieldNames.length == 0) {
            throw new SystemErrorException(makeErrorMessage("_deleteByCompositeId", "delete", null, null, "Composite id field names were not specified"));
        }
        final DBObject query = new BasicDBObject(idFieldNames.length);
        for (String idFieldName : idFieldNames) {
            final Object value = this.get(idFieldName);
            if (value == null) {
                throw new SystemErrorException(makeErrorMessage("_deleteByCompositeId", "delete", null, null, "null value in composite id field '" + idFieldName + "'"), ErrorCodes.SERVER_DB_ERROR);
            }
            query.put(idFieldName, value);
        }
        for (int attempt = 1; attempt < 5; attempt++) {
            try {
                final WriteResult result = _getCollection().remove(query, WriteConcern.SAFE);
                if (result.getError() != null) {
                    throw new SystemErrorException(makeErrorMessage("_deleteByCompositeId", "delete", attempt, null, result.getError()), ErrorCodes.SERVER_DB_ERROR);
                }
                break;
            } catch (Exception e) {
                if (attempt > 3) {
                    throw new SystemErrorException(makeErrorMessage("_deleteByCompositeId", "delete", attempt, e, null), e, ErrorCodes.SERVER_DB_ERROR);
                } else {
                    logger.warning("_deleteByCompositeId failed in attempt #" + attempt + " in collection '" + _getCollectionName() + "'");
                }
            }
        }
    }

    private String makeErrorMessage(String method, String action, Integer attempt, Exception exception, String suffixMessage) throws SystemErrorException {
        final StringBuilder b = new StringBuilder(getClass().getSimpleName());
        b.append(": ");
        b.append(method);
        b.append(" failed to ");
        b.append(action);
        b.append(" dao id '");
        b.append(this.getId());
        b.append("' in object=");
        b.append(this);
        b.append(" in collection '");
        b.append(_getCollection());
        if (attempt != null) {
            b.append(" after ");
            b.append(attempt);
            b.append(" attempts.");
        }
        if ((exception != null) && (exception instanceof MongoException)) {
            b.append(" Mongo error code=");
            b.append(((MongoException) exception).getCode());
        }
        if (suffixMessage != null) {
            b.append(". ");
            b.append(suffixMessage);
        }
        return b.toString();
    }

    @Override
    public void addFromMap(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        if (map == null) {
            throw new SystemErrorException(getClass().getSimpleName() + ": addFromMap failed to add fields to this DAO in collection '" + _getCollection() + "'. map to add was null for dao object=" + this, ErrorCodes.SERVER_DB_ERROR);
        }
        if (validateAndConvert) {
            validateAndConvertFields(map);
        }
        putAll(map);
    }

    @Override
    public boolean _exists() throws SystemErrorException {
        return _getCollection().count(this) != 0;
    }

    @Override
    public long _count() throws SystemErrorException {
        return _getCollection().count();
    }


    /**
     * Creates an atomic additive update. No fields are removed.
     *
     * @param updateType
     * @return DBObject Containing the atomic update commands
     */
    private DBObject makeAtomicUpdateObject(DAOUpdateType updateType) throws SystemErrorException {
        Map<String, Object> updates = this;
        final int size = updates.size();
        final BasicDBObject updater = new BasicDBObject(size);
        if (size == 0) {
            return updater;
        }
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            final String fieldName = entry.getKey();
            if (fieldName.equals(ID)) {
                continue; // skip!
            }
            // Get map and NEVER transverse it
            final Map<String, MongoFieldTypes> map = _getFieldNameToTypeMap();
            if (map == null) {
                throw new SystemErrorException(getClass().getSimpleName() + ": missing field name type map", ErrorCodes.SERVER_SEVERE_ERROR);
            }
            final MongoFieldTypes type = map.get(fieldName);
            if (type == null) {
                throw new SystemErrorException(getClass().getSimpleName() + ": makeAtomicUpdateObject failed operation in collection '" + _getCollection() + "' due to null data type (did implementation not add the data type map for this field?) for fieldName '" + fieldName + "' in object=" + this + "\ndata type field map: " + map, ErrorCodes.SERVER_SEVERE_ERROR);
            }

            final Object value = entry.getValue();
            final boolean nullValue = (value == null);

            String operation = null;
            switch (updateType) {
                case INCREMENTAL_DAO_UPDATE:
                    operation = type.getIncrementalOperation();
                    break;
                case ABSOLUTE_UPDATE:
                    operation = nullValue ? type.getDeleteOperation() : type.getOverwriteOperation();
                    break;
                default:
                    throw new SystemErrorException(getClass().getSimpleName() + ": Operation '" + operation + "' is not valid for update type '" + updateType + "'", ErrorCodes.SERVER_SEVERE_ERROR);
            }
            Map<String, Object> operationContents = (Map<String, Object>) updater.get(operation);
            if (operationContents == null) {
                operationContents = new HashMap<String, Object>(5);
                updater.put(operation, operationContents);
            }

            if (value == null && operation.equals("$unset")) {
                operationContents.put(fieldName, 1); // deletes
            } else if (type == MongoFieldTypes.SET) {
                if (value instanceof Collection<?>) {
                    for (Object obj : (Collection<?>) value) {
                        operationContents.put(fieldName, obj); // TODO test whether mongo complains if there are two fields with the same name (e.g., pushing two values into same field)
                    }
                }
            } else if (type == MongoFieldTypes.ARRAY) {
                if (value == null) {  // nuke array
                    updater.remove(operation);
                    updater.put("$unset", new BasicDBObject(fieldName, 1));
                } else if (value instanceof Collection<?>) {
                    operationContents.put(fieldName, new BasicDBObject("$each", value));
                } else {
                    throw new SystemErrorException("Expected collection in field '" + fieldName + "' operation '" + operation + "'", ErrorCodes.SERVER_SEVERE_ERROR);
                }
            } else {
                operationContents.put(fieldName, value);
            }
        }
        return updater;
    }

    /**
     * Creates a map specifying which fields should be returned.
     *
     * @param fieldsToReturnHint An array of fields to return.
     * @return DBObject A db object containing the map, or null
     *         if there are no fields in the specified parameter.
     */
    private DBObject makeFieldsToReturnMap(String[] fieldsToReturnHint) {
        DBObject fieldsToReturn = null;
        if (fieldsToReturnHint.length != 0) {
            fieldsToReturn = new BasicDBObject(fieldsToReturnHint.length);
            for (String field : fieldsToReturnHint) {
                fieldsToReturn.put(field, 1);
            }
        }
        return fieldsToReturn;
    }

    private Constructor<? extends BaseDAOImpl> findDAOConstructor() throws NoSuchMethodException {
        final Class<? extends BaseDAOImpl> c = getClass();
        Constructor<? extends BaseDAOImpl> constructor = classToConstructorMap.get(c);
        if (constructor == null) {
            constructor = c.getDeclaredConstructor(Map.class, boolean.class);
            classToConstructorMap.put(c, constructor);
        }
        return constructor;
    }
}


//    @Override
//    public List<? extends BaseDAO> _findManyByPrimaryIds(String[] primaryIds) throws SystemErrorException {
//        if (primaryIds == null || primaryIds.length == 0) {
//            throw new SystemErrorException(getClass().getSimpleName()+": Failed to find objects. Missing primary ids in object=" + this, ErrorCodes.SERVER_SEVERE_ERROR);
//        }
//        try {
//            final List<Object> ids = new ArrayList<Object>(primaryIds.length);
//            for (String id : primaryIds) {
//                ids.add(makeMongoId(id));
//            }
//            final BasicDBObject clause = new BasicDBObject("$in", ids);
//            final DBObject query = new BasicDBObject(ID, clause);
//            final DBCursor cursor =  _getCollection().find(query);
//            if (cursor.count() == 0) {
//                return new ArrayList<BaseDAO>(0);
//            }
//            final Constructor<? extends BaseDAO> constructor = findDAOConstructor();
//            final List<BaseDAO> result = new ArrayList<BaseDAO>(cursor.count());
//            while (cursor.hasNext()) {
//                result.add(constructor.newInstance(cursor.next(), false));
//            }
//
//            return result;
//        } catch (Exception e) {
//            throw new SystemErrorException(getClass().getSimpleName()+": Failed to find objects with ids=" + primaryIds + "in object=" + this, e, ErrorCodes.SERVER_DB_ERROR);
//        }
//    }