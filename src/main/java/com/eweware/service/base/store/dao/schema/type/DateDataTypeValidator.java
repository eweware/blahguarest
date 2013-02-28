package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;

import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 11:14 AM
 */
public class DateDataTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) throws SystemErrorException {
        if (value instanceof String) { // expecting an ISO-formatted date string
            try {
                return DateUtils.fromISODateToUTC((String) value);
            } catch (ParseException e) { // should not happen if field was first validated
                throw new SystemErrorException("invalid value for " + main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType.D.getDescription(), e, ErrorCodes.SERVER_RECOVERABLE_ERROR);
            }
        }
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        if (value == null) {
            return true; // a null value is a command to delete the field or set the field's value to null
        } else if (value instanceof String) {
            final String regexp = spec.getValidationRegexp();
            if ((regexp == null) || (Pattern.compile(regexp).matcher((String) value)).matches()) {
                try {
                    DateUtils.fromISODateToUTC((String) value);
                    return true;
                } catch (ParseException e) {
                    return false;
                }
            }
        }
        return false;
    }
}
