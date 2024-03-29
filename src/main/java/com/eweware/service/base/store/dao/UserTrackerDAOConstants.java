package com.eweware.service.base.store.dao;

/**
 * <p>Entity contains stats about a user.</p>
 * <p><b>TODO: needs doc</b></p>
 * @author rk@post.harvard.edu
 *         Date: 8/16/12 Time: 4:15 PM
 *         }
 */
public interface UserTrackerDAOConstants {

    static final String UT_STATS_AVERAGE_FIELD_NAME_PREFIX = "A";
    static final String UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX = "S";

    static final String UT_USER_ID = "id"; // TODO used by unit tests as a quick way to delete all trackers for a user
    static final String UT_NUMBER_OF_DAYS_IN_MONTH = "n"; // convenience for clients

    static final String UT_BLAHS_CREATED_IN_MONTH = "X";
    static final String UT_COMMENTS_ON_OWNED_BLAHS_IN_MONTH = "C";
    static final String UT_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH = "OC";
    static final String UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH = "T";
    static final String UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH = "U";
    static final String UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH = "DT";
    static final String UT_VIEWS_OF_OWNED_BLAHS_IN_MONTH = "V";
    static final String UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH = "VS";
    static final String UT_OPENS_OF_OWNED_BLAHS_IN_MONTH = "O";
    static final String UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH = "OS";


    static final String UT_AVE_BLAHS_CREATED_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_BLAHS_CREATED_IN_MONTH;
    static final String UT_SD_BLAHS_CREATED_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_BLAHS_CREATED_IN_MONTH;
    static final String UT_AVE_COMMENTS_ON_OWNED_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_COMMENTS_ON_OWNED_BLAHS_IN_MONTH;
    static final String UT_SD_COMMENTS_ON_OWNED_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_COMMENTS_ON_OWNED_BLAHS_IN_MONTH;
    static final String UT_AVE_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH;
    static final String UT_SD_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH;
    static final String UT_AVE_VOTES_FOR_OWNED_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_SD_VOTES_FOR_OWNED_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_AVE_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_SD_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_AVE_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_SD_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_AVE_VIEWS_OF_OWNED_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VIEWS_OF_OWNED_BLAHS_IN_MONTH;
    static final String UT_SD_VIEWS_OF_OWNED_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VIEWS_OF_OWNED_BLAHS_IN_MONTH;
    static final String UT_AVE_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH;
    static final String UT_SD_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH;
    static final String UT_AVE_OPENS_OF_OWNED_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_OPENS_OF_OWNED_BLAHS_IN_MONTH;
    static final String UT_SD_OPENS_OF_OWNED_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_OPENS_OF_OWNED_BLAHS_IN_MONTH;
    static final String UT_AVE_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH;
    static final String UT_SD_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH;

    static final String UT_BLAH_STRENGTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX;
    static final String UT_BLAH_STRENGTH_MAXIMUM = "SM";

    static final String UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = "t";
    static final String UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = "u";
    static final String UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = "d";
    static final String UT_VIEWS_OF_OTHERS_BLAHS_IN_MONTH = "v";
    static final String UT_OPENS_OF_OTHERS_BLAHS_IN_MONTH = "o";

    static final String UT_AVE_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_SD_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_AVE_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_SD_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_AVE_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_SD_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_AVE_VIEWS_OF_OTHERS_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VIEWS_OF_OTHERS_BLAHS_IN_MONTH;
    static final String UT_SD_VIEWS_OF_OTHERS_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VIEWS_OF_OTHERS_BLAHS_IN_MONTH;
    static final String UT_AVE_OPENS_OF_OTHERS_BLAHS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_OPENS_OF_OTHERS_BLAHS_IN_MONTH;
    static final String UT_SD_OPENS_OF_OTHERS_BLAHS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_OPENS_OF_OTHERS_BLAHS_IN_MONTH;

    static final String UT_COMMENTS_CREATED_IN_MONTH = "XX";
    static final String UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH = "XO";
    static final String UT_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = "TT";
    static final String UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = "UU";
    static final String UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = "DD";
    static final String UT_VIEWS_OF_OWNED_COMMENTS_IN_MONTH = "VV";
    static final String UT_OPENS_OF_OWNED_COMMENTS_IN_MONTH = "OO";

    static final String UT_AVE_COMMENTS_CREATED_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_COMMENTS_CREATED_IN_MONTH;
    static final String UT_SD_COMMENTS_CREATED_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_COMMENTS_CREATED_IN_MONTH;
    static final String UT_AVE_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH;
    static final String UT_SD_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH;
    static final String UT_AVE_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_SD_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_AVE_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_SD_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_AVE_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_SD_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_AVE_VIEWS_OF_OWNED_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VIEWS_OF_OWNED_COMMENTS_IN_MONTH;
    static final String UT_SD_VIEWS_OF_OWNED_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VIEWS_OF_OWNED_COMMENTS_IN_MONTH;
    static final String UT_AVE_OPENS_OF_OWNED_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_OPENS_OF_OWNED_COMMENTS_IN_MONTH;
    static final String UT_SD_OPENS_OF_OWNED_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_OPENS_OF_OWNED_COMMENTS_IN_MONTH;

