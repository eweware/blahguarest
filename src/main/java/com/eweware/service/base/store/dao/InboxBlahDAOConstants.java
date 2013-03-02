package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 *    <p>Field names and value data types for inbox blah entities. These are fields
 *    in a the elements of a set of inbox entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 8/30/12 Time: 11:58 AM
 */
public interface InboxBlahDAOConstants {

    /**
     * <p>The blah's id. A string.</p>
     */
    static final String BLAH_ID = "blahId";

    /**
     * <p>The blah's author id. A string.</p>
     */
    static final String AUTHOR_ID = "authorId";

    /**
     * <p>The blah type id of the blah. A string.</p>
     */
    static final String TYPE = "type";

    /**
     * <p>The blah's text line (tagline). A string.</p>
     */
    static final String BLAH_TEXT = "text";

    /**
     * <p>The blah's originating group id. A string.</p>
     */
    static final String GROUP_ID = "groupId";

    /**
     * <p>The inbox number. Inboxes for a group, if any, are sequentially
     * numbered, 0-origin. An integer.</p>
     */
    static final String INBOX_NUMBER = "sn";

    /**
     * <p>The number of up votes for this blah. An integer</p>
     */
    static final String UP_VOTES = "u";

    /**
     * <p>The number of down votes for this blah. An integer</p>
     */
    static final String DOWN_VOTES = "d";

    /**
     * <p>The number of times this blah has been viewed. An Integer.</p>
     */
    static final String VIEWS = "v";

    /**
     * <p>The number of times this blah has been opened. An integer.</p>
     */
    static final String OPENS = "o";

    /**
     * <p>The number of comments on this blah. An integer.</p>
     */
    static final String COMMENTS = "c";

    /**
     * <p> The blah's all-time strength. A float between 0 and 1, inclusive.</p>
     */
    static final String BLAH_STRENGTH = "s";

    /**
     * <p>The blah's recent strength. A float between 0 and 1, inclusive.</p>
     */
    static final String RECENT_BLAH_STRENGTH = "r";


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
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{BLAH_ID, AUTHOR_ID, TYPE, BLAH_TEXT, GROUP_ID}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{INBOX_NUMBER, UP_VOTES, DOWN_VOTES, VIEWS, OPENS, COMMENTS}),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{BLAH_STRENGTH, RECENT_BLAH_STRENGTH}),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[] {IMAGE_IDS}),
    };
}
