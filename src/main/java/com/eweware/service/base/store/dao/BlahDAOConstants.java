package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 8:15 PM
 */
public interface BlahDAOConstants {

    static final String TYPE_ID = "typeId";
    static final String TEXT = "text";
    static final String BODY = "b";
    static final String GROUP_ID = "groupId";
    static final String AUTHOR_ID = "authorId";
    static final String VOTES = "votes";  // TODO retire this field: we are now using UP_VOTES and DOWN_VOTES
    static final String UP_VOTES = "vu";
    static final String DOWN_VOTES = "vd";
    static final String VIEWS = "views";
    static final String OPENS = "opens";
    static final String COMMENTS = "c";
    static final String BLAH_STRENGTH = "s";
    static final String RECENT_BLAH_STRENGTH = "r";
    static final String STATS = "stats";
    static final String IMAGE_IDS = "img";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    TYPE_ID, TEXT, BODY, GROUP_ID, AUTHOR_ID}),

            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{
                    VOTES, UP_VOTES, DOWN_VOTES, VIEWS, OPENS, COMMENTS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{
                    BLAH_STRENGTH, RECENT_BLAH_STRENGTH
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new String[]{
                    STATS
            }),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[] {
                    IMAGE_IDS
            }),
    };

}
