package com.eweware.service.base.payload;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.UserProfileDAOConstants;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.UserProfileSchema;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/2/12 Time: 3:21 PM
 */
public class UserProfilePayload extends BasePayload implements UserProfileDAOConstants {

    protected static final BaseSchema getSchema() {
        return UserProfileSchema.getSchema(LocaleId.en_us);
    }

    public UserProfilePayload() {
        super();
    }

    public UserProfilePayload(String id) {
        super(id);
    }

    public UserProfilePayload(Map<String, Object> map) {
        super(map);
        ensureCanonicalDateDate(USER_PROFILE_DATE_OF_BIRTH);  // TODO use schema for this!
    }

    public String getUserType() {
        return (String) get(USER_PROFILE_USER_TYPE);
    }

    public void setUserType(String userType) {
        put(USER_PROFILE_USER_TYPE, userType);
    }

    public String getNickname() {
        return (String) get(USER_PROFILE_NICKNAME);
    }

    public void setNickname(String nickname) {
        put(USER_PROFILE_NICKNAME, nickname);
    }

    public Boolean getNicknamePublic() {
        return (Boolean) get(USER_PROFILE_NICKNAME_PERMISSIONS);
    }

    public void setNicknamePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_NICKNAME_PERMISSIONS, p);
        }
    }

    public String getGender() {
        return (String) get(USER_PROFILE_GENDER);
    }

    public void setGender(String gender) {
        put(USER_PROFILE_GENDER, gender);
    }

    public Boolean getGenderPublic() {
        return (Boolean) get(USER_PROFILE_GENDER_PERMISSIONS);
    }

    public void setGenderPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_GENDER_PERMISSIONS, p);
        }
    }

    public String getDateOfBirth() {
        return (String) get(USER_PROFILE_DATE_OF_BIRTH);
    }

    public void setDateOfBirth(String dob) {
        put(USER_PROFILE_DATE_OF_BIRTH, dob);
    }

    public Boolean getDateOfBirthPublic() {
        return (Boolean) get(USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS);
    }

    public void setDateOfBirthPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_DATE_OF_BIRTH_PERMISSIONS, p);
        }
    }

    public String getRace() {
        return (String) get(USER_PROFILE_RACE);
    }

    public void setRace(String race) {
        put(USER_PROFILE_RACE, race);
    }

    public Boolean getRacePublic() {
        return (Boolean) get(USER_PROFILE_RACE_PERMISSIONS);
    }

    public void setRacePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_RACE_PERMISSIONS, p);
        }
    }

    public String getIncomeRange() {
        return (String) get(USER_PROFILE_INCOME_RANGE);
    }

    public void setIncomeRange(String range) {
        put(USER_PROFILE_INCOME_RANGE, range);
    }

    public Boolean getIncomeRangePublic() {
        return (Boolean) get(USER_PROFILE_INCOME_RANGE_PERMISSIONS);
    }

    public void setIncomeRangePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_INCOME_RANGE_PERMISSIONS, p);
        }
    }

    public String getGPSLocation() {
        return (String) get(USER_PROFILE_GPS_LOCATION);
    }

    public void setGPSLocation(String gpsLocation) {
        put(USER_PROFILE_GPS_LOCATION, gpsLocation);
    }

    public Boolean getGPSLocationPublic() {
        return (Boolean) get(USER_PROFILE_GPS_LOCATION_PERMISSIONS);
    }

    public void setGPSLocationPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_GPS_LOCATION_PERMISSIONS, p);
        }
    }

    public String getCity() {
        return (String) get(USER_PROFILE_CITY);
    }

    public void setCity(String city) {
        put(USER_PROFILE_CITY, city);
    }

    public Boolean getCityPublic() {
        return (Boolean) get(USER_PROFILE_CITY_PERMISSIONS);
    }

    public void setCityPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_CITY_PERMISSIONS, p);
        }
    }

    public String getState() {
        return (String) get(USER_PROFILE_STATE);
    }

    public void setState(String state) {
        put(USER_PROFILE_STATE, state);
    }

    public Boolean getStatePublic() {
        return (Boolean) get(USER_PROFILE_STATE_PERMISSIONS);
    }

    public void setStatePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_STATE_PERMISSIONS, p);
        }
    }

    public String getZipCode() {
        return (String) get(USER_PROFILE_ZIP_CODE);
    }

    public void setZipCode(String zipCode) {
        put(USER_PROFILE_ZIP_CODE, zipCode);
    }

    public Boolean getZipCodePublic() {
        return (Boolean) get(USER_PROFILE_ZIP_CODE_PERMISSIONS);
    }

    public void setZipCodePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_ZIP_CODE_PERMISSIONS, p);
        }
    }

    public String getCountry() {
        return (String) get(USER_PROFILE_COUNTRY);
    }

    public void setCountry(String country) {
        put(USER_PROFILE_COUNTRY, country);
    }

    public Boolean getCountryPublic() {
        return (Boolean) get(USER_PROFILE_COUNTRY_PERMISSIONS);
    }

    public void setCountryPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_COUNTRY_PERMISSIONS, p);
        }
    }
}
