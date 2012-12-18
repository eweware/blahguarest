package main.java.com.eweware.service.base.store.dao.schema;

public interface SchemaConstants {

    // Schema spec field names
    static final String SCHEMA_SPEC_FIELD_ID = "fid";
    static final String SCHEMA_SPEC_REGEXP = "R";  // Obtained from i18n service
    static final String SCHEMA_SPEC_MINIMUM = "m"; // Minimum number value or minimum string length
    static final String SCHEMA_SPEC_MAXIMUM = "M";  // Maximum number value or minimum string length
    static final String SCHEMA_SPEC_DISPLAY_NAME = "E";  // Obtained from i18n service
    static final String SCHEMA_SPEC_DATA = "DT";   // Obtained from i18n service
    static final String SCHEMA_SPEC_DATA_TYPE = "T";
    static final String SCHEMA_SPEC_DEFAULT_VALUE = "Di"; // always an i18n-independent value

}