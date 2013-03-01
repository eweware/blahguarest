package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions;

import java.util.LinkedHashMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/6/12 Time: 5:02 PM
 *         <p/>
 *         This is the first schema object.
 *         TODO All creation of singletons should be data-driven from the database, via a cache service if necessary
 *         TODO Dynamically generate the class from the db spec for the dao type (here, UserProfileDAO)? May be overkill.
 *
 */
public class UserProfileSchema extends BaseSchema implements SchemaConstants, UserProfileDAOConstants {

    protected UserProfileSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final UserProfileSchema getSchema(LocaleId localeId) {

        UserProfileSchema schema = (UserProfileSchema) BaseSchema.getCachedSchema(UserProfileSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new UserProfileSchema(localeId);

        schema.createUserTypeSpec();
        schema.createRecoveryCodeSpec();
        schema.createGenderSpec();
        schema.createRaceSpec();
        schema.createIncomeRangeSpec();
        schema.createGPSLocationSpec();
        schema.createDateOfBirthSpec();
        schema.createCitySpec();
        schema.createStateSpec();
        schema.createZipCodeSpec();
        schema.createCountrySpec();
        schema.createNicknameSpec();
        schema.createEmailAddressSpec();

        cacheSchema(UserProfileSchema.class, localeId, schema);

        return schema;
    }

    private void createRecoveryCodeSpec() {
        final String regexp = null;
        final LinkedHashMap<String, Object> data = null;
        final boolean hasDefaultValue = false;
        createSpec(SchemaDataType.S, USER_PROFILE_RECOVERY_CODE, "Recovery Code", regexp, data, hasDefaultValue, null);

        createSpec(SchemaDataType.S, USER_PROFILE_RECOVER_CODE_SET_METHOD, "RC Method", null, null, false, null);
    }

    private void createUserTypeSpec() {
        final String defaultValue = "0";
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("0", "User"); // TODO obtain from i18n service
        data.put("1", "Admin");
        data.put("2", "Sponsor");
        data.put("3", "Pro User");
        final String regexp = null;
        final boolean hasDefaultValue = true;
        createSpec(SchemaDataType.ILS, USER_PROFILE_USER_TYPE, "User Type", regexp, data, hasDefaultValue, defaultValue);
    }

    private void createNicknameSpec() {
        final String regexp = null;
        final LinkedHashMap<String, Object> data = null;
        final boolean hasDefaultValue = true;
        createSpec(SchemaDataType.S, USER_PROFILE_NICKNAME, "Nickname", regexp, data, hasDefaultValue, null);
        createSpec(SchemaDataType.I, USER_PROFILE_NICKNAME_PERMISSIONS, "Permissions", regexp, data, hasDefaultValue, UserProfilePermissions.PRIVATE);
    }

    private void createEmailAddressSpec() {
        final String regexp = null;
        final LinkedHashMap<String, Object> data = null;
        final boolean hasDefaultValue = true;
        createSpec(SchemaDataType.S, USER_PROFILE_EMAIL_ADDRESS, "Email Address", regexp, data, hasDefaultValue, null);
        createSpec(SchemaDataType.I, USER_PROFILE_EMAIL_ADDRESS_PERMISSIONS, "Permissions", regexp, data, hasDefaultValue, UserProfilePermissions.PRIVATE);
    }

    private void createCountrySpec() { // ISO 3166-1 alpha-2 code
        final String defaultValue = "-1";
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>(2);
        data.put(defaultValue, "Unspecified");
        data.put("AU", "Australia"); // TODO obtain from i18n service
        data.put("CA", "Canada");
        data.put("CN", "China (PRC)");
        data.put("GB", "United Kingdom");
        data.put("SG", "Singapore");
        data.put("TW", "Taiwan");
        data.put("US", "United States");
        final String regexp = null;
        final boolean hasDefaultValue = true;
        createSpec(SchemaDataType.ILS, USER_PROFILE_COUNTRY, "Country", regexp, data, hasDefaultValue, defaultValue);
        createSpec(SchemaDataType.I, USER_PROFILE_COUNTRY_PERMISSIONS, "Permissions", regexp, null, hasDefaultValue, UserProfilePermissions.PRIVATE);
    }

    private void createZipCodeSpec() {
        final LinkedHashMap<String, Object> data = null;
        final boolean hasDefaultValue = true;
        final Object defaultValue = null;
        final String regexp = "^\\d{5}(?:[-\\s]\\d{4})?$";
        createSpec(SchemaDataType.S, USER_PROFILE_ZIP_CODE, "Zip Code", regexp, data, hasDefaultValue, defaultValue);
        createSpec(SchemaDataType.I, USER_PROFILE_ZIP_CODE_PERMISSIONS, "Permissions", null, data, hasDefaultValue, UserProfilePermissions.PRIVATE);
    }

    private void createStateSpec() {
        createSpec(SchemaDataType.S, USER_PROFILE_STATE, "State", null, null, true, null);
        createSpec(SchemaDataType.I, USER_PROFILE_STATE_PERMISSIONS, "Permissions", null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createCitySpec() {
        createSpec(SchemaDataType.S, USER_PROFILE_CITY, "City", null, null, true, null);
        createSpec(SchemaDataType.I, USER_PROFILE_CITY_PERMISSIONS, "Permissions", null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createDateOfBirthSpec() {
        createSpec(SchemaDataType.D, USER_PROFILE_DATE_OF_BIRTH, "Birth Date", DateUtils.ISO_DATE_FORMAT_REGEXP, null, true, null);
        createSpec(SchemaDataType.I, USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS, "Permissions", null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createGPSLocationSpec() {
        createSpec(SchemaDataType.GPS, USER_PROFILE_GPS_LOCATION, "GPS", null, null, true, null);
        createSpec(SchemaDataType.I, USER_PROFILE_GPS_LOCATION_PERMISSIONS, "Permissions", null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createIncomeRangeSpec() {
        final String defaultValue = "-1";
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>(10);
        data.put("0", "Under $2,500"); // TODO obtain from i18n service
        data.put("1", "$2,500 to $9,999");
        data.put("2", "$10,000 to $24,999");
        data.put("3", "$25,000 to $49,999");
        data.put("4", "$50,000 to $74,999");
        data.put("5", "$75,000 to $99,999");
        data.put("6", "$100,000 to $149,999");
        data.put("7", "$150,000 to $199,999");
        data.put("8", "$200,000 to $249,999");
        data.put("9", "$250,000 and above");
        data.put(defaultValue, "Unspecified");
        createSpec(SchemaDataType.ILS, USER_PROFILE_INCOME_RANGE, "Income Range", null, data, true, defaultValue);
        createSpec(SchemaDataType.I, USER_PROFILE_INCOME_RANGE_PERMISSIONS, "Permissions", null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createRaceSpec() {
        final String defaultValue = "-1";
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>(6);
        data.put("0", "American Indian or Alaskan Native");  // TODO obtain from i18n service
        data.put("1", "Asian");
        data.put("2", "Black or African American");
        data.put("3", "Native Hawaiian or Pacific Islander");
        data.put("4", "White");
        data.put("5", "Other");
        data.put(defaultValue, "Unspecified");
        createSpec(SchemaDataType.ILS, USER_PROFILE_RACE, "Race", null, data, true, defaultValue);
        createSpec(SchemaDataType.I, USER_PROFILE_RACE_PERMISSIONS, "Permissions", null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createGenderSpec() {
        final String defaultValue = "-1";
        LinkedHashMap<String, Object> genderData = new LinkedHashMap<String, Object>(2);
        genderData.put("0", "Male"); // TODO obtain from i18n service
        genderData.put("1", "Female");  // TODO obtain from i18n service
        genderData.put(defaultValue, "Unspecified");  // TODO obtain from i18n service
        createSpec(SchemaDataType.ILS, USER_PROFILE_GENDER, "Gender", null, genderData, true, defaultValue);
        createSpec(SchemaDataType.I, USER_PROFILE_GENDER_PERMISSIONS, "Permissions", null, null, true, UserProfilePermissions.PRIVATE);
    }
}

// ethnicity:
//        data.put(0, "Arab");
//        data.put(1, "Chinese");
//        data.put(2, "Cuban");
//        data.put(3, "Fillipino");
//        data.put(4, "Hispanic or Latino Americans");
//        data.put(5, "Indian (India)");
//        data.put(6, "Japanese");
//        data.put(7, "Other");