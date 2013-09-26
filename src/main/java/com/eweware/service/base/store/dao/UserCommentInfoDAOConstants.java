package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

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
    static final String USER_ID = "U";

    /**
     * <p>The comment author's id. A string.</p>
     */
    static final String AUTHOR_ID = "A";

    /**
     * <p>The comment's id. A string.</p>
     */
    static final String COMMENT_ID = "C";

    /**
     * <p>Whether the user has voted on this comment. An integer.</p>
     * <div>Possible values:</div>
     * <div>-1 := voted down</div>
     * <div>0 := did not vote</div>
     * <div>null := did not vote</div>
     * <div>+1 := voted up</div>
     */
    static final String VOTE = "V"; // -1, 0 (or null), +1

    /**
     * <p>Number of times user has viewed this comment. An integer.</p>
     */
    static final String VIEWS = "W";

    /**
     * <p>Number of times user has opened this comment. An integer.</p>
     */
    static final String OPENS = "O";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USER_ID, AUTHOR_ID, COMMENT_ID}),
            new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{VOTE, VIEWS, OPENS})
    };
}
