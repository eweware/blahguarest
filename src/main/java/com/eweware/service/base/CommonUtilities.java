package main.java.com.eweware.service.base;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.payload.CommentPayload;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.UserProfileDAO;
import main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/22/12 Time: 3:52 PM
 */
public final class CommonUtilities {


    private static final Logger logger = Logger.getLogger(CommonUtilities.class.getName());

    public static final Double getValueAsDouble(Object val) throws SystemErrorException {
        if (val == null) return 0.0d;
        if (val instanceof Double) {
            return (Double)val;
        }
        try {
            if (val instanceof Integer) {
                return new Double(((Integer) val).intValue());
            }
            if (val instanceof Long) {
                return new Double(((Long) val).doubleValue());
            }
            if (val instanceof String) {
                return Double.parseDouble((String) val);
            }
        } catch (NumberFormatException e) {
            // fall through
        }
        throw new SystemErrorException("getValueAsDouble: Can't handle value=" + val);
    }

    public static Double getValueAsDouble(Object val, Double defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Double) {
            return (Double) val;
        }
        try {
            if (val instanceof Integer) {
                return ((double)((Integer)val).intValue());
            }
            if (val instanceof Long) {
                return ((double) ((Long) val).longValue());
            }
            if (val instanceof String) {
                return Double.parseDouble((String) val);
            }
        } catch (Exception e) {
            // fall through
        }
        return defaultValue;
    }

    public static final Long getValueAsLong(Object val) throws SystemErrorException {
        if (val == null) return 0l;
        if (val instanceof Long) {
            return (Long) val;
        }
        try {
            if (val instanceof Double) {
                return new Long(Math.round((Double) val));
            }
            if (val instanceof Integer) {
                return new Long(((Integer) val).intValue());
            }
            if (val instanceof String) {
                return Long.parseLong((String) val);
            }
        } catch (NumberFormatException e) {
            // fall through
        }
        throw new SystemErrorException("getValueAsLong: Can't handle value=" + val);
    }

    public static final Long getValueAsLong(Object val, Long defaultValue) {
        if (val instanceof Long) {
            return (Long) val;
        }
        try {
            if (val instanceof Double) {
                return new Long(Math.round((Double) val));
            }
            if (val instanceof Integer) {
                return new Long(((Integer) val).intValue());
            }
            if (val instanceof String) {
                return Long.parseLong((String) val);
            }
        } catch (Exception e) {
            // fall through
        }
        return defaultValue;
    }

    public static List<Long> getListAsLongs(Object obj, List<Long> defaultList) {
        if (obj == null) {return defaultList;}
        if (!(obj instanceof List<?>)) {
            return defaultList;
        }
        final List<?> list = (List<?>) obj;
        if (list.size() == 0) {
            return new ArrayList<Long>(0);
        }
        final List<Long> result = new ArrayList<Long>(list.size());
        for (Object item : list) {
            result.add(getValueAsLong(item, 0L));
        }
        return result;
    }

    public static final Integer getValueAsInteger(Object val) throws SystemErrorException {
        if (val == null) return 0;
        if (val instanceof Integer) {
            return (Integer) val;
        }
        try {
            if (val instanceof Double) {
                return new Integer(((Double) val).intValue());
            }
            if (val instanceof Long) {
                return new Integer(((Long)val).intValue());
            }
            if (val instanceof String) {
                return Integer.parseInt((String)val);
            }
        } catch (NumberFormatException e) {
            // fall through
        }
        throw new SystemErrorException("getValueAsInteger: Can't handle value=" + val);
    }

    public static final Integer getValueAsInteger(Object val, Integer defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Integer) {
            return (Integer) val;
        }
        try {
            if (val instanceof Long) {
                return ((Long) val).intValue();
            }
            if (val instanceof Double) {
                return new Long(Math.round((Double) val)).intValue();
            }
            if (val instanceof String) {
                return Integer.parseInt((String) val);
            }
        } catch (Exception e) {
            // fall through
        }
        return defaultValue;
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
     * <p>Returns plain text from potentially marked up HTML text or escaped HTML.</p>
     *
     * @param maybeMarkedUpText  The text to clean  up.
     * @return  The plain text (HTML tag data and compromising characters stripped out).
     */
    public static String scrapeMarkup(String maybeMarkedUpText) throws SystemErrorException {
        if (maybeMarkedUpText == null || maybeMarkedUpText.length() == 0) {
            return maybeMarkedUpText;
        }
        maybeMarkedUpText = maybeMarkedUpText.replaceAll("<", "&#60;");
        return maybeMarkedUpText.replaceAll(">", "&#62;");
//
//        try {
//
//            // First, unescape any escaped HTML
//            final String text = StringEscapeUtils.unescapeHtml4(maybeMarkedUpText);
//
//            if (!text.equals(maybeMarkedUpText)) {
//                logger.warning("Client sent escaped HTML text: " + maybeMarkedUpText);
//            }
//
//            // Now filter out any HTML markup
//            final InputStream input = IOUtils.toInputStream(text);
//            ContentHandler handler = new BodyContentHandler();
//            Metadata metadata = new Metadata();
//            new HtmlParser().parse(input, handler, metadata, new ParseContext());
//            return handler.toString();
//        } catch (Exception e) {
//            throw new SystemErrorException("Problem evaluating marked up text", e, ErrorCodes.INVALID_TEXT_INPUT);
//        }
    }

    public static void maybeAddUserNickname(StoreManager storeMgr, boolean authenticated, String commentAuthorId, CommentPayload commentPayload) throws SystemErrorException {
        final String nickname = maybeGetUserNickname(storeMgr, authenticated, commentAuthorId);
        if (nickname != null) {
            commentPayload.setUserNickname(nickname);
        }
    }

    public static String maybeGetUserNickname(StoreManager storeMgr, boolean authenticated, String userId) throws SystemErrorException {
        final UserProfileDAO userProfile = (UserProfileDAO) storeMgr.createUserProfile(userId)._findByPrimaryId(UserProfileDAO.USER_PROFILE_NICKNAME, UserProfileDAO.USER_PROFILE_NICKNAME_PERMISSIONS);
        if (userProfile != null) {
            final Integer nicknamePermissions = userProfile.getNicknamePermissions();
            if ((nicknamePermissions != null) &&
                    ((nicknamePermissions == UserProfilePermissions.PUBLIC.getCode()) ||
                            ((nicknamePermissions == UserProfilePermissions.MEMBERS.getCode()) && authenticated))) {
                final String nickname = userProfile.getNickname();
                return nickname;
            }
        }
        return null;
    }

    /**
	 * Ensures that if a value is non-null, it is in {-1, 0, 1}.
     * Returns the value or 0 if the value is null.
	 *
     * @param value The value to test
     * @param entity The entity to use in case of value is not within limits
     * @return	Integer	Returns the value. If the value is null, it returns 0.
     * @throws main.java.com.eweware.service.base.error.InvalidRequestException  Thrown if the value is not null and is not in {-1, 0, 1}
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
    public static Long checkDiscreteValue(Long value, Object entity) throws InvalidRequestException {
        if (value != null) {
            final long val = value.longValue();
            if (val != 1L && val != -1L && val != 0L) {
                throw new InvalidRequestException("value="+value+" must be either -1, 0 or 1", entity, ErrorCodes.INVALID_INPUT);
            }
            return value;
        } else {
            return 0L;
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
	 * @throws main.java.com.eweware.service.base.error.InvalidRequestException Thrown if the value is not null and is not within range.
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
    public static Long checkValueRange(Long value, long min, long max, Object entity) throws InvalidRequestException {
        if (value != null) {
            final long val = value.longValue();
            if ((val < min) || (val > max)) {
                throw new InvalidRequestException("value " + value + " out of range: must be between " + min + " and " + max, entity, ErrorCodes.INVALID_INPUT);
            }
            return value;
        } else {
            return 0L;
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

    /**
     * Returns the long value if it is not null or else the default value
     * @param longVal   The long value
     * @param defaultValue  The default value
     * @return  An long value (either the long or the defaultValue)
     */
    public static Long safeGetLong(Long longVal, Long defaultValue) {
        return (longVal != null) ? longVal : defaultValue;
    }

    /**
     * <p>Returns the name of the collection in the blahdb containing the
     * inbox items for the specified group and inbox number.</p>
     * @param groupId   The group id
     * @param inboxNumber The inbox number
     * @return  Returns the collection name of the inbox
     */
    public static String makeInboxCollectionName(String groupId, Integer inboxNumber) {
        final StringBuilder inboxCollectionName = new StringBuilder(groupId);
        inboxCollectionName.append("-");
        inboxCollectionName.append(String.format("%07d", inboxNumber));
        return inboxCollectionName.toString();
    }

    /**
     * <p>Returns the name of the inbox collection holding recent blahs for the specified group.</p>
     * @param groupId The id of the group.
     * @return The recents inbox collection name for the group
     */
    public static String makeRecentsInboxCollectionName(String groupId) {
        return groupId;
    }

//    public static void main(String[] s) throws SystemErrorException {
//        final String t = scrapeMarkup("<html><p>hello</p><p>there</p>\n\n\n\n\n\nHello there.\n" +
//                "&nbsp;    &#933;&#933; People of the world.\n" +
//                "<a href=\"rubenkleiman.com\">Ruben</a>\n");
//        String p = scrapeMarkup("<html><input type='button' value='Click Test 2' onclick='alert(\"I just stole your wallet!\");' /></html>");
//        String a = scrapeMarkup("%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20The%20Beatles%20will%20re-unite%20for%20a%20final%20send-off%20concert%20when%20the%20Messiah%20arrives.");
//        String b = scrapeMarkup("%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20<b>The%20Beatles</b>%20will%20re-unite%20for%20a%20final%20send-off%20concert%20when%20the%20Messiah%20arrives.");
//        String b = scrapeMarkup("%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20&gt;b&lt;The%20Beatles&gt;/b&lt;%20will%20re-unite%20for%20a%20final%20send-off%20concert%20when%20the%20Messiah%20arrives.");
//        System.out.println(p);
//    }
}
