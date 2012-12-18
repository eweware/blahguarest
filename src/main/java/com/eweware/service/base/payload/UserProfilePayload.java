package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/2/12 Time: 3:21 PM
 */
public class UserProfilePayload extends BasePayload implements UserProfileDAOConstants {

    public UserProfilePayload() {
        super();
    }

    public UserProfilePayload(String id) {
        super(id);
    }

    public UserProfilePayload(Map<String, Object> map) {
        super(map);
        ensureDates();
    }

    // TODO not too elegant: the payload (client) expects a string representation, not a date. Would be nice to have a per-field autoconversion method
    @JsonIgnore
    private void ensureDates() {
        final Object dob = get(USER_PROFILE_DATE_OF_BIRTH);
        if (dob != null && (dob instanceof  Date)) {
            put(USER_PROFILE_DATE_OF_BIRTH, DateUtils.formatDate((Date) dob));
        }
    }

    public String getUserType() {
        return (String) get(USER_PROFILE_USER_TYPE);
    }

    public void setUserType(String userType) {
        put(USER_PROFILE_USER_TYPE, userType);
    }

    public String getRecoveryCode() {
        return (String) get(USER_PROFILE_RECOVERY_CODE);
    }

    public void setRecoveryCode(String code) {
        put(USER_PROFILE_RECOVERY_CODE, code);
    }

    public String getRecoverySetMethod() {
        return (String) get(USER_PROFILE_RECOVER_CODE_SET_METHOD);
    }

    public void setRecoverySetMethod(String method) {
        put(USER_PROFILE_RECOVER_CODE_SET_METHOD, method);
    }

    public String getNickname() {
        return (String) get(USER_PROFILE_NICKNAME);
    }

    public void setNickname(String nickname) {
        put(USER_PROFILE_NICKNAME, nickname);
    }

    public Boolean getNicknamePublic() {
        return (Boolean) get(USER_PROFILE_NICKNAME_PUBLIC);
    }

    public void setNicknamePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_NICKNAME_PUBLIC, p);
        }
    }

    public String getEmailAddress() {
        return (String) get(USER_PROFILE_EMAIL_ADDRESS);
    }

    public void setEmailAddress(String emailAddress) {
        put(USER_PROFILE_EMAIL_ADDRESS, emailAddress);
    }

    public Boolean getEmailAddressPublic() {
        return (Boolean) get(USER_PROFILE_EMAIL_ADDRESS_PUBLIC);
    }

    public void setEmailAddressPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_EMAIL_ADDRESS_PUBLIC, p);
        }
    }

    public String getGender() {
        return (String) get(USER_PROFILE_GENDER);
    }

    public void setGender(String gender) {
        put(USER_PROFILE_GENDER, gender);
    }

    public Boolean getGenderPublic() {
        return (Boolean) get(USER_PROFILE_GENDER_PUBLIC);
    }

    public void setGenderPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_GENDER_PUBLIC, p);
        }
    }

    public String getDateOfBirth() {
        return (String) get(USER_PROFILE_DATE_OF_BIRTH);
    }

    public void setDateOfBirth(String dob) {
        put(USER_PROFILE_DATE_OF_BIRTH, dob);
    }

    public Boolean getDateOfBirthPublic() {
        return (Boolean) get(USER_PROFILE_DATE_OF_BIRTH_PUBLIC);
    }

    public void setDateOfBirthPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_DATE_OF_BIRTH_PUBLIC, p);
        }
    }

    public String getRace() {
        return (String) get(USER_PROFILE_RACE);
    }

    public void setRace(String race) {
        put(USER_PROFILE_RACE, race);
    }

    public Boolean getRacePublic() {
        return (Boolean) get(USER_PROFILE_RACE_PUBLIC);
    }

    public void setRacePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_RACE_PUBLIC, p);
        }
    }

    public String getIncomeRange() {
        return (String) get(USER_PROFILE_INCOME_RANGE);
    }

    public void setIncomeRange(String range) {
        put(USER_PROFILE_INCOME_RANGE, range);
    }

    public Boolean getIncomeRangePublic() {
        return (Boolean) get(USER_PROFILE_INCOME_RANGE_PUBLIC);
    }

    public void setIncomeRangePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_INCOME_RANGE_PUBLIC, p);
        }
    }

    public String getGPSLocation() {
        return (String) get(USER_PROFILE_GPS_LOCATION);
    }

    public void setGPSLocation(String gpsLocation) {
        put(USER_PROFILE_GPS_LOCATION, gpsLocation);
    }

    public Boolean getGPSLocationPublic() {
        return (Boolean) get(USER_PROFILE_GPS_LOCATION_PUBLIC);
    }

    public void setGPSLocationPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_GPS_LOCATION_PUBLIC, p);
        }
    }

    public String getCity() {
        return (String) get(USER_PROFILE_CITY);
    }

    public void setCity(String city) {
        put(USER_PROFILE_CITY, city);
    }

    public Boolean getCityPublic() {
        return (Boolean) get(USER_PROFILE_CITY_PUBLIC);
    }

    public void setCityPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_CITY_PUBLIC, p);
        }
    }

    public String getState() {
        return (String) get(USER_PROFILE_STATE);
    }

    public void setState(String state) {
        put(USER_PROFILE_STATE, state);
    }

    public Boolean getStatePublic() {
        return (Boolean) get(USER_PROFILE_STATE_PUBLIC);
    }

    public void setStatePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_STATE_PUBLIC, p);
        }
    }

    public String getZipCode() {
        return (String) get(USER_PROFILE_ZIP_CODE);
    }

    public void setZipCode(String zipCode) {
        put(USER_PROFILE_ZIP_CODE, zipCode);
    }

    public Boolean getZipCodePublic() {
        return (Boolean) get(USER_PROFILE_ZIP_CODE_PUBLIC);
    }

    public void setZipCodePublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_ZIP_CODE_PUBLIC, p);
        }
    }

    public String getCountry() {
        return (String) get(USER_PROFILE_COUNTRY);
    }

    public void setCountry(String country) {
        put(USER_PROFILE_COUNTRY, country);
    }

    public Boolean getCountryPublic() {
        return (Boolean) get(USER_PROFILE_COUNTRY_PUBLIC);
    }

    public void setCountryPublic(Boolean p) {
        if (p != null) {
            put(USER_PROFILE_COUNTRY_PUBLIC, p);
        }
    }
}
