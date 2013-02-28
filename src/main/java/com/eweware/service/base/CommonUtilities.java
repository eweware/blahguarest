package main.java.com.eweware.service.base;

import main.java.com.eweware.service.base.error.SystemErrorException;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/22/12 Time: 3:52 PM
 */
public final class CommonUtilities {


    public static final Double getValueAsDouble(Object val) throws SystemErrorException {
        if (val == null) return 0.0d;
        if (val instanceof Double) {
            return (Double)val;
        }
        if (val instanceof Integer) {
            return new Double(((Integer) val).intValue());
        }
        if (val instanceof Long) {
            return new Double(((Long) val).doubleValue());
        }
        if (val instanceof String) {
            return Double.parseDouble((String) val);
        }
        throw new SystemErrorException("Can't handle value=" + val);
    }

    public static final Long getValueAsLong(Object val) throws SystemErrorException {
        if (val == null) return 0l;
        if (val instanceof Long) {
            return (Long) val;
        }
        if (val instanceof Double) {
            return new Long(Math.round((Double) val));
        }
        if (val instanceof Integer) {
            return new Long(((Integer) val).intValue());
        }
        if (val instanceof String) {
            return Long.parseLong((String) val);
        }
        throw new SystemErrorException("Can't handle value=" + val);
    }

    public static final Integer getValueAsInteger(Object val) throws SystemErrorException {
        if (val == null) return 0;
        if (val instanceof Integer) {
            return (Integer) val;
        }
        if (val instanceof Double) {
            return new Integer(((Double) val).intValue());
        }
        if (val instanceof Long) {
            return new Integer(((Long)val).intValue());
        }
        if (val instanceof String) {
            return Integer.parseInt((String)val);
        }
        throw new SystemErrorException("Can't handle value=" + val);
    }

    public static boolean isEmptyString(String string) {
        return (string == null || string.length() == 0);
    }

    /**
     * Returns true if the string is within specs
     * @param string    The string
     * @param minimumLength A minimum
     * @param maximumLength A maximum
     * @return  True if the string is within the maximum and minimum.
     * Returns false if string is null.
     */
    public static boolean checkString(String string, int minimumLength, int maximumLength) {
        if (string == null) {
            return false;
        }
        final int len = string.length();
        return (len <= maximumLength && len >= minimumLength);
    }
}
