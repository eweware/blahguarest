package com.eweware.service.base.store.dao.schema;

public interface SchemaConstants {

    /**
     * The field id of this spec
     */
    static final String SCHEMA_SPEC_FIELD_ID = "fid";

    /**
     * A regular expression to be used to select valid values for this field.
     * Depending on the field, this is an i18n value.
     */
    static final String SCHEMA_SPEC_REGEXP = "R";

    /**
     * Minimum value of real/integer or minimum string length
     * (Undefined for dates for now but could be a date)
     * Depending on the field, this is an i18n value.
     */
    static final String SCHEMA_SPEC_MINIMUM = "m";

    /**
     * Maximum value of real/integer or minimum string length
     * (Undefined for dates for now but could be a date)
     * Depending on the field, this is an i18n value.
     */
    static final String SCHEMA_SPEC_MAXIMUM = "M";  // Maximum number value or minimum string length

    /**
     * A displayable name for this field.
     * Should be obtained from i18n service.
     */
    static final String SCHEMA_SPEC_DISPLAY_NAME = "E";

    /**
     * The validator function.
     */
    static final String SCHEMA_SPEC_VALIDATOR = "V";

    /**
     * Validation data. An optional linked hashed map used by the
     * validator to validate this field.
     * Depending on the field, this is an i18n value.
     */
    static final String SCHEMA_SPEC_VALIDATION_MAP = "DT";

    /**
     * A data type.
     * @see com.eweware.service.base.store.dao.schema.type.SchemaDataType
     */
    static final String SCHEMA_SPEC_DATA_TYPE = "T";

    /**
     * A boolean. If true, then the default field contains a default value.
     * If false, don't use the default field value (if any).
     */
    static final String SCHEMA_SPEC_HAS_DEFAULT_VALUE = "D";

    /**
     * The default value for this field. Used when the
     * DAO is explicitly initialized to defaults by the client.
     * Depending on the field, this is an i18n value.
     * @see com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#initToDefaultValues(com.eweware.service.base.i18n.LocaleId)
     */
    static final String SCHEMA_SPEC_DEFAULT_VALUE = "Di";

}