package com.eweware.service.base.store.dao.schema.type;

import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.store.dao.schema.SchemaSpec;
import com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import org.bson.types.ObjectId;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/17/12 Time: 12:26 PM
 */
public class MongoObjectIdValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) throws SystemErrorException {
        if (value != null && !(value instanceof ObjectId)) {
            return MongoStoreManager.makeObjectId(value.toString());
        }
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        return (value != null);
    }
}
