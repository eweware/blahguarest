package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BaseDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.type.DefaultEmbeddedDataTypeValidator;
import main.java.com.eweware.service.base.store.dao.schema.type.FieldDescriptor;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *         <p/>
 *         1. Fetch the DB schema using a version number from the xDAOConstants interface. The DB schema is i18n-independent.
 *         2. Validate that expected field ids are in DB schema representation (this should really be pre-validated, but this is a sanity check during dev't)
 *         3. This is the expected DB schema:
 *         {"version": "1.0",
 *         "fields": ["field-id":  // SchemaConstants.SCHEMA_SPEC_FIELD_ID
 *         {
 *         "N": nature, // SchemaConstants.SCHEMA_SPEC_DISPLAY_NAME
 *         "dataType": data-type-value,
 *         "defaultValue": nature,
 *         "R": nature,  // SCHEMA_SPEC_REGEXP
 *         "min": min-value, "max": max-value,
 *         "data": nature
 *         },
 *         ...]
 *         }
 *         Notes:    1. nature:  A string value of ".0." means that this field is not used
 *         A string value of ".1." means that this field must be obtained from the i18n string service
 *         Any other value means that the value is a literal (for example, an i18n-independent regexp)
 *         2. If a field is absent, it is not needed (e.g., min and max for some data types)
 *         3. Instead of "displayName", etc..., the actual field names are the same
 *         as the ones in the schema constants class (e.g., SchemaConstants.SCHEMA_SPEC_DISPLAY_NAME = "N")
 *         <p/>
 *         <p/>
 *         i18n: Right now, we use the "nature" field to simplify i18n for US while
 *         making it easy to extend it later. We never have .1. nature in the schema DB: that code
 *         left for later use. This allows us to directly fetch the en_US strings
 *         from the DB schema: works great on the one-country scenario.
 *         When we expand to other countries, we'll simply replace the English data in the schema DB
 *         with the .1. nature code and look up the i18n
 *         database (TBD) for the required localized values.
 *
 * @author rk@post.harvard.edu
 *         Date: 9/13/12 Time: 1:21 PM
 *         <p/>
 *         TODO Loading of subclass schema should be data-driven from DB.
 *         <p/>
 */
public abstract class BaseSchema implements SchemaConstants {

    // TODO  specs could indicate desired Index and Store values

    /**
     * Name of static method used to obtain the schema.
     */
    public static final String GET_SCHEMA_METHOD_NAME = "getSchema";

    // TODO this is a cheap one-level inheritance: generalize when necessary
    // Maps a base field name to its spec
    private static final Map<String, SchemaSpec> baseFieldNameToSpecMap = new HashMap<String, SchemaSpec>();

    static {

        // Mongo ID field: this should never have a default value
        final SchemaSpec idSpec = new SchemaSpec();
        idSpec.setFieldId(BaseDAOConstants.ID);
        idSpec.setDataType(SchemaDataType.DB_OBJECT_ID);
        baseFieldNameToSpecMap.put(BaseDAOConstants.ID, idSpec);

        // Doesn't need a default: _insert is responsible for setting it
        final SchemaSpec createdSpec = new SchemaSpec();
        createdSpec.setFieldId(BaseDAOConstants.CREATED);
        createdSpec.setDataType(SchemaDataType.DT);
        baseFieldNameToSpecMap.put(BaseDAOConstants.CREATED, createdSpec);

        // Doesn't need a default: _insert is responsible for setting it
        final SchemaSpec updatedSpec = new SchemaSpec();
        updatedSpec.setFieldId(BaseDAOConstants.UPDATED);
        updatedSpec.setDataType(SchemaDataType.DT);
        baseFieldNameToSpecMap.put(BaseDAOConstants.UPDATED, updatedSpec);

        // Doesn't need a default: get for boolean returns false if field is not set
        final SchemaSpec deletedSpec = new SchemaSpec();
        deletedSpec.setFieldId(BaseDAOConstants.IS_DELETED);
        deletedSpec.setDataType(SchemaDataType.B);
        baseFieldNameToSpecMap.put(BaseDAOConstants.IS_DELETED, deletedSpec);
    }

    // Maps each field name for an instance to its spec
    private final Map<String, SchemaSpec> fieldNameToSpecMap = new Hashtable<String, SchemaSpec>(); // TODO this needs to be a readable but sync'd table

    // Maps a schema class to the schemas for each supported locale id. TODO this is a lame cache.
    private static final Map<Class<? extends BaseSchema>, Map<LocaleId, BaseSchema>> schemaClassToSchemasMap = new HashMap<Class<? extends BaseSchema>, Map<LocaleId, BaseSchema>>();

    /**
     * Returns the cached schema for the specified locale
     *
     * @param schemaClass The class to which the schema belongs
     * @param localeId    The locale id.
     * @return BaseSchema The schema for this locale or null if it is not available.
     */
    protected static BaseSchema getCachedSchema(Class<? extends BaseSchema> schemaClass, LocaleId localeId) {
        final Map<LocaleId, BaseSchema> localeToSchemaMap = schemaClassToSchemasMap.get(schemaClass);
        // TODO localeId lookup should comply with bcp47 rules!
        return (localeToSchemaMap == null) ? null : localeToSchemaMap.get(localeId);
    }

    /**
     * Caches the schema for the specified schema class and locale.
     *
     * @param schemaClass The subclass of BaseSchema
     * @param localeId    The locale id
     * @param schema      The schema
     */
    protected static void cacheSchema(Class<? extends BaseSchema> schemaClass, LocaleId localeId, BaseSchema schema) {
        // TODO localeId lookup should comply with bcp47 rules!
        Map<LocaleId, BaseSchema> localeToSchemaMap = schemaClassToSchemasMap.get(schemaClass);
        if (localeToSchemaMap == null) {
            localeToSchemaMap = new HashMap<LocaleId, BaseSchema>();
            schemaClassToSchemasMap.put(schemaClass, localeToSchemaMap);
        }
        localeToSchemaMap.put(localeId, schema);
    }

    private LocaleId localeId;

    // Schema version number
    private String version = "1.0";

    private BaseSchema() {
    } // not allowed

    protected BaseSchema(LocaleId localeId) {
        this.localeId = localeId;
        fieldNameToSpecMap.putAll(baseFieldNameToSpecMap);
    }

    protected LocaleId getLocaleId() {
        return localeId;
    }

    protected String getVersion() {
        return version;
    }

    public SchemaSpec getSpec(String fieldName) {
        return fieldNameToSpecMap.get(fieldName);
    }

    // Must override
    public Map<String, SchemaSpec> getFieldNameToSpecMap() {
        return fieldNameToSpecMap;
    }

    /**
     * Used to create a simple schema spec.
     *
     *
     * @param dataType The type of data
     * @param fieldDescriptor The field's descriptor
     * @param displayName   The display name for this field
     * @param validator   An optional validator/converter for this field
     * @param regexp    An optional regexp for validating this field
     * @param validationMap An optional linked hash map for validating this field
     * @param hasDefaultValue If true, then the defaultValue is to be used to initialize the DAO
     * @param defaultValue  An optional default value. If hasDefaultValue is true and defaultValue is null,
*                      the field <i>will</i> be initialized to new     @return  A simple schema spec for a field
     * @see SchemaConstants for further explanation of fields
     * TODO Generalize to use a validation class instead of a map and regexp. https://eweware.atlassian.net/browse/WRS-241
     */
    public SchemaSpec createSpec(SchemaDataType dataType, FieldDescriptor fieldDescriptor, String displayName, DefaultEmbeddedDataTypeValidator validator, String regexp, LinkedHashMap<String, Object> validationMap, boolean hasDefaultValue, Object defaultValue) {
        final SchemaSpec spec = new SchemaSpec();
        spec.setFieldId(fieldDescriptor.getFieldName());
        if (displayName != null) {
            spec.setDisplayName(displayName);
        }
        if (fieldDescriptor.getValidator() != null) {
            spec.setValidator(fieldDescriptor.getValidator());
        }
        if (regexp != null) {
            spec.setValidationRegexp(regexp);
        }
        spec.setDataType(dataType);
        if (hasDefaultValue) {
            spec.setDefaultValue(defaultValue);
        }
        if (validationMap != null) {
            spec.setValidationMap(validationMap);
        }
        getFieldNameToSpecMap().put(fieldDescriptor.getFieldName(), spec);
        return spec;
    }

    /**
     * <p>Creates simple specs for specified field types. This is a convenience method.
     * We default all fields to null.</p>
     * @param schema     The schema
     * @param fieldTypes An array of maps from field names to their schema data types.
     */
    protected static void createSimpleFieldSpecs(BaseSchema schema, SchemaDataTypeFieldMap[] fieldTypes) {
        for (SchemaDataTypeFieldMap map : fieldTypes) {
            final SchemaDataType dataType = map.getDataType();
            for (FieldDescriptor descriptor: map.getFieldDescriptors()) {
                schema.createSpec(dataType, descriptor, null, null, null, null, false, null); // all fields default to null
            }
        }
    }
}
