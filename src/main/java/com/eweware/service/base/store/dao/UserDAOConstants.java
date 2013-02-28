package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/31/12 Time: 4:21 PM
 */
public interface UserDAOConstants {

    /** not part of DAO or Payload class */
    static final String PASSWORD = "pwd";

    static final String USERNAME = "displayName";
    static final String LAST_INBOX = "li";
    static final String STATS = "stats";
    static final String USER_STRENGTH = "s";
    static final String USER_CONTROVERSY_STRENGTH = "cs";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
      new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USERNAME}),
      new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{LAST_INBOX}),
      new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{USER_STRENGTH, USER_CONTROVERSY_STRENGTH}),
      new SchemaDataTypeFieldMap(SchemaDataType.E, new String[]{STATS}),
    };
}
