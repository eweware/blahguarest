package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/7/12 Time: 10:25 AM
 */
public final class SchemaSpec extends HashMap<String, Object> implements SchemaConstants {

    // TODO  spec could indicate desired Index (e.g., Index.ANALYZE) and Store (e.g., Store.YES) values

    public String getFieldId() {
        return (String) get(SCHEMA_SPEC_FIELD_ID);
    }

    public void setFieldId(String fieldId) {
        put(SCHEMA_SPEC_FIELD_ID, fieldId);
    }

    public LinkedHashMap<String, Object> getData() {
        return (LinkedHashMap<String, Object>) get(SCHEMA_SPEC_DATA);
    }

    public void setData(LinkedHashMap<String, Object> data) {
        put(SCHEMA_SPEC_DATA, data);
    }

    public SchemaDataType getDataType() {
        return (SchemaDataType) get(SCHEMA_SPEC_DATA_TYPE);
    }

    public void setDataType(SchemaDataType type) {
        put(SCHEMA_SPEC_DATA_TYPE, type);
    }

    public String getDisplayName() {
        return (String) get(SCHEMA_SPEC_DISPLAY_NAME);
    }

    public void setDisplayName(String displayName) {
        put(SCHEMA_SPEC_DISPLAY_NAME, displayName);
    }

    public Number getMinimumValue() {
        return (Number) get(SCHEMA_SPEC_MINIMUM);
    }

    public void setMinimumValue(Number value) {
        put(SCHEMA_SPEC_MINIMUM, value);
    }

    public Number getMaximumValue() {
        return (Number) get(SCHEMA_SPEC_MAXIMUM);
    }

    public void setMaximumValue(Number value) {
        put(SCHEMA_SPEC_MAXIMUM, value);
    }

    public String getRegExp() {
        return (String) get(SCHEMA_SPEC_REGEXP);
    }

    public void setRegExp(String regExp) {
        put(SCHEMA_SPEC_REGEXP, regExp);
    }

    public Object getDefaultValue() {
        return get(SCHEMA_SPEC_DEFAULT_VALUE);
    }

    public void setDefaultValue(Object value) {
        put(SCHEMA_SPEC_DEFAULT_VALUE, value);
    }

    public boolean isNumeric() {
        return isInteger() || isReal();
    }

    public boolean isReal() {
        return (getDataType() == SchemaDataType.R);
    }

    public boolean isInteger() {
        return (getDataType() == SchemaDataType.I);
    }

    public boolean isCalendar() {
        final SchemaDataType dataType = getDataType();
        return (dataType == SchemaDataType.DT || dataType == SchemaDataType.D);
    }

    public boolean isString() {
        return !isNumeric() && !isCalendar();
    }
}
