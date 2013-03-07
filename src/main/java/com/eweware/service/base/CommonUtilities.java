package main.java.com.eweware.service.base;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import org.apache.commons.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

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

    public static final long getAgeInYears(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) <= dob.get(Calendar.DAY_OF_YEAR))
            age--;
        return age;
//        final long time = System.currentTimeMillis() - dateOfBirth.getTime();
//        final Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(time);
//        return cal.get(Calendar.YEAR);
    }

    /**
     * <p>Returns plain text from potentially marked up HTML text.</p>
     *
     * @param maybeMarkedUpText  The text to clean  up.
     * @return  The plain text (HTML tag data and compromising characters stripped out).
     */
    public static String getPlainText(String maybeMarkedUpText) throws SystemErrorException {
        try {
            final InputStream input = IOUtils.toInputStream(maybeMarkedUpText);
            ContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            new HtmlParser().parse(input, handler, metadata, new ParseContext());
            String plainText = handler.toString();
            return plainText;
        } catch (Exception e) {
            throw new SystemErrorException("Problem evaluation marked up text", e, ErrorCodes.INVALID_TEXT_INPUT);
        }
    }

//    public static void main(String[] s) {
//        System.out.println(getPlainText("<html><p>hello</p><p>there</p>\n\n\n\n\n\nHello there.\n" +
//                "&nbsp;    &#933;&#933; People of the world.\n" +
//                "<a href=\"rubenkleiman.com\">Ruben</a>\n"));
//    }


}
