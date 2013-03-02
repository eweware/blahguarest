package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Field names and corresponding data types for entity used to relate user and blah information.</p>
 * <p>Information is used to check vote, and other permission based on state.</p>
 * <p>A multi-weighed, directed graph of the social network can be created from this information.</p>
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:44 PM
 */
public interface UserBlahInfoDAOConstants {

    /**
     * Id of user acting on this blah.
     * A string.
     */
    static final String USER_ID = "userId";

    /**
     * <p>Id of the blah being acted upon.
     * A string.</p>
     */
    static final String BLAH_ID = "blahId";

    /**
     * <p>Id of group into which blah was introduced.
     * This is used by stats.
     * A string.</p>
     */
    static final String ORIGINAL_GROUP_ID = "ogid";

    /**
     * <p>Id of blah type.
     * A string.</p>.
     */
    static final String BLAH_TYPE_ID = "btid";

    /**
     * <p>The vote action on this blah by the user.
     * Set to 1 or -1 if the user voted on this blah;
     * else this field is null or 0.
     * An integer.</p>
     */
    static final String VOTE = "v";

    /**
     * <p>If this is a poll blah type, indicates whether the user
     * has voted on the poll. The value is the poll option index
     * (an integer) or null if the user has not voted on this poll.
     * An integer.</p>
     */
    static final String POLL_VOTE_INDEX = "p";

    /**
     * <p>If POLL_VOTE_INDEX is not null, this is
     * the datetime when the vote was made.
     * A datetime.</p>
     */
    static final String POLL_VOTE_TIMESTAMP = "t";

    /**
     * <p>The number of views of this blah by the user.
     * If the user hasn't viewed the blah, this
     * field is either null or 0.
     * An integer.</p>
     */
    static final String VIEWS = "w";

    /**
     * <p>The number of opens of this blah by the user.
     * If the user hasn't opened the blah, this
     * field is either null or 0.
     * An integer.</p>
     */
    static final String OPENS = "o";

    /**
     * <p>Number of times this user has commented on this blah.
     * An integer.</p>
     */
    static final String COMMENTS_ON_THIS_BLAH = "c";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USER_ID, BLAH_ID, ORIGINAL_GROUP_ID}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{VOTE, POLL_VOTE_INDEX, VIEWS, OPENS, COMMENTS_ON_THIS_BLAH}),
            new SchemaDataTypeFieldMap(SchemaDataType.DT, new String[]{POLL_VOTE_TIMESTAMP})
    };
}
