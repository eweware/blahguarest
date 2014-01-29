package com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/2/12 Time: 1:49 PM
 */
public final class ErrorCodes {


    /**
     * SERVER_NOT_INITIALIZED = 1
     */
    public static final Integer SERVER_NOT_INITIALIZED = 1;

    /**
     * <p> MISSING_USERNAME = 2</p>
     */
    public static final Integer MISSING_USERNAME = 2;

    /**
     * MISSING_GROUP_TYPE_ID = 3
     */
    public static final Integer MISSING_GROUP_TYPE_ID = 3;

    /**
     * ALREADY_EXISTS_GROUP_TYPE_WITH_DISPLAY_NAME = 4
     */
    public static final Integer ALREADY_EXISTS_GROUP_TYPE_WITH_DISPLAY_NAME = 4;

    /**
     * NOTHING_TO_UPDATE = 5
     */
    public static final Integer NOTHING_TO_UPDATE = 5;

    /**
     * NOT_FOUND_GROUP_TYPE_ID = 6
     */
    public static final Integer NOT_FOUND_GROUP_TYPE_ID = 6;

    /**
     * ALREADY_EXISTS_GROUP_WITH_DISPLAY_NAME = 7
     */
    public static final Integer ALREADY_EXISTS_GROUP_WITH_DISPLAY_NAME = 7;

    /**
     * MISSING_GROUP_ID = 8
     */
    public static final Integer MISSING_GROUP_ID = 8;

    /**
     * INVALID_STATE_CODE = 9
     */
    public static final Integer INVALID_STATE_CODE = 9;

    /**
     * NOT_FOUND_GROUP_ID = 10
     */
    public static final Integer NOT_FOUND_GROUP_ID = 10;

    /**
     * ALREADY_EXISTS_USER_WITH_USERNAME = 11
     */
    public static final Integer ALREADY_EXISTS_USER_WITH_USERNAME = 11;

    /**
     * MISSING_EMAIL_ADDRESS = 12
     */
    public static final Integer MISSING_EMAIL_ADDRESS = 12;

    /**
     * MISSING_USER_ID = 13
     */
    public static final Integer MISSING_USER_ID = 13;

    /**
     * NOT_FOUND_USER_ID = 14
     */
    public static final Integer NOT_FOUND_USER_ID = 14;

    /**
     * USER_ALREADY_JOINED_GROUP = 15
     */
    public static final Integer USER_ALREADY_JOINED_GROUP = 15;

    /**
     * VALIDATION_EMAIL_NOT_SENT_DUE_TO_MAIL_SYSTEM_ERROR = 16
     */
    public static final Integer VALIDATION_EMAIL_NOT_SENT_DUE_TO_MAIL_SYSTEM_ERROR = 16;

    /**
     * MISSING_VALIDATION_CODE = 17
     */
    public static final Integer MISSING_VALIDATION_CODE = 17;

    /**
     * VALIDATION_CODE_INVALID_OR_EXPIRED = 18
     */
    public static final Integer VALIDATION_CODE_INVALID_OR_EXPIRED = 18;

    /**
     * INVALID_STATE_CODE_IS_NEITHER_P_NOR_S = 19
     */
    public static final Integer INVALID_STATE_CODE_IS_NEITHER_P_NOR_S = 19;

    /**
     * USER_HAS_NOT_JOINED_GROUP = 20
     */
    public static final Integer USER_HAS_NOT_JOINED_GROUP = 20;

    /**
     * INVALID_ACTION_CODE = 21
     */
    public static final Integer INVALID_ACTION_CODE = 21;

    /**
     * INVALID_STATE_USER_CANNOT_JOIN_INACTIVE_GROUP = 22
     */
    public static final Integer INVALID_STATE_USER_CANNOT_JOIN_INACTIVE_GROUP = 22;

    /**
     * USER_CANNOT_BE_ACTIVATED_WHEN_STATE_IS_NOT_P_OR_S = 23
     */
    public static final Integer USER_CANNOT_BE_ACTIVATED_WHEN_STATE_IS_NOT_P_OR_S = 23;

    /**
     * USER_CANNOT_BE_SUSPENDED_IN_STATE_OTHER_THAN_A = 24
     */
    public static final Integer USER_CANNOT_BE_SUSPENDED_IN_STATE_OTHER_THAN_A = 24;

    /**
     * USER_MUST_INITIALLY_JOIN_GROUP_IN_STATE_P = 25
     */
    public static final Integer USER_MUST_INITIALLY_JOIN_GROUP_IN_STATE_P = 25;

    /**
     * SERVER_INDEXING_ERROR = 26
     */
    public static final Integer SERVER_INDEXING_ERROR = 26;

    /**
     * MISSING_TEXT = 27
     */
    public static final Integer MISSING_TEXT = 27;

    /**
     * MISSING_BLAH_TYPE_ID = 28
     */
    public static final Integer MISSING_BLAH_TYPE_ID = 28;

