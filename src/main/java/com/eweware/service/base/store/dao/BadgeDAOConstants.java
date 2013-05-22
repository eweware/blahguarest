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
    static final String AUTHORITY_BADGE_ID = "I";

    /**
     * <p>This is the id of the badging authority record in the Blahgua database.
     * This is the badge authority's major endpoint.</p>
     */
    static final String AUTHORITY_ID = "A";

    /**
     * <p>This is the badge authority's displayable name.</p>
     */
    static final String AUTHORITY_DISPLAY_NAME = "D";

    /**
     * <p>The badge's display name.</p>
     */
    static final String DISPLAY_NAME = "N";

    /**
     * <p>The badge's type</p>
     */
    static final String BADGE_TYPE = "Y";

    /**
     * <p>This field's value is an URL to a 128x128 icon that may be used
     * to represent the badge.</p>
     */
    static final String ICON_URL = "K";

    /**
     * <p>The id of the user to whom this badge has been granted.</p>
     */
    static final String USER_ID = "U";

    /**
     * <p>A date. The expiration date of the badge, if any.</p>
     */
    static final String EXPIRATION_DATE = "X";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    AUTHORITY_BADGE_ID, AUTHORITY_ID, DISPLAY_NAME, BADGE_TYPE, ICON_URL, USER_ID, EXPIRATION_DATE})};

    /**
     * Badge type values
     */
    public static final String BADGE_TYPE_EMAIL = "e";
    public static final String BADGE_TYPE_ABSTRACTION = "a";
}
