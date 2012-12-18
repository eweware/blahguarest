package main.java.com.eweware.service.base.store.dao.schema.type;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:43 PM
 *
 *         Used to map field names to their respective type
 */
public class SchemaDataTypeFieldMap {

    private final SchemaDataType dataType;
    private final String[] fieldNames;

    public SchemaDataTypeFieldMap(SchemaDataType dataType, String[] fieldNames) {
        this.dataType = dataType;
        this.fieldNames = fieldNames;
    }

    public SchemaDataType getDataType() {
        return dataType;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }
}
