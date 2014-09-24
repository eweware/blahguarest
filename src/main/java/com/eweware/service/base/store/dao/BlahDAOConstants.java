package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.FieldDescriptor;
import com.eweware.service.base.store.dao.schema.type.PollOptionTextValidator;
import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Field names and value data types for blah entities.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 8:15 PM
 */
public interface BlahDAOConstants {

    /**
     * <p>The blah type id of this blah. A string.</p>
     */
    static final String TYPE_ID = "Y";

    /**
     * <p>The blahs text line (tagline). A string.
     * Unicode. Must <b>not</b> include any HTML markup.</p>
     */
    static final String TEXT = "T";

    /**
     * <p>The blahs body text. Optional. A string.</p>
     */
    static final String BODY = "F";

    /**
     * <p>The blah's group id. For now, this is the
     * originating group id. We don't deal with
     * infections across groups yet.
     * A string.</p>
     */
    static final String GROUP_ID = "G";

    /**
     * <p>The blah author's user id. A string.</p>
     */
    static final String AUTHOR_ID = "A";

    /**
     * <p>The count of users who have promoted this blah.
     * A long.</p>
     */
    static final String PROMOTED_COUNT = "P";

    /**
     * <p>The count of users who have demoted this blah.
     * A Long.</p>
     */
    static final String DEMOTED_COUNT = "D";

    /**
     * <p>The number of views of this blah.
     * A Long.</p>
     */
    static final String VIEWS = "V";

    /**
     * <p>The number of opens of this blah.
     * A long.</p>
     */
    static final String OPENS = "O";

    /**
     * <p>The number of comments on this blah.
     * A long.</p>
     */
    static final String COMMENTS = "C";


    /**
     * <p>Whether or not this blah is anonymous.
     * A boolean.</p>
     */
    static final String ANONYMOUS = "XX";



    /**
     * <p>Whether or not this blah is mature.
     * A boolean.</p>
     */
    static final String FLAGGEDCONTENT = "XXX";


    /**
     * <p>The blah's all-time strength.
     * A float between 0 and 1, inclusive.</p>
     */
    static final String BLAH_STRENGTH = "S";

    /**
     * <p>Optional field: blah expires on this date. Used in, e.g., predictions.</p>
     * <p>A datetime.</p>
     *
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType#PREDICTION
     */
    static final String EXPIRATION_DATE = "E";

    /**
     * <p>A count of the number of users who have deemed
     * this blah's prediction result to be valid. Applicable to blahs whose blah type
     * category is a prediction pattern.</p>
     *
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType#PREDICTION
     */
    static final String PREDICTION_RESULT_CORRECT_COUNT = "1";

    /**
     * <p>A count of the number of users who have deemed
     * this blah's prediction result to be invalid. Applicable to blahs whose blah type
     * category is a prediction pattern.</p>
     *
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType#PREDICTION
     */
    static final String PREDICTION_RESULT_INCORRECT_COUNT = "2";

    /**
     * <p>A count of the number of users who have deemed
     * this blah's prediction result to be unclear or unresolvable.
     * Applicable to blahs whose blah type
     * category is a prediction pattern.</p>
     *
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType#PREDICTION
     */
    static final String PREDICTION_RESULT_UNCLEAR_COUNT = "3";

    /**
     * <p>A count of the number of users who agree with the prediction.
     * Applicable to blahs whose blah type
     * category is a prediction pattern.</p>
     *
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType#PREDICTION
     */
    static final String PREDICTION_USER_AGREE_COUNT = "4";

    /**
     * <p>A count of the number of users who disagree with the prediction.
     * Applicable to blahs whose blah type
     * category is a prediction pattern.</p>
     *
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType#PREDICTION
     */
    static final String PREDICTION_USER_DISAGREE_COUNT = "5";

    /**
     * <p>A count of the number of users who deem this prediction unresolvable.
     * Applicable to blahs whose blah type
     * category is a prediction pattern.</p>
     *
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType#PREDICTION
     */
    static final String PREDICTION_USER_UNCLEAR_COUNT = "6";

    /**
     * <p>If this blah is a poll, this is the number
     * of poll options.
     * A long.</p>
     *
     * @see #POLL_OPTIONS_TEXT
     * @see #POLL_OPTION_VOTES
     */
    static final String POLL_OPTION_COUNT = "H";

    /**
     * <p>If this blah is a poll, this is an array
     * with the text for each poll option.
     * An array of string.</p>
     *
     * @see #POLL_OPTION_COUNT
     */
    static final String POLL_OPTIONS_TEXT = "I";

    /**
     * <p>If this blah is a poll, this is an array
     * with the vote counts for each poll option.
     * An array of integer.</p>
     *
     * @see #POLL_OPTION_COUNT
     */
    static final String POLL_OPTION_VOTES = "J";

    /**
     * <p>The blah's "recent" strength. The meaning of "recent"
     * may vary in terms of time and algorithm used.
     * A float between 0 and 1, inclusive.</p>
     */
    static final String RECENT_BLAH_STRENGTH = "R";

    /**
     * <p> An optional JSON document containing statistics for the blah. <b>TODO: need better doc for this</b></p>
     */
    static final String STATS = "L";

    /**
     * <p>An optional array of image ids for the blah. The semantics
     * of the array are currently not well-defined, subject to
     * experimentation. The image id points to the metadata for
     * the image, stored in a media record. Image data itself is
     * stored in S3.
     * An array of string.</p>
     *
     * @see MediaDAOConstants
     * @see MediaDAO
     */
    static final String IMAGE_IDS = "M";

    /**
     * <p>The id of the user who this blah is intended for</p>
     */
    static final String TARGET_USER_ID = "TU";

    /**
     * <p>A set of badges associated with this blah.</p>
     */
    static final String BADGE_IDS = "B";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    TYPE_ID, TEXT, BODY, GROUP_ID, AUTHOR_ID, TARGET_USER_ID}),

            new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{
                    PREDICTION_RESULT_CORRECT_COUNT, PREDICTION_RESULT_INCORRECT_COUNT, PREDICTION_RESULT_UNCLEAR_COUNT,
                    PREDICTION_USER_AGREE_COUNT, PREDICTION_USER_DISAGREE_COUNT, PREDICTION_USER_UNCLEAR_COUNT,
                    PROMOTED_COUNT, DEMOTED_COUNT,
                    VIEWS, OPENS, COMMENTS,
                    POLL_OPTION_COUNT
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{
                    BLAH_STRENGTH, RECENT_BLAH_STRENGTH
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new String[]{
                    STATS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.B, new String[]{
                    ANONYMOUS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{
                    FLAGGEDCONTENT
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[]{
                    IMAGE_IDS, BADGE_IDS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.DT, new String[]{EXPIRATION_DATE}),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new FieldDescriptor[]{
                    new FieldDescriptor(POLL_OPTIONS_TEXT, new PollOptionTextValidator()),
                    new FieldDescriptor(POLL_OPTION_VOTES)})
    };

}
