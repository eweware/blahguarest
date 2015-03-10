package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 *  <p>Field names and value data types for group entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:43 PM
 */
public interface GroupDAOConstants {

    /**
     * <p> The groups visible display name (English). A String <b>>TODO: i18n</b</p>
     */
    static final String DISPLAY_NAME = "N";

    /**
     * <p> An english description. A string. <b>TODO: i18n</b></p>
     */
    static final String DESCRIPTION = "D";

    /**
     * <p>The group's descriptor. Encodes permissions and
     * other group attributes. Each attribute is
     * a single character and the attribute's identity
     * is given by its position in the string.
     * Current structure:</p>
     * <div><visibility></div>
     *
     * <div>There's currently only one attribute:</div>
     * <div><visibility> := {a|o} </div>
     * <p>where 'a' means that the group is open and
     * can be viewed by anonymous users and 'o' means that it can't.
     * All possible descriptor values are specified by the code
     * in GroupDescriptor enum</p>
     * @see GroupDescriptor
     */
    static final String DESCRIPTOR = "X";

    /**
     * <p> The group type id. A string. This is used to group groups.</p>
     */
    static final String GROUP_TYPE_ID = "Y";

    /**
     * <p>The relative rank order for displaying groups (an integer). The lower
     * the number, the more prominent the group.</p>
     */
    static final String RANK = "R";

    /**
     * <p> The current number of users in this group. An integer</p>
     */
    static final String USER_COUNT = "U";

    /**
     * <p> The current number of blahs in this group. An integer.</p>
     */
    static final String BLAH_COUNT = "B";

    /**
     * <p> The current number of users watching this group. An integer.</p>
     */
    static final String CURRENT_VIEWER_COUNT = "V";


    /**
     * <p>Group state information.
     * Its value corresponds to an authorization state (e.g., active)</p>
     * @see com.eweware.service.base.payload.AuthorizedState
     */
    static final String STATE = "S";

    /**
     * <p>The number of the first active inbox of the group, an Integer.</p>
     * <p>Each inbox is a collection whose name is the concatenation
     * of the group id and the inbox number.</p>
     */
    static final String FIRST_INBOX_NUMBER = "F";

    /**
     * <p>The number of the last active inbox of the group, an Integer.</p>
     * <p>Each inbox is a collection whose name is the concatenation
     * of the group id and the inbox number.</p>
     */
    static final String LAST_INBOX_NUMBER = "L";

    /**
     * <p>The number of the first active inbox of the group, an Integer.</p>
     * <p>Each inbox is a collection whose name is the concatenation
     * of the group id and the inbox number.</p>
     */
    static final String FIRST_SAFE_INBOX_NUMBER = "FS";

    /**
     * <p>The number of the last active inbox of the group, an Integer.</p>
     * <p>Each inbox is a collection whose name is the concatenation
     * of the group id and the inbox number.</p>
     */
    static final String LAST_SAFE_INBOX_NUMBER = "LS";

    /**
     * <p>The last time that the inboxes for this group were generated, a Date.</p>
     * <p>Precisely, this is the generation completion time.</p>
     */
    static final String LAST_TIME_INBOXES_GENERATED = "G";

    /**
     * <p>The number of milliseconds it took to generate the inboxes for
     * this group, a Long</p>
     */
    static final String INBOX_GENERATION_DURATION = "I";

    static final String ADMIN = "GA";
    static final String HEADER_IMAGE = "GHI";
    static final String AD_BLAH_ID = "GAB";
    static final String JOIN_BADGE_LIST = "PJ";
    static final String COMMENT_BADGE_LIST = "PC";
    static final String POST_BADGE_LIST = "PB";
    static final String CONTENT_EXPIRATION_DAYS = "CE";
    static final String POSTS_MODERATION_BADGE_LIST = "PM";
    static final String MODERATED = "MX";
    static final String IS_MATURE = "XXX";
    static final String IS_PRIVATE = "PP";
    static final String SHOWS_AUTHOR_DESC = "SAD";
    static final String SHOWS_SPEECH_ACTS = "SSA";
    static final String SHOW_BADGE_LIST = "SBL";
    static final String PROFILE_QUESTIONS = "PQ";

    /**
     * <p>How comments are moderated.</p>
     * <p>0 or missing = not moderated</p>
     * <p>1 = moderated by group admin</p>
     * <p>2 = moderated by post author</p>
     */
    static final String COMMENT_MODERATION_STYLE = "CMX";


    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
        new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                DISPLAY_NAME, DESCRIPTION, DESCRIPTOR, GROUP_TYPE_ID, STATE
        }),
        new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{USER_COUNT, BLAH_COUNT, CURRENT_VIEWER_COUNT, INBOX_GENERATION_DURATION}),
        new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{COMMENT_MODERATION_STYLE, CONTENT_EXPIRATION_DAYS, RANK, FIRST_INBOX_NUMBER, LAST_INBOX_NUMBER, FIRST_SAFE_INBOX_NUMBER, LAST_SAFE_INBOX_NUMBER}),
        new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[] {PROFILE_QUESTIONS, SHOW_BADGE_LIST, ADMIN, JOIN_BADGE_LIST, COMMENT_BADGE_LIST, POST_BADGE_LIST, POSTS_MODERATION_BADGE_LIST}),
        new SchemaDataTypeFieldMap(SchemaDataType.B, new String[] {SHOWS_AUTHOR_DESC, SHOWS_SPEECH_ACTS, IS_MATURE, IS_PRIVATE}),
        new SchemaDataTypeFieldMap(SchemaDataType.S, new String[] {HEADER_IMAGE,AD_BLAH_ID}),
        new SchemaDataTypeFieldMap(SchemaDataType.DT, new String[]{LAST_TIME_INBOXES_GENERATED})
    };

    public static enum CommentModerationStyle {
        NO_MODERATION(0),
        AUTHOR_MODERATION(1),
        ADMIN_MODERATION(2);

        private final Integer code;

        CommentModerationStyle(Integer theCode) { this.code = theCode; }

        public Integer getValue() {
            return code;
        }
    }

    /**
     * <p>Enumerates the single-character codes for the values for each kind of group descriptor.
     * Each enum's name starts with the name of the descriptor in the descriptor string.</p>
     */
    public static enum GroupDescriptor {
        VISIBILITY_OPEN("a"),
        VISIBILITY_OTHER("o");

        private final String code;

        GroupDescriptor(String code) {
            this.code = code;
        }

        /**
         * <p>Returns the code (value) for the descriptor field.</p>
         * @return  <p>The code (value) for the descriptor field.</p>
         */
        public String getCode() {
            return code;
        }

        /**
         * Returns a descriptor with the specified code if it exists.
         * @param code    A group descriptor's code
         * @return  the group descriptor or null if it doesn't exist
         */
        public static GroupDescriptor findDescriptor(String code) {
            for (GroupDescriptor d : GroupDescriptor.values()) {
                if (d.getCode().equals(code)) {
                    return d;
                }
            }
            return null;
        }
    }
}
