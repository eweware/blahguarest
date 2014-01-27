package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

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
    static final String BLAH_ID = "I";

    /**
     * <p>The blah's author id. A string.</p>
     */
    static final String AUTHOR_ID = "A";

    /**
     * <p>The blah type id of the blah. A string.</p>
     */
    static final String TYPE = "Y";

    /**
     * <p>The blah's text line (tagline). A string.</p>
     */
    static final String BLAH_TEXT = "T";

    /**
     * <p>The blah's originating group id. A string.</p>
     */
    static final String GROUP_ID = "G";

    /**
     * <p>The inbox number. Inboxes for a group, if any, are sequentially
     * numbered, 0-origin. An integer.</p>
     */
    static final String INBOX_NUMBER = "N";

    /**
     * <p>The number of up votes for this blah. An integer</p>
     */
    static final String UP_VOTES = "P";

    /**
     * <p>The number of down votes for this blah. An integer</p>
     */
    static final String DOWN_VOTES = "D";

    /**
     * <p>The number of times this blah has been viewed. An Integer.</p>
     */
    static final String VIEWS = "V";

    /**
     * <p>The number of times this blah has been opened. An integer.</p>
     */
    static final String OPENS = "O";

    /**
     * <p>The number of comments on this blah. An integer.</p>
     */
    static final String COMMENTS = "C";

    /**
     * <p> The blah's all-time strength. A float between 0 and 1, inclusive.</p>
     */
    static final String BLAH_STRENGTH = "S";

    /**
     * <p>The blah's recent strength. A float between 0 and 1, inclusive.</p>
     */
    static final String RECENT_BLAH_STRENGTH = "R";


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
    static final String IMAGE_IDS = "M";

    /**
     * <p>Indicates badges associated with this blah.</p>
     * <p>'b' := has badges</p>
     */
    static final String BADGE_INDICATOR = "B";

    /**
     * <p>Possible nickname of author</p>
     */
    static final String AUTHOR_NICKNAME = "K";

    /**
     * <p>Indicates recent activity on the blah</p>
     */
    static final String RECENTLY_ACTIVE = "RR";


    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{BLAH_ID, AUTHOR_ID, TYPE, BLAH_TEXT, GROUP_ID, BADGE_INDICATOR, AUTHOR_NICKNAME}),
            new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{INBOX_NUMBER, UP_VOTES, DOWN_VOTES, VIEWS, OPENS, COMMENTS}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{INBOX_NUMBER}),
            new SchemaDataTypeFieldMap(SchemaDataType.B, new String[]{RECENTLY_ACTIVE}),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{BLAH_STRENGTH, RECENT_BLAH_STRENGTH}),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[] {IMAGE_IDS}),
    };
}
