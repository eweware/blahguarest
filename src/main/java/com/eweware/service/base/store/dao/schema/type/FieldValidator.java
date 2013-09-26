package com.eweware.service.base.store.dao.schema.type;

import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.store.dao.schema.SchemaSpec;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 10:52 AM
 */
public interface FieldValidator {

    /**
     * Assumes that the specified value is valid (see isValid())
     * and, if necessary, converts the value to an appropriate
     * type that is internally stored by the DAO.
     *
     * @param value A valid field value.
     * @param spec The field's schema specification.
     * @return Object   Returns a value suitable for storage in the DAO.
     */
    abstract Object toValidValue(Object value, SchemaSpec spec) throws SystemErrorException;

    /**
     * Verifies whether the specified field is valid.
     * @param value A value for this field. This value may be represented
     *              by any type of object for which toValidValue() can
     *              return a value. For example, a date value might be accepted as valid
     *              as a String or a Date representation. However, the actual
     *              representation of the date stored in the DB is
     *              obtained by using the toValidValue() method.
     * @param spec The field's schema specification.
     * @return boolean  Returns true if the value is valid.
     */
    abstract boolean isValid(Object value, SchemaSpec spec);
}
