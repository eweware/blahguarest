package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.store.dao.UserAccountDAO;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/26/13 Time: 4:31 PM
 */
public class UserAccountDAOImpl extends BaseDAOImpl implements UserAccountDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(USER_ACCOUNT_TYPE, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(CANONICAL_USERNAME, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(PASSWORD_DIGEST, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(PASSWORD_SALT, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(EMAIL_ADDRESS, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(EMAIL_ADDRESS_PERMISSIONS, MongoFieldTypes.NUMBER);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(ACCOUNT_RECOVERY_METHOD, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(RECOVERY_CODE_STRING, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(RECOVERY_CODE_EXPIRATION_DATE, MongoFieldTypes.DATE);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(CHALLENGE_ANSWER_1, MongoFieldTypes.STRING);

        addInheritedFieldToTypeMapItems(UserAccountDAOImpl.FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserAccountDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserAccountDAOImpl.collectionName == null) {
            UserAccountDAOImpl.collectionName = MongoStoreManager.getInstance().getUserAccountsCollectionName();
        }
        return UserAccountDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserAccountDAOImpl.collection == null) {
            UserAccountDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return UserAccountDAOImpl.collection;
    }

    public UserAccountDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public UserAccountDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    public UserAccountDAOImpl() {
    }

    @Override
    public String getAccountType() {
        return (String) get(USER_ACCOUNT_TYPE);
    }

    @Override
    public void setAccountType(String userAccountType) {
        put(USER_ACCOUNT_TYPE, userAccountType);
    }

    @Override
    public String getCanonicalUsername() {
        return (String) get(CANONICAL_USERNAME);
    }

    @Override
    public void setCanonicalUsername(String canonicalUsername) {
        put(CANONICAL_USERNAME, canonicalUsername);
    }

    @Override
    public String getDigest() {
        return (String) get(PASSWORD_DIGEST);
    }

    @Override
    public void setDigest(String digest) {
        put(PASSWORD_DIGEST, digest);
    }

    @Override
    public String getSalt() {
        return (String) get(PASSWORD_SALT);
    }

    @Override
    public void setSalt(String salt) {
        put(PASSWORD_SALT, salt);
    }

    @Override
    public String getEmailAddress() {
        return (String) get(EMAIL_ADDRESS);
    }

    @Override
    public void setEmailAddress(String emailAddress) {
        put(EMAIL_ADDRESS, emailAddress);
    }

    @Override
    public Integer getEmailAddressPermissions() {
        return (Integer) get(EMAIL_ADDRESS_PERMISSIONS);
    }

    @Override
    public void setEmailAddressPermissions(Integer permissions) {
        put(EMAIL_ADDRESS_PERMISSIONS, permissions);
    }

    @Override
    public String getRecoverySetMethod() {
        return (String) get(ACCOUNT_RECOVERY_METHOD);
    }

    @Override
    public void setRecoverySetMethod(String method) {
        put(ACCOUNT_RECOVERY_METHOD, method);
    }

    @Override
    public Date getRecoveryCodeExpiration() {
        return (Date) get(RECOVERY_CODE_EXPIRATION_DATE);
    }

    @Override
    public void setRecoveryCodeExpiration(Date expirationDate) {
        put(RECOVERY_CODE_EXPIRATION_DATE, expirationDate);
    }

    @Override
    public String getSecurityChallengeAnswer1() {
        return (String) get(CHALLENGE_ANSWER_1);
    }

    @Override
    public void setSecurityChallengeAnswer1(String answer) {
        put(CHALLENGE_ANSWER_1, answer);
    }

    @Override
    public String getRecoveryCode() {
        return (String) get(RECOVERY_CODE_STRING);
    }

    @Override
    public void setRecoveryCode(String recoveryCode) {
        put(RECOVERY_CODE_STRING, recoveryCode);
    }
}
