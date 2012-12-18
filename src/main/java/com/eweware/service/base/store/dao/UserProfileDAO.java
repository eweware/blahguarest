package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/2/12 Time: 3:10 PM
 */
public interface UserProfileDAO extends BaseDAO, UserProfileDAOConstants {

    // public abstract static BaseSchema getSchema();

    public String getUserType();

    public void setUserType(String userType);

    public String getRecoveryCode();

    public void setRecoveryCode(String code);

    public String getRecoverySetMethod();

    public void setRecoverySetMethod(String method);

    public String getNickname();

    public void setNickname(String nickname);

    public Object getNicknamePublic();

    public void setNicknamePublic(Boolean p);

    public String getEmailAddress();

    public void setEmailAddress(String emailAddress);

    public Boolean getEmailAddressPublic();

    public void setEmailAddressPublic(Boolean p);

    public String getGender();

    public void setGender(String gender);

    public Boolean getGenderPublic();

    public void setGenderPublic(Boolean p);

    public Date getDateOfBirth();

    public void setDateOfBirth(Date dob);

    public Boolean getDateOfBirthPublic();

    public void setDateOfBirthPublic(Boolean p);

    public String getRace();

    public void setRace(String race);

    public Boolean getRacePublic();

    public void setRacePublic(Boolean p);

    public String getIncomeRange();

    public void setIncomeRange(String range);

    public Boolean getIncomeRangePublic();

    public void setIncomeRangePublic(Boolean p);

    public String getGPSLocation();

    public void setGPSLocation(String gpsLocation);

    public Boolean getGPSLocationPublic();

    public void setGPSLocationPublic(Boolean p);

    public String getCity();

    public void setCity(String city);  // TODO should this be canonicalized to a code?

    public Boolean getCityPublic();

    public void setCityPublic(Boolean p);

    public String getState();

    public void setState(String state);  // TODO should this be canonicalized to a code?

    public Boolean getStatePublic();

    public void setStatePublic(Boolean p);

    public String getZipCode();

    public void setZipCode(String zipCode);  // TODO should this be canonicalized to a code?

    public Boolean getZipCodePublic();

    public void setZipCodePublic(Boolean p);

    public String getCountry();

    public void setCountry(String country);

    public Boolean getCountryPublic();

    public void setCountryPublic(Boolean p);
}
