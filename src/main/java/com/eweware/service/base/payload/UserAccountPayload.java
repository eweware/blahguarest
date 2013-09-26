package com.eweware.service.base.payload;

import com.eweware.service.base.store.dao.UserAccountDAOConstants;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 5/22/13 Time: 3:20 PM
 */
public class UserAccountPayload extends BasePayload implements UserAccountDAOConstants {


    public String getAccountType() {
        return (String) get(USER_ACCOUNT_TYPE);
    }


    public void setAccountType(String userAccountType) {
        put(USER_ACCOUNT_TYPE, userAccountType);
    }


    public String getCanonicalUsername() {
        return (String) get(CANONICAL_USERNAME);
    }


    public void setCanonicalUsername(String canonicalUsername) {
        put(CANONICAL_USERNAME, canonicalUsername);
    }


    public String getDigest() {
        return (String) get(PASSWORD_DIGEST);
    }


    public void setDigest(String digest) {
        put(PASSWORD_DIGEST, digest);
    }


    public String getSalt() {
        return (String) get(PASSWORD_SALT);
    }


    public void setSalt(String salt) {
        put(PASSWORD_SALT, salt);
    }


    public String getEmailAddress() {
        return (String) get(EMAIL_ADDRESS);
    }


    public void setEmailAddress(String emailAddress) {
        put(EMAIL_ADDRESS, emailAddress);
    }


    public Integer getEmailAddressPermissions() {
        return (Integer) get(EMAIL_ADDRESS_PERMISSIONS);
    }


    public void setEmailAddressPermissions(Integer permissions) {
        put(EMAIL_ADDRESS_PERMISSIONS, permissions);
    }


    public String getRecoverySetMethod() {
        return (String) get(ACCOUNT_RECOVERY_METHOD);
    }


    public void setRecoverySetMethod(String method) {
        put(ACCOUNT_RECOVERY_METHOD, method);
    }


    public Date getRecoveryCodeExpiration() {
        return (Date) get(RECOVERY_CODE_EXPIRATION_DATE);
    }


    public void setRecoveryCodeExpiration(Date expirationDate) {
        put(RECOVERY_CODE_EXPIRATION_DATE, expirationDate);
    }


    public String getSecurityChallengeAnswer1() {
        return (String) get(CHALLENGE_ANSWER_1);
    }


    public void setSecurityChallengeAnswer1(String answer) {
        put(CHALLENGE_ANSWER_1, answer);
    }


    public String getRecoveryCode() {
        return (String) get(RECOVERY_CODE_STRING);
    }


    public void setRecoveryCode(String recoveryCode) {
        put(RECOVERY_CODE_STRING, recoveryCode);
    }

}
