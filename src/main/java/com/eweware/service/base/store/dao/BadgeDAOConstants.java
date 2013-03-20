package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>The field names for a badge.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 7:17 PM
 */
public interface BadgeDAOConstants {

    /**
     * <p>This is the badge authority's own badge id.</p>
     */
    static final String BADGE_ID = "I";

    /**
     * <p>This is the id of the badging authority record in the Blahgua database.</p>
     */
    static final String AUTHORITY_ID = "A";

    /**
     * <p>The badge's display name.</p>
     */
    static final String DISPLAY_NAME = "N";

    /**
     * <p>A brief description of the badge.</p>
     */
    static final String DESCRIPTION = "D";

    /**
     * <p>This field indicates whether a request to create a badge has been
     * accepted or refused. The possible values of this field are 'y', 'n', or 'c',
     * for accepted refused, or cancelled by user, respectively.</p>
     */
    static final String STATE = "S";

    /**
     * <p>If the state field's value is 'n', this might explain why
     * the request was refused.</p>
     */
    static final String REASON = "R";

    /**
     * <p>This field's value is an URL to a 128x128 icon that may be used
     * to represent the badge.</p>
     */
    static final String ICON_URL = "K";

    /**
     * <p>This field's value is a common token that is used through which
     * the authority and Blahgua exchange information about the user. This
     * is a permanent token.</p>
     */
    static final String USER_TOKEN = "T";


    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    BADGE_ID, AUTHORITY_ID, DISPLAY_NAME,  DESCRIPTION, STATE,  REASON, ICON_URL, USER_TOKEN})
    };
}