    static final String UT_COMMENT_STRENGTH_IN_MONTH = "SS";
    static final String UT_COMMENT_STRENGTH_MAXIMUM_IN_MONTH = "CM";

    static final String UT_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = "tt";
    static final String UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = "uu";
    static final String UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = "dd";
    static final String UT_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH = "vv";
    static final String UT_OPENS_OF_OTHERS_COMMENTS_IN_MONTH = "oo";

    static final String UT_AVE_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_SD_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_AVE_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_SD_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_AVE_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_SD_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_AVE_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_SD_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_AVE_OPENS_OF_OTHERS_COMMENTS_IN_MONTH = UT_STATS_AVERAGE_FIELD_NAME_PREFIX + UT_OPENS_OF_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_SD_OPENS_OF_OTHERS_COMMENTS_IN_MONTH = UT_STATS_STANDARD_DEVIATION_FIELD_NAME_PREFIX + UT_OPENS_OF_OTHERS_COMMENTS_IN_MONTH;

    // DAILY
    static final String UT_DAILY_STATS_ARRAY = "dy";

    static final String UT_BLAHS_CREATED_IN_DAY = UT_BLAHS_CREATED_IN_MONTH;
    static final String UT_COMMENTS_ON_OWNED_BLAHS_IN_DAY = UT_COMMENTS_ON_OWNED_BLAHS_IN_MONTH;
    static final String UT_COMMENTS_ON_OTHERS_BLAHS_IN_DAY = UT_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH;
    static final String UT_VOTES_FOR_OWNED_BLAHS_IN_DAY = UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_UP_VOTES_FOR_OWNED_BLAHS_IN_DAY = UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_DAY = UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH;
    static final String UT_VIEWS_OF_OWNED_BLAHS_IN_DAY = UT_VIEWS_OF_OWNED_BLAHS_IN_MONTH;
    static final String UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_DAY = UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH;
    static final String UT_OPENS_OF_OWNED_BLAHS_IN_DAY = UT_OPENS_OF_OWNED_BLAHS_IN_MONTH;
    static final String UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_DAY = UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH;

    static final String UT_VOTES_FOR_OTHERS_BLAHS_IN_DAY = UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_DAY = UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_DAY = UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH;
    static final String UT_VIEWS_OF_OTHERS_BLAHS_IN_DAY = UT_VIEWS_OF_OTHERS_BLAHS_IN_MONTH;
    static final String UT_OPENS_OF_OTHERS_BLAHS_IN_DAY = UT_OPENS_OF_OTHERS_BLAHS_IN_MONTH;

    static final String UT_COMMENTS_CREATED_IN_DAY = UT_COMMENTS_CREATED_IN_MONTH;
    static final String UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_DAY = UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH;
    static final String UT_VOTES_FOR_OWNED_COMMENTS_IN_DAY = UT_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_DAY = UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_DAY = UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH;
    static final String UT_VIEWS_OF_OWNED_COMMENTS_IN_DAY = UT_VIEWS_OF_OWNED_COMMENTS_IN_MONTH;
    static final String UT_OPENS_OF_OWNED_COMMENTS_IN_DAY = UT_OPENS_OF_OWNED_COMMENTS_IN_MONTH;

    static final String UT_VOTES_FOR_OTHERS_COMMENTS_IN_DAY = UT_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_DAY = UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_DAY = UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_VIEWS_OF_OTHERS_COMMENTS_IN_DAY = UT_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH;
    static final String UT_OPENS_OF_OTHERS_COMMENTS_IN_DAY = UT_OPENS_OF_OTHERS_COMMENTS_IN_MONTH;


