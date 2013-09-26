package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.UserProfileDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.UserProfileSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/2/12 Time: 3:14 PM
 */
public class UserProfileDAOImpl extends BaseDAOImpl implements UserProfileDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static { // TODO should be derived from schema
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_USER_TYPE, MongoFieldTypes.STRING);
//        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RECOVERY_CODE, MongoFieldTypes.STRING);
//        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RECOVER_CODE_SET_METHOD, MongoFieldTypes.STRING);
//        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RECOVERY_CODE_EXPIRATION_DATE, MongoFieldTypes.DATE);
//        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_CHALLENGE_ANSWER, MongoFieldTypes.STRING);
//        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_EMAIL_ADDRESS, MongoFieldTypes.STRING);
//        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_EMAIL_ADDRESS_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_NICKNAME, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_NICKNAME_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_DATE_OF_BIRTH, MongoFieldTypes.DATE);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GENDER, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GENDER_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_CITY, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_CITY_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_STATE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_STATE_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_ZIP_CODE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_ZIP_CODE_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_COUNTRY, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_COUNTRY_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RACE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RACE_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_INCOME_RANGE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_INCOME_RANGE_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GPS_LOCATION, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GPS_LOCATION_PERMISSIONS, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserProfileDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserProfileDAOImpl.collectionName == null) {
            UserProfileDAOImpl.collectionName = MongoStoreManager.getInstance().getUserProfileCollectionName();
        }
        return UserProfileDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserProfileDAOImpl.collection == null) {
            UserProfileDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return UserProfileDAOImpl.collection;
    }

    UserProfileDAOImpl() {
        super();
    }

    UserProfileDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    UserProfileDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    // Override static
    public static BaseSchema getSchema(LocaleId localeId) {
        return UserProfileSchema.getSchema(localeId);
    }

    @Override
    public String getUserType() {
        return (String) get(USER_PROFILE_USER_TYPE);
    }

    @Override
    public void setUserType(String userType) {
        put(USER_PROFILE_USER_TYPE, userType);
    }

//    @Override
//    public String getRecoverySetMethod() {
//        return (String) get(USER_PROFILE_RECOVER_CODE_SET_METHOD);
//    }
//
//    @Override
//    public void setRecoverySetMethod(String method) {
//        put(USER_PROFILE_RECOVER_CODE_SET_METHOD, method);
//    }
//
//    @Override
//    public Date getRecoveryCodeExpiration() {
//        return (Date) get(USER_PROFILE_RECOVERY_CODE_EXPIRATION_DATE);
//    }
//
//    @Override
//    public void setRecoveryCodeExpiration(Date expirationDate) {
//        put(USER_PROFILE_RECOVERY_CODE_EXPIRATION_DATE, expirationDate);
//    }
//
//    @Override
//    public String getSecurityChallengeAnswer1() {
//        return (String) get(USER_PROFILE_CHALLENGE_ANSWER);
//    }
//
//    @Override
//    public void setSecurityChallengeAnswer1(String answer1) {
//        put(USER_PROFILE_CHALLENGE_ANSWER, answer1);
//    }

    @Override
    public String getGender() {
        return (String) get(USER_PROFILE_GENDER);
    }

    @Override
    public String getNickname() {
        return (String) get(USER_PROFILE_NICKNAME);
    }

    @Override
    public void setNickname(String nickname) {
        put(USER_PROFILE_NICKNAME, nickname);
    }

    @Override
    public Integer getNicknamePermissions() {
        return (Integer) get(USER_PROFILE_NICKNAME_PERMISSIONS);
    }

    @Override
    public void setNicknamePermissions(Integer p) {
        put(USER_PROFILE_NICKNAME_PERMISSIONS, p);
    }

