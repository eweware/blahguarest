package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * Created by Dave on 1/25/14.
 */
public interface WhatsNewDAOConstants {
    static final String MESSAGE = "message";

    static final String NEW_COMMENTS = "newComments";

    static final String NEW_OPENS = "newOpens";

    static final String NEW_UP_VOTES = "newUpVotes";

    static final String NEW_DOWN_VOTES = "newDownVotes";

    static final String NEW_MESSAGES = "newMessages";

    static final String TARGET_USER = "U";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{MESSAGE}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{NEW_COMMENTS, TARGET_USER, NEW_OPENS, NEW_UP_VOTES, NEW_DOWN_VOTES, NEW_MESSAGES})
    };
}

