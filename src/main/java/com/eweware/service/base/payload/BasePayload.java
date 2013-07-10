package main.java.com.eweware.service.base.payload;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.BaseDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         These objects are used to serialize data
 *         in the REST service.
 */
public abstract class BasePayload extends LinkedHashMap<String, Object> implements BaseDAOConstants, Serializable {

    private BaseSchema cachedSchema;

    public BasePayload() {
        super();
    }

    public BasePayload(String id) {
        super();
        setId(id);
    }

    public BasePayload(Map<String, Object> map) {
        super(map);
        ensureStringAsId(map);
        ensureCanonicalDates(map);
    }

    /**
     * Adds the specified map "as-is" to this object's properties,
     * except for the object _id.
     *
     * @param map
     */
    public void addFromMap(Map<String, Object> map) {
        final Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(BaseDAO.ID)) {
                iterator.remove();
                break;
            }
        }
        putAll(map);
    }

    public String getId() {
        Object id = get(ID);
        return (id == null) ? null : id.toString();
    }

    public void setId(String id) {
        if (id != null) {
            put(ID, id);
        } else {
            remove(ID);
        }
    }

    public String getCreated() {
        return (String) get(CREATED);
    }

    public void setCreated(String utc) throws SystemErrorException {
        if (utc != null) {
            if (DateUtils.checkISODateTime(utc)) {
                put(CREATED, utc);
            } else {
                throw new SystemErrorException("on setCreated, invalid UTC date=" + utc);
            }
        } else {
            remove(CREATED);
        }
    }

    public String getUpdated() {
        return (String) get(UPDATED);
    }

    public void setUpdated(String utc) throws SystemErrorException {
        if (utc != null) {
            if (DateUtils.checkISODateTime(utc)) {
                put(UPDATED, utc);
            } else {
                throw new SystemErrorException("on setUpdated, invalid UTC date=" + utc);
            }
        } else {
            remove(UPDATED);
        }
    }

    // TODO not too elegant: the payload (client) expects a string representation, not a date. Would be nice to have a per-field autoconversion method
    @JsonIgnore
    protected void ensureCanonicalDateDate(String dateFieldName) {
        final Object dob = get(dateFieldName);
        if (dob != null && (dob instanceof Date)) {
            put(dateFieldName, DateUtils.formatDate((Date) dob));
        }
    }

    @JsonIgnore
    protected void ensureCanonicalDateTime(String dateFieldName) {
        final Object dob = get(dateFieldName);
        if (dob != null && (dob instanceof Date)) {
            put(dateFieldName, DateUtils.formatDateTime((Date) dob));
        }
    }

    /**
     * <p>Returns the schema for the payload's DAO representation.</p>
     *
     * @return the dao-equivalent schema
     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#getDAOSchema(main.java.com.eweware.service.base.i18n.LocaleId)
     */
    protected final BaseSchema getCachedSchema() throws SystemErrorException {
        if (cachedSchema != null) {
            return cachedSchema;
        }
        try {
            final Class<? extends BasePayload> claz = getClass();
            final Method getSchema = claz.getDeclaredMethod(BaseSchema.GET_SCHEMA_METHOD_NAME);
            final Object obj = getSchema.invoke(this);
            if (obj instanceof BaseSchema) {
                cachedSchema = (BaseSchema) obj;
            }
        } catch (NoSuchMethodException e) {
            throw new SystemErrorException("Method '" + BaseSchema.GET_SCHEMA_METHOD_NAME + "' not found via reflection: was name refactored?", e, ErrorCodes.SERVER_SEVERE_ERROR);
        } catch (InvocationTargetException e) {
            throw new SystemErrorException("Method '" + BaseSchema.GET_SCHEMA_METHOD_NAME + "' could not be invoked via reflection: were params refactored?", e, ErrorCodes.SERVER_SEVERE_ERROR);
        } catch (IllegalAccessException e) {
            throw new SystemErrorException("Method '" + BaseSchema.GET_SCHEMA_METHOD_NAME + "' could not be queried via reflection: were params refactored?", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
        return cachedSchema;
    }

//    // TODO this is already handled by the schema validateandconvert: check it out
//    @JsonIgnore
//    private void ensureCanonicalDates(Map<String, Object> map) {
//        BaseSchema schema = null;
//        try {
//            final Class<? extends BasePayload> claz = getClass();
//            final Method getSchema = claz.getDeclaredMethod(BaseSchema.GET_SCHEMA_METHOD_NAME);
//            final Object obj = getSchema.invoke(this);
//            if (obj instanceof BaseSchema) {
//                schema = (BaseSchema) obj;
//                ensureCanonicalDates1(map, schema);
//            }
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        // Enforce these for now TODO add CREATED and UPDATED to BaseDAOConstants in its own schema and inherit schema
//        Object created = map.get(CREATED);
//        if (created != null && (created instanceof Date)) {
//            put(CREATED, DateUtils.formatDateTime((Date) created));
//        }
//        Object updated = map.get(UPDATED);
//        if (updated != null && (updated instanceof Date)) {
//            put(UPDATED, DateUtils.formatDateTime((Date) updated));
//        }
//    }

    @JsonIgnore
    private void ensureCanonicalDates(Map<String, Object> map) {
        try {
            final BaseSchema schema = getCachedSchema();
            ensureCanonicalDates1(map, schema);
        } catch (SystemErrorException e) {
            e.printStackTrace();
        }

        // Enforce these for now TODO add CREATED and UPDATED to BaseDAOConstants in its own schema and inherit schema
//        Object created = map.get(CREATED);
//        if (created != null && (created instanceof Date)) {
//            put(CREATED, DateUtils.formatDateTime((Date) created));
//        }
//        Object updated = map.get(UPDATED);
//        if (updated != null && (updated instanceof Date)) {
//            put(UPDATED, DateUtils.formatDateTime((Date) updated));
//        }
    }

    private void ensureCanonicalDates1(Map<String, Object> map, BaseSchema schema) {
        final Map<String, SchemaSpec> specMap = schema.getFieldNameToSpecMap();
        for (Map.Entry<String, SchemaSpec> entry : specMap.entrySet()) {
            if (entry.getValue().getDataType() == SchemaDataType.DT) {
                final String fieldName = entry.getKey();
                final Object datetime = map.get(fieldName);
                if (datetime != null && (datetime instanceof Date)) {
                    put(fieldName, DateUtils.formatDateTime((Date) datetime));
                }
            }
        }
    }

    @JsonIgnore
    private void ensureStringAsId(Map<String, Object> obj) {
        Object id = obj.get(ID);
        if (id != null && !(id instanceof String)) {
            setId(id.toString()); // e.g., ObjectId.toString()
        }
    }
}
