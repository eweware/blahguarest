package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 8:22 PM
 */
public interface CommentDAOConstants {

    static final String BLAH_ID = "blahId";
    static final String TEXT = "text";
    static final String AUTHOR_ID = "authorId";
    static final String BLAH_VOTE = "blahVote";
    static final String COMMENT_VOTES = "commentVotes";
    static final String COMMENT_UP_VOTES = "cuv";
    static final String COMMENT_DOWN_VOTES = "cdv";
    static final String COMMENT_STRENGTH = "s";
    static final String VIEWS = "views";
    static final String OPENS = "opens";
    static final String STATS = "stats";
    static final String IMAGE_IDS = "img";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    BLAH_ID, TEXT, AUTHOR_ID
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{
                    BLAH_VOTE, COMMENT_VOTES, COMMENT_UP_VOTES, COMMENT_DOWN_VOTES, VIEWS, OPENS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{
                    COMMENT_STRENGTH
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new String[]{
                    STATS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[] {
                    IMAGE_IDS
            }),
    };
}
