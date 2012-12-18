package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/16/12 Time: 4:48 PM
 */
public interface BlahTrackerDAOConstants {

    public static final String BT_OBJECT_ID = "id"; // blah id TODO used by unit tests to delete related trackers
    public static final String BT_AUTHOR_ID = "ud";   // blah author id

    public static final String BT_COMMENTS = "c";
    public static final String BT_UP_VOTES = "u";
    public static final String BT_DOWN_VOTES = "d";
    public static final String BT_VIEWS = "v";
    public static final String BT_OPENS = "o";

    public static final String[] BT_FIELD_NAMES = new String[]{
            BT_COMMENTS,
            BT_UP_VOTES,
            BT_DOWN_VOTES,
            BT_VIEWS,
            BT_OPENS,
    };
}
