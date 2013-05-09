package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/29/12 Time: 3:53 PM
 *
 *         All records here are in the demographics collection.
 */
public interface DemographicsObjectDAOConstants {


    /**
     * This is the id in the demographics collection for the object holding
     * overall demographics totals for blahs. This is cross-group.
     */
    public static final String BLAH_DEMOGRAPHICS = "_b";

    /**
     * This is the prefix of the id in the demographics collection for the object holding
     * 30-day demographics totals for blahs.
     */
    public static final String BLAH_30_DAYS_DEMOGRAPHICS = "_b30";

    public static final String[] BLAH_DEMOGRAPHIC_IDS = new String[]{BLAH_DEMOGRAPHICS, BLAH_30_DAYS_DEMOGRAPHICS};

    /**
     * This is the prefix of the id in the demographics collection for the object holding
     * overall demographics totals for blahs for each group.
     * The group id is suffixed to this.
     */
    public static final String BLAHS_PER_GROUP_DEMOGRAPHICS = "_bg_";

    /**
     * This is the prefix of the id in the demographics collection for the object holding
     * 30-day demographics totals for blahs for each group.
     * The group id is suffixed to this.
     */
    public static final String BLAHS_PER_GROUP_30_DAY_DEMOGRAPHICS = "_bg30_";

    public static final String[] BLAH_PER_GROUP_FIELD_NAME_PREFIXES = new String[]{BLAHS_PER_GROUP_DEMOGRAPHICS, BLAHS_PER_GROUP_30_DAY_DEMOGRAPHICS};

    /**
     * This is the prefix of the id in the demographics collection for the object holding
     * overall demographics totals for each blah type.
     * The blah type id is suffixed to this.
     */
    public static final String BLAHS_PER_BLAH_TYPE_DEMOGRAPHICS = "_bt_";

    /**
     * This is the prefix of the id in the demographics collection for the object holding
     * 30-day demographics totals for each blah type.
     * The blah type id is suffixed to this.
     */
    public static final String BLAHS_PER_BLAH_TYPE_30_DAY_DEMOGRAPHICS = "_bt30_";

    public static final String[] BLAH_TYPE_FIELD_NAME_PREFIXES = new String[]{BLAHS_PER_BLAH_TYPE_DEMOGRAPHICS, BLAHS_PER_BLAH_TYPE_30_DAY_DEMOGRAPHICS};

    /**
     * This is the id in the demographics collection for the object holding
     * overall demographics totals for comments.
     */
    public static final String COMMENTS_DEMOGRAPHICS = "_c";

    /**
     * This is the id in the demographics collection for the object holding
     * 30-day demographics totals for comments.
     */
    public static final String COMMENTS_30_DAYS_DEMOGRAPHICS = "_c30";

    public static final String[] COMMENT_DEMOGRAPHIC_IDS = new String[]{COMMENTS_DEMOGRAPHICS, COMMENTS_30_DAYS_DEMOGRAPHICS};


    /**
     * This is the field name in a blah, comment, group or other dao,
     * for the object holding demographics data. The contents
     * of this data are the fields described at the end of this file
     * under "Demographic Field Names".
     */
    public static final String DEMOGRAPHICS_RECORD = "_d";

    /**
     * This is similar to DEMOGRAPHICS_RECORD, but it holds
     * recent demographics. How recent? It depends on the
     * type of object to which it is attached. For example,
     * if it is attached to a UserDAO, then _d1 probably
     * holds last weeks (7 days') statistics.
     */
    public static final String RECENT_DEMOGRAPHICS_RECORD = "_d1";

    /**
     * Demographic Field Names ------------------------------------------------------
     * Each demographic record contains some of the following fields.
     */

    /**
     * This contains the possibly 0 up count (likes).
     */
    public static final String UP_VOTE_COUNT = "_u";

    /**
     * This contains the possibly 0 down vote count (dislikes).
     */
    public static final String DOWN_VOTE_COUNT = "_d";

    /**
     * This contains the view counts.
     */
    public static final String VIEW_COUNT = "_v";

    /**
     * This contains the open counts.
     */
    public static final String OPEN_COUNT = "_o";

    /**
     * This contents the blah comments count.
     */
    public static final String COMMENT_COUNT = "_c";

    public static final String[] DEMOGRAPHICS_RECORD_FIELD_NAMES =
            new String[]{DemographicsObjectDAOConstants.UP_VOTE_COUNT, DemographicsObjectDAOConstants.DOWN_VOTE_COUNT, DemographicsObjectDAOConstants.VIEW_COUNT,
                    DemographicsObjectDAOConstants.OPEN_COUNT, DemographicsObjectDAOConstants.COMMENT_COUNT
            };



}
