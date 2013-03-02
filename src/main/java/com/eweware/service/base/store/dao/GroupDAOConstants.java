package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 *  <p>Field names and value data types for group entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:43 PM
 */
public interface GroupDAOConstants {

    /**
     * <p> The groups visible display name (English). A String <b>>TODO: i18n</b</p>
     */
    static final String DISPLAY_NAME = "displayName";

    /**
     * <p> An english description. A string. <b>TODO: i18n</b></p>
     */
    static final String DESCRIPTION = "d";

    /**
     * <p>The group's descriptor. Encodes permissions and
     * other group attributes. Each attribute is
     * a single character and the attribute's identity
     * is given by its position in the string.
     * Current structure:</p>
     * <div><visibility></div>
     *
     * <div>There's currently only one attribute:</div>
     * <div><visibility> := {a|o} </div>
     * <p>where 'a' means that the group is open and
     * can be viewed by anonymous users and 'o' means that it can't.
     * All possible descriptor values are specified by the code
     * in GroupDescriptor enum</p>
     * @see GroupDescriptor
     */
    static final String DESCRIPTOR = "s";

    /**
     * <p> The group type id. A string. This is used to group groups.</p>
     */
    static final String GROUP_TYPE_ID = "groupTypeId";

    /**
     * <p> The current number of users in this group. An integer</p>
     */
    static final String USER_COUNT = "ucount";

    /**
     * <p> The current number of blahs in this group. An integer.</p>
     */
    static final String BLAH_COUNT = "bcount";

    /**
     * <p> The current number of users watching this group. An integer.</p>
     */
    static final String CURRENT_VIEWER_COUNT = "v";

    /**
     * <p> This group's validation method. A string</p>
     * <p><b>TODO this will be changed when badging is implemented. Was in use in alpha but is no longer supported.</b></p>
     */
    static final String USER_VALIDATION_METHOD = "vmeth";

    /**
     * <p> This group's validation method parameters. </p>
     * <p><b>TODO this will be changed when badging is implemented. Was in use in alpha but is no longer supported.</b></p>
     */
    static final String USER_VALIDATION_PARAMETERS = "vp";

    /**
     * <p>Group state information.
     * Its value corresponds to an authorization state (e.g., active)</p>
     * @see main.java.com.eweware.service.base.payload.AuthorizedState
     */
    static final String STATE = "state";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
        new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                DISPLAY_NAME, DESCRIPTION, DESCRIPTOR, GROUP_TYPE_ID, USER_VALIDATION_METHOD, USER_VALIDATION_PARAMETERS, STATE
        }),
        new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{USER_COUNT, BLAH_COUNT, CURRENT_VIEWER_COUNT}),
    };



    /**
     * <p>Enumerates the single-character codes for the values for each kind of group descriptor.
     * Each enum's name starts with the name of the descriptor in the descriptor string.</p>
     */
    public static enum GroupDescriptor {
        VISIBILITY_OPEN("a"),
        VISIBILITY_OTHER("o");

        private final String code;

        GroupDescriptor(String code) {
            this.code = code;
        }

        /**
         * <p>Returns the code (value) for the descriptor field.</p>
         * @return  <p>The code (value) for the descriptor field.</p>
         */
        public String getCode() {
            return code;
        }

        /**
         * Returns a descriptor with the specified code if it exists.
         * @param code    A group descriptor's code
         * @return  the group descriptor or null if it doesn't exist
         */
        public static GroupDescriptor findDescriptor(String code) {
            for (GroupDescriptor d : GroupDescriptor.values()) {
                if (d.getCode().equals(code)) {
                    return d;
                }
            }
            return null;
        }
    }
}
