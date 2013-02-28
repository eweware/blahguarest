package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:43 PM
 */
public interface GroupDAOConstants {

    /**
     * The groups visible display name (English)
     * TODO L10n
     */
    static final String DISPLAY_NAME = "displayName";

    /**
     * An english description
     * TODO L10n
     */
    static final String DESCRIPTION = "d";

    /**
     * The group's descriptor. Encodes permissions and
     * other group attributes. Each attribute is
     * a single character and the attribute's identity
     * is given by its position in the string.
     * Current structure:
     * <visibility>
     *
     * There's currently only one attribute:
     * <visibility> := {a|o} where 'a' means that the group is open and
     * can be viewed by anonymous users and 'o' means that it can't.
     * All possible descriptor values are specified by the code
     * in GroupDescriptor enum
     * @see GroupDescriptor
     */
    static final String DESCRIPTOR = "s";

    /**
     * Enumerates the single-character codes for the values for each kind of group descriptor.
     * Each enum's name starts with the name of the descriptor in the descriptor string.
     */
    public static enum GroupDescriptor {
        VISIBILITY_OPEN("a"),
        VISIBILITY_OTHER("o");

        private final String code;

        GroupDescriptor(String code) {
            this.code = code;
        }
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

    /**
     * The group type id. This is used to group groups.
     */
    static final String GROUP_TYPE_ID = "groupTypeId";

    /**
     * The current number of users in this group
     */
    static final String USER_COUNT = "ucount";

    /**
     * The current number of blahs in this group
     */
    static final String BLAH_COUNT = "bcount";

    /**
     * The current number of users watching this group.
     */
    static final String CURRENT_VIEWER_COUNT = "v";

    /**
     * This group's validation method
     * TODO this will be changed when badging is implemented. Was in use in alpha but is no longer in use until badging is implemented.
     */
    static final String USER_VALIDATION_METHOD = "vmeth";

    /**
     * This group's validation method parameters.
     * TODO this will be changed when badging is implemented. Was in use in alpha but is no longer in use until badging is implemented.
     */
    static final String USER_VALIDATION_PARAMETERS = "vp";

    /**
     * Group state information.
     * Its value corresponds to an authorization state (e.g., active)
     * @see main.java.com.eweware.service.base.payload.AuthorizedState
     */
    static final String STATE = "state";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
        new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                DISPLAY_NAME, DESCRIPTION, DESCRIPTOR, GROUP_TYPE_ID, USER_VALIDATION_METHOD, USER_VALIDATION_PARAMETERS, STATE
        }),
        new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{USER_COUNT, BLAH_COUNT, CURRENT_VIEWER_COUNT}),
    };
}
