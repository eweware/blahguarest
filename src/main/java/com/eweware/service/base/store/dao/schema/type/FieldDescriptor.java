package main.java.com.eweware.service.base.store.dao.schema.type;

/**
 * <p>Describes a field. A transient object.</p>
 * @author rk@post.harvard.edu
 *         Date: 3/11/13 Time: 1:21 PM
 */
public class FieldDescriptor {

    private final String fieldName;
    private final FieldValidator validator;

    public FieldDescriptor(String fieldName) {
        this(fieldName, null);
    }
    public FieldDescriptor(String fieldName, FieldValidator validator) {
        this.fieldName = fieldName;
        this.validator = validator;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldValidator getValidator() {
        return validator;
    }
}
