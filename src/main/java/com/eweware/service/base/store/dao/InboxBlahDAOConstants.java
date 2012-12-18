package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/30/12 Time: 11:58 AM
 */
public interface InboxBlahDAOConstants {

    static final String BLAH_ID = "blahId";
    static final String AUTHOR_ID = "authorId";
    static final String TYPE = "type";
    static final String BLAH_TEXT = "text";
    static final String GROUP_ID = "groupId";
    static final String INBOX_NUMBER = "sn";
    static final String UP_VOTES = "u";
    static final String DOWN_VOTES = "d";
    static final String VIEWS = "v";
    static final String OPENS = "o";
    static final String COMMENTS = "c";
    static final String BLAH_STRENGTH = "s";
    static final String RECENT_BLAH_STRENGTH = "r";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{BLAH_ID, AUTHOR_ID, TYPE, BLAH_TEXT, GROUP_ID}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{INBOX_NUMBER, UP_VOTES, DOWN_VOTES, VIEWS, OPENS, COMMENTS}),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{BLAH_STRENGTH, RECENT_BLAH_STRENGTH}),
    };
}
