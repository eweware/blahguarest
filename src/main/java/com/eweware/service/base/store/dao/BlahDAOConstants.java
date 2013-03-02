package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Field names and value data types for blah entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 8:15 PM
 */
public interface BlahDAOConstants {

    /**
     * <p>The blah type id of this blah. A string.</p>
     */
    static final String TYPE_ID = "typeId";

    /**
     * <p>The blahs text line (tagline). A string.
     * Unicode. Must <b>not</b> include any HTML markup.</p>
     */
    static final String TEXT = "text";

    /**
     * <p>The blahs body text. Optional. A string.</p>
     */
    static final String BODY = "b";

    /**
     * <p>The blah's group id. For now, this is the
     * originating group id. We don't deal with
     * infections across groups yet.
     * A string.</p>
     */
    static final String GROUP_ID = "groupId";

    /**
     * <p>The blah author's user id. A string.</p>
     */
    static final String AUTHOR_ID = "authorId";

    /**
     * <p>The number of up votes for this blah.
     * An integer.</p>
     */
    static final String UP_VOTES = "vu";

    /**
     * <p>The number of down votes for this blah.
     * An integer.</p>
     */
    static final String DOWN_VOTES = "vd";

    /**
     * <p>The number of views of this blah.
     * An integer.</p>
     */
    static final String VIEWS = "views";

    /**
     * <p>The number of opens of this blah.
     * An integer.</p>
     */
    static final String OPENS = "opens";

    /**
     * <p>The number of comments on this blah.
     * An integer.</p>
     */
    static final String COMMENTS = "c";

    /**
     * <p>The blah's all-time strength.
     * A float between 0 and 1, inclusive.</p>
     */
    static final String BLAH_STRENGTH = "s";

    /**
     * <p>If this blah is a poll, this is the number
     * of poll options.
     * An integer.</p>
     *
     * @see #POLL_OPTIONS_TEXT
     * @see #POLL_OPTION_VOTES
     */
    static final String POLL_OPTION_COUNT = "pc";

    /**
     * <p>If this blah is a poll, this is an array
     * with the text for each poll option.
     * An array of string.</p>
     *
     * @see #POLL_OPTION_COUNT
     */
    static final String POLL_OPTIONS_TEXT = "pt";

    /**
     * <p>If this blah is a poll, this is an array
     * with the vote counts for each poll option.
     * An array of integer.</p>
     *
     * @see #POLL_OPTION_COUNT
     */
    static final String POLL_OPTION_VOTES = "pv";

    /**
     * <p>The blah's "recent" strength. The meaning of "recent"
     * may vary in terms of time and algorithm used.
     * A float between 0 and 1, inclusive.</p>
     */
    static final String RECENT_BLAH_STRENGTH = "r";

    /**
     * <p> An optional JSON document containing statistics for the blah. <b>TODO: need better doc for this</b></p>
     *
     */
    static final String STATS = "stats";

    /**
     * <p>An optional array of image ids for the blah. The semantics
     * of the array are currently not well-defined, subject to
     * experimentation. The image id points to the metadata for
     * the image, stored in a media record. Image data itself is
     * stored in S3.
     * An array of string.</p>
     * @see MediaDAOConstants
     * @see MediaDAO
     */
    static final String IMAGE_IDS = "img";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    TYPE_ID, TEXT, BODY, GROUP_ID, AUTHOR_ID}),

            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{
                    UP_VOTES, DOWN_VOTES, VIEWS, OPENS, COMMENTS, POLL_OPTION_COUNT
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{
                    BLAH_STRENGTH, RECENT_BLAH_STRENGTH
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new String[]{
                    STATS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[] {
                    IMAGE_IDS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new String[] {POLL_OPTIONS_TEXT, POLL_OPTION_VOTES})
    };

}
