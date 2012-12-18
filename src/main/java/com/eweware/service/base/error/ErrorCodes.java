package main.java.com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/2/12 Time: 1:49 PM
 */
public final class ErrorCodes {


    public static final Integer SERVER_NOT_INITIALIZED = 1;
    public static final Integer MISSING_DISPLAY_NAME = 2;
    public static final Integer MISSING_GROUP_TYPE_ID = 3;
    public static final Integer ALREADY_EXISTS_GROUP_TYPE_WITH_DISPLAY_NAME = 4;
    public static final Integer NOTHING_TO_UPDATE = 5;
    public static final Integer NOT_FOUND_GROUP_TYPE_ID = 6;
    public static final Integer ALREADY_EXISTS_GROUP_WITH_DISPLAY_NAME = 7;
    public static final Integer MISSING_GROUP_ID = 8;
    public static final Integer INVALID_STATE_CODE = 9;
    public static final Integer NOT_FOUND_GROUP_ID = 10;
    public static final Integer ALREADY_EXISTS_USER_WITH_DISPLAY_NAME = 11;
    public static final Integer MISSING_EMAIL_ADDRESS = 12;
    public static final Integer MISSING_USER_ID = 13;
    public static final Integer NOT_FOUND_USER_ID = 14;

    public static final Integer USER_ALREADY_JOINED_GROUP = 15;
    public static final Integer VALIDATION_EMAIL_NOT_SENT_DUE_TO_MAIL_SYSTEM_ERROR = 16;
    public static final Integer MISSING_VALIDATION_CODE = 17;
    public static final Integer VALIDATION_CODE_INVALID_OR_EXPIRED = 18;
    public static final Integer INVALID_STATE_CODE_IS_NEITHER_P_NOR_S = 19;
    public static final Integer USER_HAS_NOT_JOINED_GROUP = 20;
    public static final Integer INVALID_ACTION_CODE = 21;
    public static final Integer INVALID_STATE_USER_CANNOT_JOIN_INACTIVE_GROUP = 22;
    public static final Integer USER_CANNOT_BE_ACTIVATED_WHEN_STATE_IS_NOT_P_OR_S = 23;
    public static final Integer USER_CANNOT_BE_SUSPENDED_IN_STATE_OTHER_THAN_A = 24;
    public static final Integer USER_MUST_INITIALLY_JOIN_GROUP_IN_STATE_P = 25;
    public static final Integer SERVER_INDEXING_ERROR = 26;
    public static final Integer MISSING_TEXT = 27;
    public static final Integer MISSING_BLAH_TYPE_ID = 28;
    public static final Integer USER_NOT_ACTIVE_IN_GROUP = 29;
    public static final Integer CANNOT_EDIT_TEXT = 30;
    public static final Integer MISSING_BLAH_ID = 31;
    public static final Integer MISSING_AUTHOR_ID = 32;
    public static final Integer NOT_FOUND_BLAH_ID = 33;
    public static final Integer USER_CANNOT_UPDATE_ON_OWN_BLAH = 34;
    public static final Integer USER_ALREADY_VOTED_ON_BLAH_ID = 35;
    public static final Integer MISSING_COMMENT_ID = 36;
    public static final Integer NOT_FOUND_COMMENT_ID = 37;
    public static final Integer CANNOT_VOTE_ON_COMMENT_WHEN_CREATING_IT = 38;
    public static final Integer CANNOT_VOTE_ON_BLAH_WHEN_UPDATING_COMMENT = 39;
    public static final Integer USER_CANNOT_VOTE_ON_COMMENTS_TO_ONES_OWN_BLAH = 40;
    public static final Integer USER_ALREADY_VOTED_FOR_COMMENT = 41;
    public static final Integer USER_CANNOT_VOTE_ON_OWN_COMMENT = 42;
    public static final Integer MEDIA_NOT_FOUND = 43;
    public static final Integer FAILED_TRACKER_INSERT = 44;
    public static final Integer INVALID_MONTH = 45;
    public static final Integer INVALID_YEAR = 46;
    public static final Integer SERVER_SEVERE_ERROR = 47;
    public static final Integer INVALID_DATE = 48;
    public static final Integer SERVER_RECOVERABLE_ERROR = 49;
    public static final Integer INVALID_INPUT = 50;
    public static final Integer INVALID_USER_VALIDATION_PARAMS = 51;
    public static final Integer INVALID_EMAIL_ADDRESS = 52;
    public static final Integer MISSING_INPUT_PAYLOAD = 53;
    public static final Integer NOT_FOUND_USER_PROFILE = 54;
    public static final Integer ALREADY_EXISTS_USER_PROFILE = 55;
    public static final Integer MISSING_AUTHORIZATION_STATE = 56;
    public static final Integer SERVER_CONFIGURATION_ERROR = 57;
    public static final Integer REQUEST_NOT_GRANTED = 58;
    public static final Integer MISSING_QUERY_PARAMETER = 59;
    public static final Integer SERVER_DB_ERROR = 60;
    public static final Integer DUPLICATE_KEY = 61;
    public static final Integer SERVER_CACHE_ERROR = 62;
    public static final Integer INVALID_UPDATE = 63;
    public static final Integer UNSUPPORTED_MEDIA_TYPE = 64;

}
