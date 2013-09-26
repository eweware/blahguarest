package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Field names and corresponding data types for entity used to relate user and blah information.</p>
 * <p>Information is used to check vote, and other permission based on state.</p>
 * <p>A multi-weighed, directed graph of the social network can be created from this information.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:44 PM
 */
public interface UserBlahInfoDAOConstants {

    /**
     * Id of user acting on this blah.
     * A string.
     */
    static final String USER_ID = "U";

    /**
     * Id of author of this blah.
     */
    static final String AUTHOR_ID = "A";

    /**
     * <p>Id of the blah being acted upon.
     * A string.</p>
     */
    static final String BLAH_ID = "B";

    /**
     * <p>Id of group into which blah was introduced.
     * This is used by stats.
     * A string.</p>
     */
    static final String ORIGINAL_GROUP_ID = "G";

    /**
     * <p>Id of blah type.
     * A string.</p>.
     */
    static final String BLAH_TYPE_ID = "Y";

    /**
     * <p>The promotion action on this blah by the user.
     * Set to 1 or -1 if the user promoted or demoted, respectively, this blah;
     * else this field is null or 0.
     * An integer.</p>
     */
    static final String PROMOTION = "P";

    /**
     * <p>If this is a poll blah type, indicates whether the user
     * has voted on the poll. The value is the poll option index
     * (an integer) or null if the user has not voted on this poll.
     * An integer.</p>
     */
    static final String POLL_VOTE_INDEX = "W";

    /**
     * <p>If POLL_VOTE_INDEX is not null, this is
     * the datetime when the vote was made.
     * A datetime.</p>
     */
    static final String POLL_VOTE_TIMESTAMP = "X";

    /**
     * <p>For a prediction blah, specifies the user's vote
     * for this prediction.</p>
     * <p>This field's value must be one of the codes
     * in PredictionVote. </p>
     *
     * @see com.eweware.service.mgr.type.PredictionVote
     */
    static final String PREDICTION_VOTE = "D";

    /**
     * <p>For a prediction blah, specifies the user's
     * assertion as to whether the prediction's statement was,
     * after it expired, correct, incorrect, or unclear.</p>
     * <p>This field's value must be one of the codes
     * in PredictionVote. </p>
     *
     * @see com.eweware.service.mgr.type.PredictionVote
     */
    static final String PREDICTION_RESULT_VOTE = "Z";
    /**
     * <p>The number of views of this blah by the user.
     * If the user hasn't viewed the blah, this
     * field is either null or 0.
     * An integer.</p>
     */
    static final String VIEWS = "V";

    /**
     * <p>The number of opens of this blah by the user.
     * If the user hasn't opened the blah, this
     * field is either null or 0.
     * An integer.</p>
     */
    static final String OPENS = "O";

    /**
     * <p>Number of times this user has commented on this blah.
     * An integer.</p>
     */
    static final String COMMENTS_ON_THIS_BLAH = "C";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USER_ID, AUTHOR_ID, BLAH_ID, ORIGINAL_GROUP_ID, PREDICTION_VOTE, PREDICTION_RESULT_VOTE}),
            new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{PROMOTION, POLL_VOTE_INDEX, VIEWS, OPENS, COMMENTS_ON_THIS_BLAH}),
            new SchemaDataTypeFieldMap(SchemaDataType.DT, new String[]{POLL_VOTE_TIMESTAMP})
    };
}
