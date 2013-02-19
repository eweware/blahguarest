package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:44 PM
 */
public interface UserBlahInfoDAOConstants {

    /**
     * Id of user acting on this blah
     */
    static final String USER_ID = "userId";

    /**
     * Id of the blah being acted upon.
     */
    static final String BLAH_ID = "blahId";

    /**
     * Id of group into which blah was introduced.
     * This is used by stats.
     */
    static final String ORIGINAL_GROUP_ID = "ogid";

    /**
     * Id of blah type.
     */
    static final String BLAH_TYPE_ID = "btid";

    /**
     * The vote action on this blah by the user.
     * Set to 1 or -1 if the user voted on this blah;
     * else this field is null or 0.
     */
    static final String VOTE = "v";

    /**
     * If this is a poll blah type, indicates whether the user
     * has voted on the poll. The value is the poll option index
     * (an integer) or null if the user has not voted on this poll.
     */
    static final String POLL_VOTE_INDEX = "p";

    /**
     * If POLL_VOTE_INDEX is not null, this is
     * the datetime when the vote was made.
     */
    static final String POLL_VOTE_TIMESTAMP = "t";

    /**
     * The number of views of this blah by the user.
     * If the user hasn't viewed the blah, this
     * field is either null or 0.
     */
    static final String VIEWS = "w";

    /**
     * The number of opens of this blah by the user.
     * If the user hasn't opened the blah, this
     * field is either null or 0.
     */
    static final String OPENS = "o";

    /**
     * Number of times this user has commented on this blah.
     */
    static final String COMMENTS_ON_THIS_BLAH = "c";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USER_ID, BLAH_ID, ORIGINAL_GROUP_ID}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{VOTE, POLL_VOTE_INDEX, VIEWS, OPENS, COMMENTS_ON_THIS_BLAH}),
            new SchemaDataTypeFieldMap(SchemaDataType.DT, new String[]{POLL_VOTE_TIMESTAMP})
    };
}
