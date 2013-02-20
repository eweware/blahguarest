package main.java.com.eweware.service.user.validation;


import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
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
     * @param password    String The password to encrypt
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

    public static void checkPassword(String password) throws InvalidRequestException {
        if (CommonUtilities.checkString(password, 6, 32)) {
            throw new InvalidRequestException("Invalid password", ErrorCodes.INVALID_PASSWORD);
        }
    }

    public static void checkUsername(String username) throws InvalidRequestException {
        if (CommonUtilities.checkString(username, 3, 32)) {
            throw new InvalidRequestException("Invalid username", ErrorCodes.INVALID_USERNAME);
        }
    }

}