//    @Override
//    public String getEmailAddress() {
//        return (String) get(USER_PROFILE_EMAIL_ADDRESS);
//    }
//
//    @Override
//    public void setEmailAddress(String emailAddress) {
//        put(USER_PROFILE_EMAIL_ADDRESS, emailAddress);
//    }
//
//    @Override
//    public Integer getEmailAddressPermissions() {
//        return (Integer) get(USER_PROFILE_EMAIL_ADDRESS_PERMISSIONS);
//    }
//
//    @Override
//    public void setEmailAddressPermissions(Integer p) {
//        put(USER_PROFILE_EMAIL_ADDRESS_PERMISSIONS, p);
//    }

    @Override
    public void setGender(String gender) {
        put(USER_PROFILE_GENDER, gender);
    }

    @Override
    public Integer getGenderPermissions() {
        return (Integer) get(USER_PROFILE_GENDER_PERMISSIONS);
    }

    @Override
    public void setGenderPermissions(Integer p) {
        put(USER_PROFILE_GENDER_PERMISSIONS, p);
    }

    @Override
    public Date getDateOfBirth() {
        return (Date) get(USER_PROFILE_DATE_OF_BIRTH);
    }

    @Override
    public void setDateOfBirth(Date dob) {
        put(USER_PROFILE_DATE_OF_BIRTH, dob);
    }

    @Override
    public Integer getDateOfBirthPermissions() {
        return (Integer) get(USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS);
    }

    @Override
    public void setDateOfBirthPermissions(Integer p) {
        put(USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS, p);
    }

    @Override
    public String getRace() {
        return (String) get(USER_PROFILE_RACE);
    }

    @Override
    public void setRace(String race) {
        put(USER_PROFILE_RACE, race);
    }

    @Override
    public Integer getRacePermissions() {
        return (Integer) get(USER_PROFILE_RACE_PERMISSIONS);
    }

    @Override
    public void setRacePermissions(Integer p) {
        put(USER_PROFILE_RACE_PERMISSIONS, p);
    }

    @Override
    public String getIncomeRange() {
        return (String) get(USER_PROFILE_INCOME_RANGE);
    }

    @Override
    public void setIncomeRange(String range) {
        put(USER_PROFILE_INCOME_RANGE, range);
    }

    @Override
    public Integer getIncomeRangePermissions() {
        return (Integer) get(USER_PROFILE_INCOME_RANGE_PERMISSIONS);
    }

    @Override
    public void setIncomeRangePermissions(Integer p) {
        put(USER_PROFILE_INCOME_RANGE_PERMISSIONS, p);
    }

    @Override
    public String getGPSLocation() {
        return (String) get(USER_PROFILE_GPS_LOCATION);
    }

    @Override
    public void setGPSLocation(String gpsLocation) {
        put(USER_PROFILE_GPS_LOCATION, gpsLocation);
    }

    @Override
    public Integer getGPSLocationPermissions() {
        return (Integer) get(USER_PROFILE_GPS_LOCATION_PERMISSIONS);
    }

    @Override
    public void setGPSLocationPermissions(Integer p) {
        put(USER_PROFILE_GPS_LOCATION_PERMISSIONS, p);
    }

    @Override
    public String getCity() {
        return (String) get(USER_PROFILE_CITY);
    }

    @Override
    public void setCity(String city) {
        put(USER_PROFILE_CITY, city);
    }

    @Override
    public Integer getCityPermissions() {
        return (Integer) get(USER_PROFILE_CITY_PERMISSIONS);
    }

    @Override
    public void setCityPermissions(Integer p) {
        put(USER_PROFILE_CITY_PERMISSIONS, p);
    }

    @Override
    public String getState() {
        return (String) get(USER_PROFILE_STATE);
    }

    @Override
    public void setState(String state) {
        put(USER_PROFILE_STATE, state);
    }

    @Override
    public Integer getStatePermissions() {
        return (Integer) get(USER_PROFILE_STATE_PERMISSIONS);
    }

    @Override
    public void setStatePermissions(Integer p) {
        put(USER_PROFILE_STATE_PERMISSIONS, p);
    }

    @Override
    public String getZipCode() {
        return (String) get(USER_PROFILE_ZIP_CODE);
    }

    @Override
    public void setZipCode(String zipCode) {
        put(USER_PROFILE_ZIP_CODE, zipCode);
    }

    @Override
    public Integer getZipCodePermissions() {
        return (Integer) get(USER_PROFILE_ZIP_CODE_PERMISSIONS);
    }

    @Override
    public void setZipCodePermissions(Integer p) {
        put(USER_PROFILE_ZIP_CODE_PERMISSIONS, p);
    }

    @Override
    public String getCountry() {
        return (String) get(USER_PROFILE_COUNTRY);
    }

    @Override
    public void setCountry(String country) {
        put(USER_PROFILE_COUNTRY, country);
    }

    @Override
    public Integer getCountryPermissions() {
        return (Integer) get(USER_PROFILE_COUNTRY_PERMISSIONS);
    }

    @Override
    public void setCountryPermissions(Integer p) {
        put(USER_PROFILE_COUNTRY_PERMISSIONS, p);
    }
}