    /**
     * USER_NOT_JOINED_GROUP = 29
     */
    public static final Integer USER_NOT_JOINED_GROUP = 29;

    /**
     * CANNOT_EDIT_TEXT = 30
     */
    public static final Integer CANNOT_EDIT_TEXT = 30;

    /**
     * MISSING_BLAH_ID = 31
     */
    public static final Integer MISSING_BLAH_ID = 31;

    /**
     * MISSING_AUTHOR_ID = 32
     */
    public static final Integer MISSING_AUTHOR_ID = 32;

    /**
     * NOT_FOUND_BLAH_ID = 33
     */
    public static final Integer NOT_FOUND_BLAH_ID = 33;

    /**
     * USER_CANNOT_UPDATE_ON_OWN_BLAH = 34
     */
    public static final Integer USER_CANNOT_UPDATE_ON_OWN_BLAH = 34;

    /**
     * USER_ALREADY_VOTED_ON_BLAH_ID = 35
     */
    public static final Integer USER_ALREADY_VOTED_ON_BLAH_ID = 35;

    /**
     * MISSING_COMMENT_ID = 36
     */
    public static final Integer MISSING_COMMENT_ID = 36;

    /**
     * NOT_FOUND_COMMENT_ID = 37
     */
    public static final Integer NOT_FOUND_COMMENT_ID = 37;

    /**
     * CANNOT_VOTE_ON_COMMENT_WHEN_CREATING_IT = 38
     */
    public static final Integer CANNOT_VOTE_ON_COMMENT_WHEN_CREATING_IT = 38;

    /**
     * CANNOT_VOTE_ON_BLAH_WHEN_UPDATING_COMMENT = 39
     */
    public static final Integer CANNOT_VOTE_ON_BLAH_WHEN_UPDATING_COMMENT = 39;

    /**
     * USER_CANNOT_VOTE_ON_COMMENTS_TO_ONES_OWN_BLAH = 40
     */
    public static final Integer USER_CANNOT_VOTE_ON_COMMENTS_TO_ONES_OWN_BLAH = 40;

    /**
     * USER_ALREADY_VOTED_FOR_COMMENT = 41
     */
    public static final Integer USER_ALREADY_VOTED_FOR_COMMENT = 41;

    /**
     * USER_CANNOT_VOTE_ON_OWN_COMMENT = 42
     */
    public static final Integer USER_CANNOT_VOTE_ON_OWN_COMMENT = 42;

    /**
     * MEDIA_NOT_FOUND = 43
     */
    public static final Integer MEDIA_NOT_FOUND = 43;

    /**
     * FAILED_TRACKER_INSERT = 44
     */
    public static final Integer FAILED_TRACKER_INSERT = 44;

    /**
     * INVALID_MONTH = 45
     */
    public static final Integer INVALID_MONTH = 45;

    /**
     * INVALID_YEAR = 46
     */
    public static final Integer INVALID_YEAR = 46;

    /**
     * SERVER_SEVERE_ERROR = 47
     */
    public static final Integer SERVER_SEVERE_ERROR = 47;

    /**
     * INVALID_DATE = 48
     */
    public static final Integer INVALID_DATE = 48;

    /**
     * SERVER_RECOVERABLE_ERROR = 49   server should retry operation
     */
    public static final Integer SERVER_RECOVERABLE_ERROR = 49; //

    /**
     * INVALID_INPUT = 50
     */
    public static final Integer INVALID_INPUT = 50;

    /**
     * INVALID_USER_VALIDATION_PARAMS = 51
     */
    public static final Integer INVALID_USER_VALIDATION_PARAMS = 51;

    /**
     * INVALID_EMAIL_ADDRESS = 52
     */
    public static final Integer INVALID_EMAIL_ADDRESS = 52;

    /**
     * MISSING_INPUT_ENTITY = 53
     */
    public static final Integer MISSING_INPUT_ENTITY = 53;

    /**
     * NOT_FOUND_USER_PROFILE = 54
     */
    public static final Integer NOT_FOUND_USER_PROFILE = 54;

    /**
     * ALREADY_EXISTS_USER_PROFILE = 55
     */
    public static final Integer ALREADY_EXISTS_USER_PROFILE = 55;

    /**
     * MISSING_AUTHORIZATION_STATE = 56
     */
    public static final Integer MISSING_AUTHORIZATION_STATE = 56;

    /**
     * SERVER_CONFIGURATION_ERROR = 57
     */
    public static final Integer SERVER_CONFIGURATION_ERROR = 57;

    /**
     * REQUEST_NOT_GRANTED = 58
     */
    public static final Integer REQUEST_NOT_GRANTED = 58;

    /**
     * MISSING_QUERY_PARAMETER = 59
     */
    public static final Integer MISSING_QUERY_PARAMETER = 59;

    /**
     * SERVER_DB_ERROR = 60
     */
    public static final Integer SERVER_DB_ERROR = 60;

