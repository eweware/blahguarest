package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/2/12 Time: 3:07 PM
 */
public interface UserProfileDAOConstants {

    // The _id is the userId

    static final String USER_PROFILE_USER_TYPE = "t";

    static final String USER_PROFILE_RECOVERY_CODE = "v";
    static final String USER_PROFILE_RECOVER_CODE_SET_METHOD = "vm";

    static final String USER_PROFILE_EMAIL_ADDRESS = "e";
    static final String USER_PROFILE_EMAIL_ADDRESS_PERMISSIONS = "ep";

    static final String USER_PROFILE_NICKNAME = "n";
    static final String USER_PROFILE_NICKNAME_PERMISSIONS = "np";

    static final String USER_PROFILE_GENDER = "g";
    static final String USER_PROFILE_GENDER_PERMISSIONS = "gp";

    static final String USER_PROFILE_DATE_OF_BIRTH = "d";
    static final String USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS = "dp";

    static final String USER_PROFILE_RACE = "r";
    static final String USER_PROFILE_RACE_PERMISSIONS = "rp";

    static final String USER_PROFILE_INCOME_RANGE = "i";
    static final String USER_PROFILE_INCOME_RANGE_PERMISSIONS = "ip";

    static final String USER_PROFILE_GPS_LOCATION = "l";
    static final String USER_PROFILE_GPS_LOCATION_PERMISSIONS = "lp";

    static final String USER_PROFILE_CITY = "cy";
    static final String USER_PROFILE_CITY_PERMISSIONS = "cyp";

    static final String USER_PROFILE_STATE = "s";
    static final String USER_PROFILE_STATE_PERMISSIONS = "sp";

    static final String USER_PROFILE_ZIP_CODE = "z";
    static final String USER_PROFILE_ZIP_CODE_PERMISSIONS = "zp";

    static final String USER_PROFILE_COUNTRY = "c";
    static final String USER_PROFILE_COUNTRY_PERMISSIONS = "cp";
}
