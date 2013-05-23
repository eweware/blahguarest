package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/2/12 Time: 3:10 PM
 */
public interface UserProfileDAO extends BaseDAO, UserProfileDAOConstants {

    public String getUserType();

    public void setUserType(String userType);

    public String getNickname();

    public void setNickname(String nickname);

    public Integer getNicknamePermissions();

    public void setNicknamePermissions(Integer p);

    public String getGender();

    public void setGender(String gender);

    public Integer getGenderPermissions();

    public void setGenderPermissions(Integer p);

    public Date getDateOfBirth();

    public void setDateOfBirth(Date dob);

    public Integer getDateOfBirthPermissions();

    public void setDateOfBirthPermissions(Integer p);

    public String getRace();

    public void setRace(String race);

    public Integer getRacePermissions();

    public void setRacePermissions(Integer p);

    public String getIncomeRange();

    public void setIncomeRange(String range);

    public Integer getIncomeRangePermissions();

    public void setIncomeRangePermissions(Integer p);

    public String getGPSLocation();

    public void setGPSLocation(String gpsLocation);

    public Integer getGPSLocationPermissions();

    public void setGPSLocationPermissions(Integer p);

    public String getCity();

    public void setCity(String city);  // TODO this should be canonicalized

    public Integer getCityPermissions();

    public void setCityPermissions(Integer p);

    public String getState();

    public void setState(String state);  // TODO this should be canonicalized

    public Integer getStatePermissions();

    public void setStatePermissions(Integer p);

    public String getZipCode();

    public void setZipCode(String zipCode);  // TODO this should be canonicalized

    public Integer getZipCodePermissions();

    public void setZipCodePermissions(Integer p);

    public String getCountry();

    public void setCountry(String country);

    public Integer getCountryPermissions();

    public void setCountryPermissions(Integer p);
}