    /**
     * DUPLICATE_KEY = 61
     */
    public static final Integer DUPLICATE_KEY = 61;

    /**
     * SERVER_CACHE_ERROR = 62
     */
    public static final Integer SERVER_CACHE_ERROR = 62;

    /**
     * INVALID_UPDATE = 63
     */
    public static final Integer INVALID_UPDATE = 63;

    /**
     * UNSUPPORTED_MEDIA_TYPE = 64
     */
    public static final Integer UNSUPPORTED_MEDIA_TYPE = 64;

    /**
     * INVALID_SESSION = 65
     */
    public static final Integer INVALID_SESSION = 65;

    /**
     * SESSION_ERROR = 66
     */
    public static final Integer SESSION_ERROR = 66;

    /**
     * INVALID_SESSION_STATE = 67
     */
    public static final Integer INVALID_SESSION_STATE = 67;

    /**
     * INVALID_PASSWORD = 68
     */
    public static final Integer INVALID_PASSWORD = 68;

    /**
     * INVALID_USERNAME = 69
     */
    public static final Integer INVALID_USERNAME = 69;

    /**
     * INVALID_USER_ID = 70
     */
    public static final Integer INVALID_USER_ID = 70;

    /**
     * UNAUTHORIZED_USER = 71
     */
    public static final Integer UNAUTHORIZED_USER = 71;

    /**
     * ALREADY_VOTED_ON_POLL = 72
     */
    public static final Integer ALREADY_VOTED_ON_POLL = 72;

    /**
     * NOT_FOUND_USER_ACCOUNT = 73
     */
    public static final Integer NOT_FOUND_USER_ACCOUNT = 73;

    /**
     * MISSING_GROUP_DESCRIPTOR = 74
     *
     * @see com.eweware.service.base.store.dao.GroupDAOConstants.GroupDescriptor
     */
    public static final Integer MISSING_GROUP_DESCRIPTOR = 74;

    /**
     * INVALID_GROUP_DESCRIPTOR = 75
     *
     * @see com.eweware.service.base.store.dao.GroupDAOConstants.GroupDescriptor
     */
    public static final Integer INVALID_GROUP_DESCRIPTOR = 75;

    /**
     * MISSING_DISPLAY_NAME = 76
     */
    public static final Integer MISSING_DISPLAY_NAME = 76;

    /**
     * RECOVERY_CODE_EXPIRED = 77
     */
    public static final Integer RECOVERY_CODE_EXPIRED = 77;

    /**
     * RECOVERY_CODE_INVALID = 78
     */
    public static final Integer RECOVERY_CODE_INVALID = 78;

    /**
     * INVALID_TEXT_INPUT = 79
     */
    public static final Integer INVALID_TEXT_INPUT = 79;

    /**
     * SERVER_DATA_INCONSISTENT = 80
     */
    public static final Integer SERVER_DATA_INCONSISTENT = 80; // TODO refactor: use this where appropriate

    /**
     * MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED = 81
     */
    public static final Integer MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED = 81;

    /**
     * SEVERE_AWS_ERROR = 82
     */
    public static final Integer SEVERE_AWS_ERROR = 82;

    /**
     * INVALID_DIGEST_OR_SALT = 83
     */
    public static final Integer INVALID_DIGEST_OR_SALT = 83;

    /**
     * SERVER_CRYPT_ERROR = 84
     */
    public static final Integer SERVER_CRYPT_ERROR = 84;

    /**
     * EMAIL_SYSTEM_ERROR = 85
     */
    public static final Integer EMAIL_SYSTEM_ERROR = 85;

    /**
     * INVALID_INBOX_ITEM_KEY = 86
     */
    public static final Integer INVALID_INBOX_ITEM_KEY = 86;

    /**
     * NOT_FOUND_BADGE_ID = 87
     */
    public static final Integer NOT_FOUND_BADGE_ID = 87;

    /**
     * BADGE_NOT_OWNED_BY_USER = 88
     */
    public static final Integer BADGE_NOT_OWNED_BY_USER = 88;

    /**
     * MISSING_TEXT_OR_BODY = 89
     */
    public static final Integer MISSING_TEXT_OR_BODY = 89;

    /**
     * <p>Failed to delete an image (with a media referend of user, blah, or comment.</p>
     * FAILED_TO_DELETE_IMAGE = 90
     */
    public static final Integer FAILED_TO_DELETE_IMAGE = 90;

    /**
     * <p>Answer to challenge question was incorrect.</p>
     * INVALID_CHALLENGE_ANSWER = 91
     */
    public static final Integer INVALID_CHALLENGE_ANSWER = 91;

    /**
     * <p>User failed to log in</p>
     * USER_LOGIN_FAILED = 92
     */
    public static final Integer USER_LOGIN_FAILED = 92;

    /**
     * NOT_FOUND_USER_ID = 14
     */
    public static final Integer NOT_FOUND_WHATS_NEW = 93;
}
