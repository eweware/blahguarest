package com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/26/13 Time: 4:27 PM
 */
public interface UserAccountDAO extends BaseDAO, UserAccountDAOConstants {

    public String getAccountType();

    public void setAccountType(String userAccountType);

    public String getCanonicalUsername();

    public void setCanonicalUsername(String canonicalUsername);

    public String getDigest();

    public void setDigest(String digest);

    public String getSalt();

    public void setSalt(String salt);

    public String getEmailAddress();

    public void setEmailAddress(String emailAddress);

    public Integer getEmailAddressPermissions();

    public void setEmailAddressPermissions(Integer p);

    public String getRecoverySetMethod();

    public void setRecoverySetMethod(String method);

    public Date getRecoveryCodeExpiration();

    public void setRecoveryCodeExpiration(Date expirationDate);

    public String getSecurityChallengeAnswer1();

    public void setSecurityChallengeAnswer1(String answer1);

    public String getRecoveryCode();

    public void setRecoveryCode(String recoveryCode);
}
