package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.type.FieldDescriptor;
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

    public static final String UNSPECIFIED_SELECTION_KEY = "-1";

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
//        schema.createRecoveryCodeSpec();
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
//        schema.createEmailAddressSpec();

        cacheSchema(UserProfileSchema.class, localeId, schema);

        return schema;
    }

//    private void createRecoveryCodeSpec() {
//        final String regexp = null;
//        final LinkedHashMap<String, Object> data = null;
//        final boolean hasDefaultValue = false;
//        createSpec(SchemaDataType.S, USER_PROFILE_RECOVERY_CODE, "Recovery Code", regexp, data, hasDefaultValue, null);
//        createSpec(SchemaDataType.S, USER_PROFILE_RECOVER_CODE_SET_METHOD, "RC Method", null, null, false, null);
//        createSpec(SchemaDataType.DT, USER_PROFILE_RECOVERY_CODE_EXPIRATION_DATE, "Recovery Exp", null, null, false, null);
//        createSpec(SchemaDataType.S, USER_PROFILE_CHALLENGE_ANSWER, "Sec Ans", null, null, false, null);
//    }

    private void createUserTypeSpec() {
        final String defaultValue = "0";
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("0", "User"); // TODO obtain from i18n service
        data.put("1", "Admin");
        data.put("2", "Sponsor");
        data.put("3", "Pro User");
        final String regexp = null;
        final boolean hasDefaultValue = true;
        createSpec(SchemaDataType.ILS, new FieldDescriptor(USER_PROFILE_USER_TYPE), "User Type", null, regexp, data, hasDefaultValue, defaultValue);
    }

    private void createNicknameSpec() {
        final String regexp = null;
        final LinkedHashMap<String, Object> data = null;
        final boolean hasDefaultValue = true;
        createSpec(SchemaDataType.S, new FieldDescriptor(USER_PROFILE_NICKNAME), "Nickname", null, regexp, data, hasDefaultValue, null);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_NICKNAME_PERMISSIONS), "Permissions", null, regexp, data, hasDefaultValue, UserProfilePermissions.PRIVATE);
    }

    private void createCountrySpec() { // ISO 3166-1 alpha-2 code
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>(2);
        data.put(UNSPECIFIED_SELECTION_KEY, "Unspecified");
        data.put("AU", "Australia"); // TODO obtain from i18n service
        data.put("BR", "Brazil");
        data.put("CA", "Canada");
        data.put("CN", "China (PRC)");
        data.put("JP", "Japan");
        data.put("SG", "Singapore");
        data.put("KR", "South Korea");
        data.put("TW", "Taiwan");
        data.put("TH", "Thailand");
        data.put("GB", "United Kingdom");
        data.put("US", "United States");
        final String regexp = null;
        final boolean hasDefaultValue = true;
        createSpec(SchemaDataType.ILS, new FieldDescriptor(USER_PROFILE_COUNTRY), "Country", null, regexp, data, hasDefaultValue, UNSPECIFIED_SELECTION_KEY);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_COUNTRY_PERMISSIONS), "Permissions", null, regexp, null, hasDefaultValue, UserProfilePermissions.PRIVATE);
    }

    private void createZipCodeSpec() {
        final LinkedHashMap<String, Object> data = null;
        final boolean hasDefaultValue = true;
        final Object defaultValue = null;
        final String regexp = "^\\d{5}(?:[-\\s]\\d{4})?$";
        createSpec(SchemaDataType.S, new FieldDescriptor(USER_PROFILE_ZIP_CODE), "Zip Code", null, regexp, data, hasDefaultValue, defaultValue);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_ZIP_CODE_PERMISSIONS), "Permissions", null, null, data, hasDefaultValue, UserProfilePermissions.PRIVATE);
    }

    private void createStateSpec() {
        createSpec(SchemaDataType.S, new FieldDescriptor(USER_PROFILE_STATE), "State", null, null, null, true, null);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_STATE_PERMISSIONS), "Permissions", null, null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createCitySpec() {
        createSpec(SchemaDataType.S, new FieldDescriptor(USER_PROFILE_CITY), "City", null, null, null, true, null);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_CITY_PERMISSIONS), "Permissions", null, null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createDateOfBirthSpec() {
        createSpec(SchemaDataType.D, new FieldDescriptor(USER_PROFILE_DATE_OF_BIRTH), "Birth Date", null, DateUtils.ISO_DATE_FORMAT_REGEXP, null, true, null);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS), "Permissions", null, null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createGPSLocationSpec() {
        createSpec(SchemaDataType.GPS, new FieldDescriptor(USER_PROFILE_GPS_LOCATION), "GPS", null, null, null, true, null);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_GPS_LOCATION_PERMISSIONS), "Permissions", null, null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createIncomeRangeSpec() {
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>(10);
        data.put("0", "Under $24.9K"); // TODO obtain from i18n service
        data.put("1", "$25K to $49.9K");
        data.put("2", "$50K to $74.9K");
        data.put("3", "$75K to $99.9K");
        data.put("4", "$100K to $149.9K");
        data.put("5", "$150K to $199.9K");
        data.put("6", "$200K and above");
        data.put(UNSPECIFIED_SELECTION_KEY, "Unspecified");
        createSpec(SchemaDataType.ILS, new FieldDescriptor(USER_PROFILE_INCOME_RANGE), "Income Range", null, null, data, true, UNSPECIFIED_SELECTION_KEY);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_INCOME_RANGE_PERMISSIONS), "Permissions", null, null, null, true, UserProfilePermissions.PRIVATE);
    }

    public static final String RACE_OTHER_SELECTION_KEY = "4";

    private void createRaceSpec() {
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>(6);
        data.put("0", "Asian");  // TODO obtain from i18n service
        data.put("1", "Black");
        data.put("2", "Hispanic");
        data.put("3", "White");
        data.put(RACE_OTHER_SELECTION_KEY, "Other");
        data.put(UNSPECIFIED_SELECTION_KEY, "Unspecified");
        createSpec(SchemaDataType.ILS, new FieldDescriptor(USER_PROFILE_RACE), "Race", null, null, data, true, UNSPECIFIED_SELECTION_KEY);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_RACE_PERMISSIONS), "Permissions", null, null, null, true, UserProfilePermissions.PRIVATE);
    }

    private void createGenderSpec() {
        LinkedHashMap<String, Object> genderData = new LinkedHashMap<String, Object>(2);
        genderData.put("0", "Male"); // TODO obtain from i18n service
        genderData.put("1", "Female");  // TODO obtain from i18n service
        genderData.put(UNSPECIFIED_SELECTION_KEY, "Unspecified");  // TODO obtain from i18n service
        createSpec(SchemaDataType.ILS, new FieldDescriptor(USER_PROFILE_GENDER), "Gender", null, null, genderData, true, UNSPECIFIED_SELECTION_KEY);
        createSpec(SchemaDataType.I, new FieldDescriptor(USER_PROFILE_GENDER_PERMISSIONS), "Permissions", null, null, null, true, UserProfilePermissions.PRIVATE);
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