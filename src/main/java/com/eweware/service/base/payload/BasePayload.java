package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.BaseDAOConstants;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
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
                throw new SystemErrorException("invalid UTC date=" + utc);
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
                throw new SystemErrorException("invalid UTC date=" + utc);
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

    // TODO this is already handled by the schema validateandconvert: check it out
    @JsonIgnore
    private void ensureCanonicalDates(Map<String, Object> map) {
        Object created = map.get(CREATED);
        if (created != null && (created instanceof Date)) {
            put(CREATED, DateUtils.formatDateTime((Date) created));
        }
        Object updated = map.get(UPDATED);
        if (updated != null && (updated instanceof Date)) {
            put(UPDATED, DateUtils.formatDateTime((Date) updated));
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
