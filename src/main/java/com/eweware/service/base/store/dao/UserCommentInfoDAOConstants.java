package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Entity holds information about user's relation to a comment</p>
 * <p>Fields and corresponding data types for this entity.</p>
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:45 PM
 */
public interface UserCommentInfoDAOConstants {

    /**
     * <p>The user's id. A string.</p>
     */
    static final String USER_ID = "userId";

    /**
     * <p>The comment's id. A string.</p>
     */
    static final String COMMENT_ID = "commentId";

    /**
     * <p>Whether the user has voted on this comment. An integer.</p>
     * <div>Possible values:</div>
     * <div>-1 := voted down</div>
     * <div>0 := did not vote</div>
     * <div>null := did not vote</div>
     * <div>+1 := voted up</div>
     */
    static final String VOTE = "v"; // -1, 0 (or null), +1

    /**
     * <p>Number of times user has viewed this comment. An integer.</p>
     */
    static final String VIEWS = "w";

    /**
     * <p>Number of times user has opened this comment. An integer.</p>
     */
    static final String OPENS = "o";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USER_ID, COMMENT_ID}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{VOTE, VIEWS, OPENS})
    };
}
