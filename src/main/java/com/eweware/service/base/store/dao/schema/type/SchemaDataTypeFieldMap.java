package com.eweware.service.base.store.dao.schema.type;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:43 PM
 *
 *         Used to map field names to their respective type
 */
public class SchemaDataTypeFieldMap {

    private final SchemaDataType dataType;
    private final FieldDescriptor[] fieldDescriptors;

    public SchemaDataTypeFieldMap(SchemaDataType dataType, String[] fieldNames) {
        final FieldDescriptor[] descriptors = new FieldDescriptor[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            descriptors[i] = new FieldDescriptor(fieldNames[i], null);
        }
        this.dataType = dataType;
        this.fieldDescriptors = descriptors;
    }

    public SchemaDataTypeFieldMap(SchemaDataType dataType, FieldDescriptor[] fieldDescriptors) {
        this.dataType = dataType;
        this.fieldDescriptors = fieldDescriptors;
    }

    public SchemaDataType getDataType() {
        return dataType;
    }

    public FieldDescriptor[] getFieldDescriptors() {
        return fieldDescriptors;
    }
}
