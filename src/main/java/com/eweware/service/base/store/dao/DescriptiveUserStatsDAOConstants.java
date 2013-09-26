package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Fields for descriptive user statistics.<b>TODO: needs better doc</b></p>
 * @author rk@post.harvard.edu
 *         Date: 9/25/12 Time: 1:37 PM
 *
 *         This contains global descriptive statistics for the user population.
 *
 */
public interface DescriptiveUserStatsDAOConstants {

    /** Number of days into the past for this descriptive stats record. 0 means full history */
    static final String NUMBER_OF_DAYS_IN_SUMMARY = "e";

    /**
     * For total instances, the SPREADS field is included and is a hash of hashes containing
     * the spreads for each kind of demographic. For example,
     * the doc looks something like:
     * {"e": 30,
     *  # demographic spreads follow:
     *  "S": {"ug": {"c": .., "cn": .., "cm": .., ...}, # gender spread
     *        "ui": {"c": .., "cn": .., "cm": .., ...}, # income spread
     *       },
     *  # totals follow:
     *  "c": .., "cn": .., "cm": .., ...
     * }
     */
    static final String SPREADS = "S";
    static final String GENDER_SPREAD = "ug";
    static final String INCOME_SPREAD = "ui";
    static final String RACE_SPREAD = "ur";

    static final String[] SPREAD_FIELD_NAMES = new String[]{
            GENDER_SPREAD, INCOME_SPREAD, RACE_SPREAD
    };

    /** Number of blahs created: value an integer.
     *  The other are incremental stats: value a double */
    static final String BLAHS_CREATED = "c";
    static final String BLAHS_CREATED_MIN = "cn";
    static final String BLAHS_CREATED_MAX = "cm";
    static final String BLAHS_CREATED_SUM = "cs";
    static final String BLAHS_CREATED_SUM_OF_SQUARES = "sq";

    /** Number of up votes for blahs */
    static final String BLAH_UP_VOTES = "u";
    static final String BLAH_UP_VOTES_MIN = "un";
    static final String BLAH_UP_VOTES_MAX = "um";
    static final String BLAH_UP_VOTES_SUM = "us";
    static final String BLAH_UP_VOTES_SUM_OF_SQUARES = "uq";

    /** Number of down votes for blahs */
    static final String BLAH_DOWN_VOTES = "d";
    static final String BLAH_DOWN_VOTES_MIN = "dn";
    static final String BLAH_DOWN_VOTES_MAX = "dm";
    static final String BLAH_DOWN_VOTES_SUM = "ds";
    static final String BLAH_DOWN_VOTES_SUM_OF_SQUARES = "dq";

    /** Number of views of blahs: an integer or null for no views */
    static final String BLAHS_VIEWED = "v";
    static final String BLAHS_VIEWED_MIN = "vn";
    static final String BLAHS_VIEWED_MAX = "vm";
    static final String BLAHS_VIEWED_SUM = "vs";
    static final String BLAHS_VIEWED_SUM_OF_SQUARES = "vq";

    /** Number of opens of blahs: an integer or null for no opens */
    static final String BLAHS_OPENED = "o";
    static final String BLAHS_OPENED_MIN = "on";
    static final String BLAHS_OPENED_MAX = "om";
    static final String BLAHS_OPENED_SUM = "os";
    static final String BLAHS_OPENED_SUM_OF_SQUARES = "oq";

    /** Number of comments created */
    static final String COMMENTS_CREATED = "C";
    static final String COMMENTS_CREATED_MIN = "Cn";
    static final String COMMENTS_CREATED_MAX = "Cm";
    static final String COMMENTS_CREATED_SUM = "Cs";
    static final String COMMENTS_CREATED_SUM_OF_SQUARES = "Cq";

    /** Number of up votes for comments */
    static final String COMMENT_UP_VOTES = "U";
    static final String COMMENT_UP_VOTES_MIN = "Un";
    static final String COMMENT_UP_VOTES_MAX = "Um";
    static final String COMMENT_UP_VOTES_SUM = "Us";
    static final String COMMENT_UP_VOTES_SUM_OF_SQUARES = "Uq";