    /* All monthly field names (including stats) */
    String[] UT_MONTHLY_FIELD_NAMES = new String[]{

            UT_BLAHS_CREATED_IN_MONTH, UT_AVE_BLAHS_CREATED_IN_MONTH, UT_SD_BLAHS_CREATED_IN_MONTH,
            UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH, UT_AVE_VOTES_FOR_OWNED_BLAHS_IN_MONTH, UT_SD_VOTES_FOR_OWNED_BLAHS_IN_MONTH,
            UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH, UT_AVE_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH, UT_SD_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH,
            UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH, UT_AVE_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH, UT_SD_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH,
            UT_COMMENTS_ON_OWNED_BLAHS_IN_MONTH, UT_AVE_COMMENTS_ON_OWNED_BLAHS_IN_MONTH, UT_SD_COMMENTS_ON_OWNED_BLAHS_IN_MONTH,
            UT_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH, UT_AVE_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH, UT_SD_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH,
            UT_VIEWS_OF_OWNED_BLAHS_IN_MONTH, UT_AVE_VIEWS_OF_OWNED_BLAHS_IN_MONTH, UT_SD_VIEWS_OF_OWNED_BLAHS_IN_MONTH,
            UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH, UT_AVE_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH, UT_SD_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH,
            UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH, UT_AVE_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH, UT_SD_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH,
            UT_OPENS_OF_OWNED_BLAHS_IN_MONTH, UT_AVE_OPENS_OF_OWNED_BLAHS_IN_MONTH, UT_SD_OPENS_OF_OWNED_BLAHS_IN_MONTH,
            UT_BLAH_STRENGTH, UT_BLAH_STRENGTH_MAXIMUM,

            UT_COMMENTS_CREATED_IN_MONTH, UT_AVE_COMMENTS_CREATED_IN_MONTH, UT_SD_COMMENTS_CREATED_IN_MONTH,
            UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH, UT_AVE_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH, UT_SD_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH,
            UT_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, UT_AVE_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, UT_SD_VOTES_FOR_OWNED_COMMENTS_IN_MONTH,
            UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, UT_AVE_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, UT_SD_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH,
            UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, UT_AVE_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, UT_SD_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH,
            UT_VIEWS_OF_OWNED_COMMENTS_IN_MONTH, UT_AVE_VIEWS_OF_OWNED_COMMENTS_IN_MONTH, UT_SD_VIEWS_OF_OWNED_COMMENTS_IN_MONTH,
            UT_OPENS_OF_OWNED_COMMENTS_IN_MONTH, UT_AVE_OPENS_OF_OWNED_COMMENTS_IN_MONTH, UT_SD_OPENS_OF_OWNED_COMMENTS_IN_MONTH,
            UT_COMMENT_STRENGTH_IN_MONTH, UT_COMMENT_STRENGTH_MAXIMUM_IN_MONTH,

            UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, UT_AVE_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, UT_SD_VOTES_FOR_OTHERS_BLAHS_IN_MONTH,
            UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, UT_AVE_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, UT_SD_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH,
            UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, UT_AVE_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, UT_SD_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH,
            UT_VIEWS_OF_OTHERS_BLAHS_IN_MONTH, UT_AVE_VIEWS_OF_OTHERS_BLAHS_IN_MONTH, UT_SD_VIEWS_OF_OTHERS_BLAHS_IN_MONTH,
            UT_OPENS_OF_OTHERS_BLAHS_IN_MONTH, UT_AVE_OPENS_OF_OTHERS_BLAHS_IN_MONTH, UT_SD_OPENS_OF_OTHERS_BLAHS_IN_MONTH,
            UT_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, UT_AVE_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, UT_SD_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH,
            UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, UT_AVE_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, UT_SD_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH,
            UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, UT_AVE_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, UT_SD_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH,
            UT_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH, UT_AVE_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH, UT_SD_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH,
            UT_OPENS_OF_OTHERS_COMMENTS_IN_MONTH, UT_AVE_OPENS_OF_OTHERS_COMMENTS_IN_MONTH, UT_SD_OPENS_OF_OTHERS_COMMENTS_IN_MONTH,
    };

    String[] UT_DAILY_FIELD_NAMES = new String[]{

            UT_BLAHS_CREATED_IN_DAY,
            UT_COMMENTS_ON_OWNED_BLAHS_IN_DAY,
            UT_COMMENTS_ON_OTHERS_BLAHS_IN_DAY,
            UT_VOTES_FOR_OWNED_BLAHS_IN_DAY,
            UT_UP_VOTES_FOR_OWNED_BLAHS_IN_DAY,
            UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_DAY,
            UT_VIEWS_OF_OWNED_BLAHS_IN_DAY,
            UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_DAY,
            UT_OPENS_OF_OWNED_BLAHS_IN_DAY,
            UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_DAY,

            UT_VOTES_FOR_OTHERS_BLAHS_IN_DAY,
            UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_DAY,
            UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_DAY,
            UT_VIEWS_OF_OTHERS_BLAHS_IN_DAY,
            UT_OPENS_OF_OTHERS_BLAHS_IN_DAY,

            UT_COMMENTS_CREATED_IN_DAY,
            UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_DAY,
            UT_VOTES_FOR_OWNED_COMMENTS_IN_DAY,
            UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_DAY,
            UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_DAY,
            UT_VIEWS_OF_OWNED_COMMENTS_IN_DAY,
            UT_OPENS_OF_OWNED_COMMENTS_IN_DAY,

            UT_VOTES_FOR_OTHERS_COMMENTS_IN_DAY,
            UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_DAY,
            UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_DAY,
            UT_VIEWS_OF_OTHERS_COMMENTS_IN_DAY,
            UT_OPENS_OF_OTHERS_COMMENTS_IN_DAY
    };
}
