package com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 11:52 AM
 */
public interface CommentTrackerDAOConstants {

    public static final String CT_OBJECT_ID = "id"; // comment id
    public static final String CT_AUTHOR_ID = "A";  // comment author id

    public static final String CT_UP_VOTES_FOR_COMMENT = "U";
    public static final String CT_DOWN_VOTES_FOR_COMMENT = "D";
    public static final String CT_UP_VOTE_FOR_BLAH = "Y";
    public static final String CT_DOWN_VOTE_FOR_BLAH = "Z";
    public static final String CT_VIEWS = "V";
    public static final String CT_OPENS = "O";

    public static final String[] CT_FIELD_NAMES = new String[]{
            CT_UP_VOTES_FOR_COMMENT,
            CT_DOWN_VOTES_FOR_COMMENT,
            CT_UP_VOTE_FOR_BLAH,
            CT_DOWN_VOTE_FOR_BLAH,
            CT_VIEWS,
            CT_OPENS
    };

}
