package main.java.com.eweware.service;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;

/**
 * @author rk@post.harvard.edu
 */
public final class GeneralUtilities {

    /**
	 * Ensures that if a value is non-null, it is in {-1, 0, 1}.
     * Returns the value or 0 if the value is null.
	 *
     * @param value The value to test
     * @param entity The entity to use in case of value is not within limits
     * @return	Integer	Returns the value. If the value is null, it returns 0.
     * @throws InvalidRequestException  Thrown if the value is not null and is not in {-1, 0, 1}
	 **/
	public static Integer checkDiscreteValue(Integer value, Object entity) throws InvalidRequestException {
		if (value != null) {
			final int val = value.intValue();
			if (val != 1 && val != -1 && val != 0) {
				throw new InvalidRequestException("value="+value+" must be either -1, 0 or 1", entity, ErrorCodes.INVALID_INPUT);
			}
            return value;
        } else {
            return 0;
        }
	}

	/**
	 * Ensures that integer value is within a range.
     * Returns the vaule or 0 if the value is null.
     *
	 * @param value
	 * @param min The exclusive minimum
	 * @param max The exclusive maximum
	 * @param entity
	 * @return Integer Returns the supplied value.
	 * @throws InvalidRequestException Thrown if the value is not null and is not within range.
	 */
	public static Integer checkValueRange(Integer value, int min, int max, Object entity) throws InvalidRequestException {
        if (value != null) {
            final int val = value.intValue();
            if ((val < min) || (val > max)) {
                throw new InvalidRequestException("value " + value + " out of range: must be between " + min + " and " + max, entity, ErrorCodes.INVALID_INPUT);
            }
            return value;
        } else {
            return 0;
        }
    }

    /**
     * Returns the integer value if it is not null or else the default value
     * @param integer   The integer value
     * @param defaultValue  The default value
     * @return  An integer value (either the integer or the defaultValue)
     */
    public static Integer safeGetInteger(Integer integer, Integer defaultValue) {
        return (integer != null) ? integer : defaultValue;
    }
}
