package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 11:52 AM
 */
public interface CommentTrackerDAOConstants {

    public static final String CT_OBJECT_ID = "id"; // comment id TODO used by unit tests to delete related trackers
    public static final String CT_AUTHOR_ID = "cid";  // comment author id

    public static final String CT_UP_VOTES_FOR_COMMENT = "u";
    public static final String CT_DOWN_VOTES_FOR_COMMENT = "d";
    public static final String CT_UP_VOTE_FOR_BLAH = "bu";
    public static final String CT_DOWN_VOTE_FOR_BLAH = "bd";
    public static final String CT_VIEWS = "v";
    public static final String CT_OPENS = "o";

    public static final String[] CT_FIELD_NAMES = new String[]{
            CT_UP_VOTES_FOR_COMMENT,
            CT_DOWN_VOTES_FOR_COMMENT,
            CT_UP_VOTE_FOR_BLAH,
            CT_DOWN_VOTE_FOR_BLAH,
            CT_VIEWS,
            CT_OPENS
    };

}
