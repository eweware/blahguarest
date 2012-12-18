package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:45 PM
 */
public interface UserCommentInfoDAOConstants {

    static final String USER_ID = "userId";
    static final String COMMENT_ID = "commentId";
    static final String VOTE = "v"; // -1, 0 (or null), +1
    static final String VIEWS = "w";
    static final String OPENS = "o";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USER_ID, COMMENT_ID}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{VOTE, VIEWS, OPENS})
    };
}