    static final String COMMENT_DOWN_VOTES = "D";
    static final String COMMENT_DOWN_VOTES_MIN = "Dn";
    static final String COMMENT_DOWN_VOTES_MAX = "Dm";
    static final String COMMENT_DOWN_VOTES_SUM = "Ds";
    static final String COMMENT_DOWN_VOTES_SUM_OF_SQUARES = "Dq";

    /** Number of views of comments: an integer or null for no views */
    static final String COMMENTS_VIEWED = "V";
    static final String COMMENTS_VIEWED_MIN = "Vn";
    static final String COMMENTS_VIEWED_MAX = "Vm";
    static final String COMMENTS_VIEWED_SUM = "Vs";
    static final String COMMENTS_VIEWED_SUM_OF_SQUARES = "Vq";

    /** Number of opens of comments: an integer or null for no opens */
    static final String COMMENTS_OPENED = "O";
    static final String COMMENTS_OPENED_MIN = "On";
    static final String COMMENTS_OPENED_MAX = "Om";
    static final String COMMENTS_OPENED_SUM = "Os";
    static final String COMMENTS_OPENED_SUM_OF_SQUARES = "Oq";


    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{
                    NUMBER_OF_DAYS_IN_SUMMARY,}),
            new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{
                    NUMBER_OF_DAYS_IN_SUMMARY,
                    BLAHS_CREATED,
                    BLAH_UP_VOTES,
                    BLAH_DOWN_VOTES,
                    BLAHS_VIEWED,
                    BLAHS_OPENED,
                    COMMENTS_CREATED,
                    COMMENT_UP_VOTES,
                    COMMENT_DOWN_VOTES,
                    COMMENTS_VIEWED,
                    COMMENTS_OPENED}),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{
                    BLAHS_CREATED_MIN, BLAHS_CREATED_MAX, BLAHS_CREATED_SUM, BLAHS_CREATED_SUM_OF_SQUARES,
                    BLAH_UP_VOTES_MIN, BLAH_UP_VOTES_MAX, BLAH_UP_VOTES_SUM, BLAH_UP_VOTES_SUM_OF_SQUARES,
                    BLAH_DOWN_VOTES_MIN, BLAH_DOWN_VOTES_MAX, BLAH_DOWN_VOTES_SUM, BLAH_DOWN_VOTES_SUM_OF_SQUARES,
                    BLAHS_VIEWED_MIN, BLAHS_VIEWED_MAX, BLAHS_VIEWED_SUM, BLAHS_VIEWED_SUM_OF_SQUARES,
                    BLAHS_OPENED_MIN, BLAHS_OPENED_MAX, BLAHS_OPENED_SUM, BLAHS_OPENED_SUM_OF_SQUARES,
                    COMMENTS_CREATED_MIN, COMMENTS_CREATED_MAX, COMMENTS_CREATED_SUM, COMMENTS_CREATED_SUM_OF_SQUARES,
                    COMMENT_UP_VOTES_MIN, COMMENT_UP_VOTES_MAX, COMMENT_UP_VOTES_SUM, COMMENT_UP_VOTES_SUM_OF_SQUARES,
                    COMMENT_DOWN_VOTES_MIN, COMMENT_DOWN_VOTES_MAX, COMMENT_DOWN_VOTES_SUM, COMMENT_DOWN_VOTES_SUM_OF_SQUARES,
                    COMMENTS_VIEWED_MIN, COMMENTS_VIEWED_MAX, COMMENTS_VIEWED_SUM, COMMENTS_VIEWED_SUM_OF_SQUARES,
                    COMMENTS_OPENED_MIN, COMMENTS_OPENED_MAX, COMMENTS_OPENED_SUM, COMMENTS_OPENED_SUM_OF_SQUARES,
            }),
    };
}
