package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Field names and value data types for comment entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 8:22 PM
 */
public interface CommentDAOConstants {

    /**
     * <p>The comment's blah id. A string.</p>
     */
    static final String BLAH_ID = "B";

    /**
     * <p>The comments text. A string.</p>
     */
    static final String TEXT = "T";

    /**
     * <p>The comment's author's user id. A string.</p>
     */
    static final String AUTHOR_ID = "A";

    /**
     * <p>The vote, if any, of the comment on the blah.
     * An integer.</p>
     */
    static final String BLAH_VOTE = "V";

    /**
     * <p>The total number of votes on this comment. An integer. <b>TODO: get rid of this: can be inferred from sum of up/down votes</b></p>
     */
    static final String COMMENT_VOTES = "C";

    /**
     * <p>The number of up votes for this comment. An integer.</p>
     */
    static final String COMMENT_UP_VOTES = "U";

    /**
     * <p>The number of down votes for this comment. An integer.</p>
     */
    static final String COMMENT_DOWN_VOTES = "D";

    /**
     * <p>This comment's current strength. A float between 0 and 1, inclusive.</p>
     */
    static final String COMMENT_STRENGTH = "S";

    /**
     * <p>The number of times this comment has been viewed. An integer.</p>
     */
    static final String VIEWS = "W";

    /**
     * <p>The number of times this comment has been opened. An integer.</p>
     */
    static final String OPENS = "O";

    /**
     * <p>Optional stats entity for this comment. <b>TODO: need better doc for this.</b></p>
     */
    static final String STATS = "L";

    /**
     * <p>An optional array of image ids for the comment. The semantics
     * of the array are currently not well-defined, subject to
     * experimentation. The image id points to the metadata for
     * the image, stored in a media record. Image data itself is
     * stored in S3.
     * An array of string.</p>
     * @see MediaDAOConstants
     * @see MediaDAO
     */
    static final String IMAGE_IDS = "M";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    BLAH_ID, TEXT, AUTHOR_ID
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{
                    BLAH_VOTE, COMMENT_VOTES, COMMENT_UP_VOTES, COMMENT_DOWN_VOTES, VIEWS, OPENS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{
                    COMMENT_STRENGTH
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new String[]{
                    STATS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[] {
                    IMAGE_IDS
            }),
    };
}
