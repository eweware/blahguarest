package main.java.com.eweware.service.user.validation;


import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 1:04 AM
 *         <p/>
 *         version 1: uses OWASP recommendations
 */
public final class Login {

    private final static int ITERATIONS = 1000;
    private static final String TWO_WAY_CRYPT_METHOD = "PBEWithMD5AndDES";

    /**
     * Recovery codes use this delimiter to separate meaningful segments.
     */
    private static final String ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER = "|";
    private static final String ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER_REGEXP = "\\|";
    private static final String EMPTY_STRING = "";

    /**
     * Authenticates the user with the given password.
     *
     * @param digest   The digest from the db
     * @param salt     The salt from the db
     * @param password String The password supplied by the user
     * @return boolean Returns true if the user is authenticated
     * @throws NoSuchAlgorithmException If the algorithm SHA-1 is not supported by the JVM
     */
    public static boolean authenticate(String digest, String salt, final String password) throws SystemErrorException {
        try {
            if (CommonUtilities.isEmptyString(digest) || CommonUtilities.isEmptyString(salt)) {
                throw new SystemErrorException("Missing validation data", ErrorCodes.SERVER_SEVERE_ERROR);
            }

            final byte[] proposedDigest = getHash(ITERATIONS, password, Base64.decodeBase64(salt));

            return Arrays.equals(proposedDigest, Base64.decodeBase64(digest));

        } catch (Exception ex) {
            throw new SystemErrorException("Unable to support validation", ex, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }


    /**
     * Creates a digest and salt for the password
     *
     * @param password String The password of the user
     * @return String[] Returns the digest and salt strings, in this order.
     * @throws NoSuchAlgorithmException If the algorithm SHA-1 or the SecureRandom is not supported by the JVM
     */
    public static String[] createSaltedPassword(final String password)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        final byte[] bSalt = new byte[8];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(bSalt);

        final byte[] bDigest = getHash(ITERATIONS, password, bSalt);

        return new String[]{new String(Base64.encodeBase64(bDigest)), new String(Base64.encodeBase64(bSalt))};
    }


    /**
     * From a password, a number of iterations and a salt,
     * returns the corresponding digest
     *
     * @param iterationNb int The number of iterations of the algorithm
     * @param password    String The password to encrypt2Way
     * @param salt        byte[] The salt
     * @return byte[] The digested password
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist
     */
    public static byte[] getHash(final int iterationNb, final String password, final byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(salt);
        byte[] input = digest.digest(password.getBytes("UTF-8"));
        for (int i = 0; i < iterationNb; i++) {
            digest.reset();
            input = digest.digest(input);
        }
        return input;
    }

    /**
     * Checks password string and returns appropriate version of it
     * @param password  The password. If null, it is interpreted as ""
     * @return  The validated version of the password.
     * @throws InvalidRequestException  Thrown if the password is unacceptable
     */
    public static String ensurePasswordString(String password) throws InvalidRequestException {
        if (password == null) {password = "";} // empty string
        if (!   CommonUtilities.checkString(password, 0, 64)) {
            throw new InvalidRequestException("Invalid password. Length must not exceed 64.", ErrorCodes.INVALID_PASSWORD);
        }
        return password;
    }

    /**
     * Checks username string and returns appropriate version of it
     * @param username  The username. If null, it is interpreted as ""
     * @return  The validated version of the username.
     * @throws InvalidRequestException  Thrown if the username is unacceptable
     */
    public static String ensureUsernameString(String username) throws InvalidRequestException {
        if (!CommonUtilities.checkString(username, 3, 32)) {
            throw new InvalidRequestException("Invalid username '" + username + "'. Length must be less than 33 and more than 2 characters.", ErrorCodes.INVALID_USERNAME);
        }
        if (username.indexOf(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER) != -1) {
            throw new InvalidRequestException("Username must not use the character '|'", ErrorCodes.INVALID_USERNAME);
        }
        return username;
    }

    // Two-way Encrypt

//    public static void main(String[] args) throws Exception {
//        String code = makeRecoveryCode("51315780036486e5ec83ff9a", "");
//        System.out.println("Encrypted: " + code);
//        final RecoveryCodeComponents comp = getRecoveryCodeComponents(code);
//        System.out.println("userId: " + "="+comp.getUserId()+"=");
//        System.out.println("username: " + "="+comp.getCanonicalUsername()+"=");
//    }

    // TODO: the master password and salt needs to be secured!
    private static final char[] MASTER_PASSWORD = "23&-*/F43v02!s_83jJ@=a".toCharArray();
    private static final byte[] MASTER_SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };

    /**
     * Class holds the userId and canonicalUsername of a recovery code
     */
    public static class RecoveryCodeComponents {
        private String userId;
        private String canonicalUsername;

        private RecoveryCodeComponents(String userId, String canonicalUsername) {
            this.userId = userId;
            this.canonicalUsername = canonicalUsername;
        }
        public String getCanonicalUsername() {
            return canonicalUsername;
        }
        public String getUserId() {
            return userId;
        }
    }
    /**
     * Creates a temporary recovery code.
     * @param userId The user's id
     * @param canonicalUsername The user's canonical name
     * @return  An encrypted recovery code
     * @throws SystemErrorException  If there is a system problem with encryption.
     */
    public static String makeRecoveryCode(String userId, String canonicalUsername) throws SystemErrorException {
        final StringBuilder b = new StringBuilder(userId);
        b.append(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER);
        b.append(canonicalUsername);
        return encrypt2Way(b.toString());
    }

    public static RecoveryCodeComponents getRecoveryCodeComponents(String recoveryCode) throws SystemErrorException {
        if (recoveryCode == null) {
            throw new SystemErrorException("Invalid recovery code", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        final String[] components = decrypt2Way(recoveryCode).split(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER_REGEXP);
        if (components.length == 0) {
            throw new SystemErrorException("recovery code has empty components", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        return new RecoveryCodeComponents(components[0], (components.length < 2) ? EMPTY_STRING : components[1]);
    }

    /**
     * <p>Encrypts the string using a two-way encryption method.</p>
     * <p><b>IMPORTANT:</b> The master key is subject to change. You should
     * only use this for temporary encryption (e.g., email recovery codes, etc).</p>
     * @param string
     * @return  An encrypted string
     * @throws GeneralSecurityException
     * @throws UnsupportedEncodingException
     * @see #decrypt2Way(String)
     */
    public static String encrypt2Way(String string) throws SystemErrorException {
        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(TWO_WAY_CRYPT_METHOD);
            SecretKey key = fac.generateSecret(new PBEKeySpec(MASTER_PASSWORD));
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(MASTER_SALT, 20));
            return new String(Base64.encodeBase64(cipher.doFinal(string.getBytes("UTF-8"))));
        } catch (Exception e) {
            throw new SystemErrorException("Server error", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    /**
     * Decrypt the encrypted string
     * @param string    Encrypted string
     * @return  The decrypted string
     * @throws SystemErrorException
     * @see #encrypt2Way(String)
     */
    public static String decrypt2Way(String string) throws SystemErrorException {
        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(TWO_WAY_CRYPT_METHOD);
            SecretKey key = fac.generateSecret(new PBEKeySpec(MASTER_PASSWORD));
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(MASTER_SALT, 20));
            return new String(cipher.doFinal(Base64.decodeBase64(string)), "UTF-8");
        } catch (Exception e) {
            throw new SystemErrorException("Server error", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    /**
     * Returns the canonical form of a username. This form
     * is the unique form of the username.
     *
     * @param username The username
     * @return The canonical username
     */
    public static final String makeCanonicalUsername(String username) {
        return username.toLowerCase();
    }
}
