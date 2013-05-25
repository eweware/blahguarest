package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.store.dao.schema.type.FieldValidator;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <p>Specifies the schema for a DAO field.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 9/7/12 Time: 10:25 AM
 */
public final class SchemaSpec extends HashMap<String, Object> implements SchemaConstants {

    // TODO  spec could indicate desired Index (e.g., Index.ANALYZE) and Store (e.g., Store.YES) values

    /**
     * <p>Returns the field's id</p>
     * @return
     */
    public String getFieldId() {
        return (String) get(SCHEMA_SPEC_FIELD_ID);
    }

    public void setFieldId(String fieldId) {
        put(SCHEMA_SPEC_FIELD_ID, fieldId);
    }

    public LinkedHashMap<String, Object> getValidationMap() {
        return (LinkedHashMap<String, Object>) get(SCHEMA_SPEC_VALIDATION_MAP);
    }

    public void setValidationMap(LinkedHashMap<String, Object> data) {
        put(SCHEMA_SPEC_VALIDATION_MAP, data);
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

    public FieldValidator getValidator() {
        return (FieldValidator) get(SCHEMA_SPEC_VALIDATOR);
    }

    public void setValidator(FieldValidator validator) {
        put(SCHEMA_SPEC_VALIDATOR, validator);
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

    public String getValidationRegexp() {
        return (String) get(SCHEMA_SPEC_REGEXP);
    }

    public void setValidationRegexp(String regExp) {
        put(SCHEMA_SPEC_REGEXP, regExp);
    }

    public boolean hasDefaultValue() {
        final Object v = get(SCHEMA_SPEC_HAS_DEFAULT_VALUE);
        return (v != null && ((Boolean) v));
    }

    public void setDefaultValue(boolean v) {
        put(SCHEMA_SPEC_HAS_DEFAULT_VALUE, new Boolean(v));
    }

    public Object getDefaultValue() {
        return get(SCHEMA_SPEC_DEFAULT_VALUE);
    }

    public void setDefaultValue(Object value) {
        put(SCHEMA_SPEC_DEFAULT_VALUE, value);
    }

    public boolean isNumeric() {
        return isInteger() || isLong() || isReal();
    }

    public boolean isReal() {
        return (getDataType() == SchemaDataType.R);
    }

    public boolean isInteger() {
        return (getDataType() == SchemaDataType.I);
    }

    public boolean isLong() {
        return (getDataType() == SchemaDataType.L);
    }

    public boolean isCalendar() {
        final SchemaDataType dataType = getDataType();
        return (dataType == SchemaDataType.DT || dataType == SchemaDataType.D);
    }

    public boolean isString() {
        return !isNumeric() && !isCalendar();
    }
}
