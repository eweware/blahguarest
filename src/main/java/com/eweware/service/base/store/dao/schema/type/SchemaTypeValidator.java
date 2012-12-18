package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 11:19 AM
 *
 *         This validates an embedded object. For now, it's a NOOP (i.e., we
 *         ignore the schema) since the only cases are readonly data (e.g., trackers).
 *         TODO when supported, methods should take the schema itself
 */
public class SchemaTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) throws SystemErrorException {
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        return true;
    }
}
