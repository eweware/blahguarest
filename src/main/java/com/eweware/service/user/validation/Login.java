package main.java.com.eweware.service.user.validation;


import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
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

//    private static final String EMPTY_STRING = "";

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
     *
     * @param password The password. If null, it is interpreted as ""
     * @return The validated version of the password.
     * @throws InvalidRequestException Thrown if the password is unacceptable
     */
    public static String ensurePasswordString(String password) throws InvalidRequestException {
        if (password == null) {
            password = "";
        } // empty string
        if (!CommonUtilities.checkString(password, 0, 64)) {
            throw new InvalidRequestException("Invalid password. Length must not exceed 64.", ErrorCodes.INVALID_PASSWORD);
        }
        return password;
    }

    /**
     * Checks username string and returns appropriate version of it
     *
     * @param username The username. If null, it is interpreted as ""
     * @return The validated version of the username.
     * @throws InvalidRequestException Thrown if the username is unacceptable
     */
    public static String ensureUsernameString(String username) throws InvalidRequestException {
        if (!CommonUtilities.checkString(username, 3, 32)) {
            throw new InvalidRequestException("Invalid username '" + username + "'. Length must be less than 33 and more than 2 characters.", ErrorCodes.INVALID_USERNAME);
        }
        if (username.indexOf(RecoveryCode.ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER) != -1) {
            throw new InvalidRequestException("Username must not use the character '|'", ErrorCodes.INVALID_USERNAME);
        }
        return username;
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

    // Two-way Encrypt

    // TODO: the master password and salt needs to be secured!
    private static final char[] MASTER_PASSWORD = "23&-*/F43v02!s_83jJ@=a".toCharArray();
    private static final byte[] MASTER_SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };



    public static void main(String[] args) throws Exception {
        RecoveryCode recoveryCode = RecoveryCode.createRecoveryCode("51315780036486e5ec83ff9a", "ruben");
        System.out.println("RECOVERY CODE:" + recoveryCode);
        System.out.println();
        System.out.println("Encrypted: " + recoveryCode.toString());
        final RecoveryCodeComponents comp = RecoveryCode.getRecoveryComponents(recoveryCode.toString());
        System.out.println("userId: " + "=" + comp.getUserId() + "=");
        System.out.println("username: " + "=" + comp.getCanonicalUsername() + "=");
    }

    public static String getStringFromSecretKey(SecretKey secretKey) {
        return Base64.encodeBase64String(secretKey.getEncoded());
    }

    public static SecretKey getSecretKeyFromString(String secretKey) {
        byte[] encodedKey     = Base64.decodeBase64(secretKey);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    public static RecoveryCode encrypt2Way(String string) throws SystemErrorException {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(MASTER_PASSWORD, MASTER_SALT, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();
            final RecoveryCode recoveryCode = new RecoveryCode(
                    Base64.encodeBase64String(cipher.doFinal(string.getBytes("UTF-8"))),
                    Base64.encodeBase64String(params.getParameterSpec(IvParameterSpec.class).getIV()),
                    getStringFromSecretKey(secret)
            );
            return recoveryCode;
        } catch (Exception e) {
            throw new SystemErrorException("Server error", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    public static String decrypt2Way(String cipherBase64, String ivBase64, String secret) throws SystemErrorException {
        try {
            SecretKey secretKey = getSecretKeyFromString(secret);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final byte[] bytes = Base64.decodeBase64(ivBase64);
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return new String(cipher.doFinal(Base64.decodeBase64(cipherBase64)), "UTF-8");
        } catch (Exception e) {
            throw new SystemErrorException("Server error", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }
}



//    public static class RecoveryCode extends BasicDBObject implements Serializable {
//
//        public RecoveryCode(String cipherBase64, String ivBase64, String secretKeyBase64) {
//            put("c", cipherBase64);
//            put("i", ivBase64);
//            put("s", secretKeyBase64);
//            put("test", MASTER_SALT);
//        }
//
//        public static final RecoveryCodeComponents fromString(String string) throws SystemErrorException {
//            final String[] foo = string.split(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER_REGEXP);
//            final String cipher = foo[0];
//            final String iv = foo[1];
//            final String secretKey = foo[2];
//            final String text = decrypt2Way(cipher, iv, secretKey);
//            final String[] components = text.split(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER_REGEXP);
//            if (components.length < 1) {
//                throw new SystemErrorException("recovery components", ErrorCodes.SERVER_RECOVERABLE_ERROR);
//            }
//            return new RecoveryCodeComponents(components[0], (components.length == 1) ? "" : components[1]);
//        }
//
//        public String toString() {
//            final StringBuilder b = new StringBuilder();
//            b.append(getCipherBase64());
//            b.append(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER);
//            b.append(getIvBase64());
//            b.append(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER);
//            b.append(getSecretKeyBase64());
//            return b.toString();
//        }
//
//        /**
//         * Creates a temporary recovery code.
//         *
//         * @param userId            The user's id
//         * @param canonicalUsername The user's canonical name
//         * @return An encrypted recovery code
//         * @throws SystemErrorException If there is a system problem with encryption.
//         */
//        public static RecoveryCode createRecoveryCode(String userId, String canonicalUsername) throws SystemErrorException {
//            final StringBuilder b = new StringBuilder(userId);
//            b.append(ENCRYPTED_RECOVERY_CODE_SEGMENT_DELIMITER);
//            b.append(canonicalUsername);
//            return encrypt2Way(b.toString());
//        }
//
//        public String getCipherBase64() {
//            return (String) get("c");
//        }
//
//        public String getIvBase64() {
//            return (String) get("i");
//        }
//
//        public String getSecretKeyBase64() {
//            return (String) get("s");
//        }
//    }
