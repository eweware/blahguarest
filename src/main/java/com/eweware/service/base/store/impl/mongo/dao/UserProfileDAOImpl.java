package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserProfileDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

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

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(10);

    static { // TODO should be derived from schema
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_USER_TYPE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RECOVERY_CODE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RECOVER_CODE_SET_METHOD, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_EMAIL_ADDRESS, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_EMAIL_ADDRESS_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_NICKNAME, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_NICKNAME_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_DATE_OF_BIRTH, MongoFieldTypes.DATE);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_DATE_OF_BIRTH_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GENDER, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GENDER_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_CITY, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_CITY_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_STATE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_STATE_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_ZIP_CODE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_ZIP_CODE_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_COUNTRY, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_COUNTRY_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RACE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_RACE_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_INCOME_RANGE, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_INCOME_RANGE_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GPS_LOCATION, MongoFieldTypes.STRING);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(USER_PROFILE_GPS_LOCATION_PUBLIC, MongoFieldTypes.BOOLEAN);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, MongoFieldTypes.DATE);
        UserProfileDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, MongoFieldTypes.DATE);
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

    @Override
    public String getRecoveryCode() {
        return (String) get(USER_PROFILE_RECOVERY_CODE);
    }

    @Override
    public void setRecoveryCode(String code) {
        put(USER_PROFILE_RECOVERY_CODE, code);
    }

    @Override
    public String getRecoverySetMethod() {
        return (String) get(USER_PROFILE_RECOVER_CODE_SET_METHOD);
    }

    @Override
    public void setRecoverySetMethod(String method) {
        put(USER_PROFILE_RECOVER_CODE_SET_METHOD, method);
    }

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
    public Boolean getNicknamePublic() {
        return (Boolean) get(USER_PROFILE_NICKNAME_PUBLIC);
    }

    @Override
    public void setNicknamePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_NICKNAME_PUBLIC, p);
        }
    }

    @Override
    public String getEmailAddress() {
        return (String) get(USER_PROFILE_EMAIL_ADDRESS);
    }

    @Override
    public void setEmailAddress(String emailAddress) {
        put(USER_PROFILE_EMAIL_ADDRESS, emailAddress);
    }

    @Override
    public Boolean getEmailAddressPublic() {
        return (Boolean) get(USER_PROFILE_EMAIL_ADDRESS_PUBLIC);
    }

    @Override
    public void setEmailAddressPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_EMAIL_ADDRESS_PUBLIC, p);
        }
    }

    @Override
    public void setGender(String gender) {
        put(USER_PROFILE_GENDER, gender);
    }

    @Override
    public Boolean getGenderPublic() {
        return (Boolean) get(USER_PROFILE_GENDER_PUBLIC);
    }

    @Override
    public void setGenderPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_GENDER_PUBLIC, p);
        }
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
    public Boolean getDateOfBirthPublic() {
        return (Boolean) get(USER_PROFILE_DATE_OF_BIRTH_PUBLIC);
    }

    @Override
    public void setDateOfBirthPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_DATE_OF_BIRTH_PUBLIC, p);
        }
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
    public Boolean getRacePublic() {
        return (Boolean) get(USER_PROFILE_RACE_PUBLIC);
    }

    @Override
    public void setRacePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_RACE_PUBLIC, p);
        }
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
    public Boolean getIncomeRangePublic() {
        return (Boolean) get(USER_PROFILE_INCOME_RANGE_PUBLIC);
    }

    @Override
    public void setIncomeRangePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_INCOME_RANGE_PUBLIC, p);
        }
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
    public Boolean getGPSLocationPublic() {
        return (Boolean) get(USER_PROFILE_GPS_LOCATION_PUBLIC);
    }

    @Override
    public void setGPSLocationPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_GPS_LOCATION_PUBLIC, p);
        }
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
    public Boolean getCityPublic() {
        return (Boolean) get(USER_PROFILE_CITY_PUBLIC);
    }

    @Override
    public void setCityPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_CITY_PUBLIC, p);
        }
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
    public Boolean getStatePublic() {
        return (Boolean) get(USER_PROFILE_STATE_PUBLIC);
    }

    @Override
    public void setStatePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_STATE_PUBLIC, p);
        }
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
    public Boolean getZipCodePublic() {
        return (Boolean) get(USER_PROFILE_ZIP_CODE_PUBLIC);
    }

    @Override
    public void setZipCodePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_ZIP_CODE_PUBLIC, p);
        }
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
    public Boolean getCountryPublic() {
        return (Boolean) get(USER_PROFILE_COUNTRY_PUBLIC);
    }

    @Override
    public void setCountryPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_COUNTRY_PUBLIC, p);
        }
    }
}
