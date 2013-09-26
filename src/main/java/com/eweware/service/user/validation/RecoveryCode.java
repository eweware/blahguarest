package com.eweware.service.user.validation;

import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.error.SystemErrorException;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/3/13 Time: 1:20 PM
 */
public class RecoveryCode {

    /**
     * Recovery codes use this delimiter to separate meaningful segments.
     */
    public static final String ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER = "|";
    private static final String ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER_REGEXP = "\\|";

    /**
     * Creates a recovery code.
     *
     * @param userId            The user's id
     * @param canonicalUsername The user's canonical name
     * @return An encrypted recovery code
     * @throws SystemErrorException If there is a system problem with encryption.
     */
    public static RecoveryCode createRecoveryCode(String userId, String canonicalUsername) throws SystemErrorException {
        final StringBuilder b = new StringBuilder(userId);
        b.append(RecoveryCode.ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER);
        b.append(canonicalUsername);
        return Login.encrypt2Way(b.toString());
    }

    public static RecoveryCodeComponents getRecoveryComponents(String recoveryCode) throws SystemErrorException {
        final String[] foo = recoveryCode.split(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER_REGEXP);
        final String cipher = foo[0];
        final String iv = foo[1];
        final String secretKey = foo[2];
        final String text = Login.decrypt2Way(cipher, iv, secretKey);
        final String[] components = text.split(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER_REGEXP);
        if (components.length < 1) {
            throw new SystemErrorException("recovery components", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        return new RecoveryCodeComponents(components[0], (components.length == 1) ? "" : components[1]);
    }

    /**
     * <p>Creates a string representing the recovery code, including the cipher, iv and secret key components.</p>
     * @return A recovery code string that can be changed back to a recovery object.
     * @see #getRecoveryComponents(String)
     */
    public  final String makeRecoveryCodeString() {
        final StringBuilder b = new StringBuilder();
        b.append(this.getCipherBase64());
        b.append(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER);
        b.append(this.getIvParamBase64());
        b.append(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER);
        b.append(this.getSecretKeyBase64());
        return b.toString();
    }

    private String cipherBase64;
    private String ivParamBase64;
    private String secretKeyBase64;

    public RecoveryCode(String cipherBase64, String ivParamBase64, String secretKeyBase64) {

        this.cipherBase64 = cipherBase64;
        this.ivParamBase64 = ivParamBase64;
        this.secretKeyBase64 = secretKeyBase64;
    }

    public String getIvParamBase64() {
        return ivParamBase64;
    }

    public String getCipherBase64() {
        return cipherBase64;
    }

    public String getSecretKeyBase64() {
        return secretKeyBase64;
    }

    public String toString() {
        return makeRecoveryCodeString();
    }
}
