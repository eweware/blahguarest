package main.java.com.eweware.service.base.store.dao;

/**
 * <p>The user profile entity contains settings for the user.</p>
 * <p>This entity is i18n-ready. It is therefore essential to
 * use the entity's schema (UserProfileSchema) to interpret
 * the data type for each field. The schema provides the type and
 * displayable strings, as necessary for the current locale.</p>
 * <p>The id key of this entity is the user's id.</p>
 * <p>Each setting might have a permission</p>
 * <p>Fields and data types for this entity.</p>
 * <p><i>Note that the schema is created directly within the UserProfileSchema class</i></p>
 * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
 * @author rk@post.harvard.edu
 *         Date: 9/2/12 Time: 3:07 PM
 */
public interface UserProfileDAOConstants {

    /**
     * <p>The user type. A string.</p>   TODO obsolete this and use the account dao's type!
     * <p>This is currently an ad-hoc field, but the point is distinguish among different
     * types of users who might have different global permissions.</p>
     */
    static final String USER_PROFILE_USER_TYPE = "t";

//    /**
//     * <p>This is an encrypted recovery code for users who have forgotten their passwords.</p>
//     * @see main.java.com.eweware.service.user.validation.Login.RecoveryCode
//     */
//    static final String USER_PROFILE_RECOVERY_CODE = "v";
//    static final String USER_PROFILE_RECOVER_CODE_SET_METHOD = "vm";

//    /**
//     * <p>The date after which the currently set recovery code expires.</p>
//     */
//    static final String USER_PROFILE_RECOVERY_CODE_EXPIRATION_DATE = "x";

//    /**
//     * <p>A challenge security answer from the user. We just have one for now.</p>
//     */
//    static final String USER_PROFILE_CHALLENGE_ANSWER = "a";

//    /**
//     * <p>An optional email address. A string.</p>
//     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
//     */
//    static final String USER_PROFILE_EMAIL_ADDRESS = "e";
//    /**
//     * <p>Permissions associated with the email address field.</p>
//     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
//     */
//    static final String USER_PROFILE_EMAIL_ADDRESS_PERMISSIONS = "ep";

    /**
     * <p>An optional nickname for the user. A string</p>
     * <p>Used as a display string along with blahs, etc.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_NICKNAME = "n";
    /**
     * <p>Permissions associated with the nickname field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_NICKNAME_PERMISSIONS = "np";

    /**
     * <p>An optional gender. A string</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_GENDER = "g";
    /**
     * <p>Permissions associated with the gender field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_GENDER_PERMISSIONS = "gp";

    /**
     * <p>Optional date of birth. A date.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_DATE_OF_BIRTH = "d";
    /**
     * <p>Permissions associated with the date of birth field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS = "dp";

    /**
     * <p>An optional race. A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_RACE = "r";
    /**
     * <p>Permissions associated with the race field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_RACE_PERMISSIONS = "rp";

    /**
     * <p>A possible income range. A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_INCOME_RANGE = "i";
    /**
     * <p>Permissions associated with the income range field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_INCOME_RANGE_PERMISSIONS = "ip";

    /**
     * <p>An optional GPS location. A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_GPS_LOCATION = "l";
    /**
     * <p>Permissions associated with the gps location field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_GPS_LOCATION_PERMISSIONS = "lp";

    /**
     * <p>An optional city. A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_CITY = "cy";
    /**
     * <p>Permissions associated with the city field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_CITY_PERMISSIONS = "cyp";

    /**
     * <p>An optional state (province, department). A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_STATE = "s";
    /**
     * <p>Permissions associated with the state field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_STATE_PERMISSIONS = "sp";

    /**
     * <p>An optional zip code field. A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_ZIP_CODE = "z";
    /**
     * <p>Permissions associated with the zip code field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_ZIP_CODE_PERMISSIONS = "zp";

    /**
     * <p>An optional country. A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    static final String USER_PROFILE_COUNTRY = "c";
    /**
     * <p>Permissions associated with the country field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String USER_PROFILE_COUNTRY_PERMISSIONS = "cp";
}
