package main.java.com.eweware.service.user;


import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Arrays;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 1:04 AM
 *
 *         version 1: uses OWASP recommendations
 *         TODO version 2: create own version of SHA-X algorithm
 */
public class Login {

    private final static int ITERATION_NUMBER = 1000;

    /**
     * Authenticates the user with a given login and password
     * If password and/or login is null then always returns false.
     * If the user does not exist in the database returns false.
     *
     * @param login    String The login of the user
     * @param password String The password of the user
     * @return boolean Returns true if the user is authenticated, false otherwise
     * @throws NoSuchAlgorithmException If the algorithm SHA-1 is not supported by the JVM
     */
    public boolean authenticate(String login, String password)
            throws Exception {
        try {

            boolean userExists = true;

            // INPUT VALIDATION
            if (login == null || password == null) {
                // TIME RESISTANT ATTACK
                // Computation time is equal to the time needed by a legitimate user
                userExists = false;
                login = "";
                password = "";
            }


            // get digest and salt from db for login key
            String digest = "base64digestfromdb", salt = "base64salt";
            userExists = true;

            if (!userExists) {
                // TIME RESISTANT ATTACK (Even if the user does not exist the
                // Computation time is equal to the time needed for a legitimate user
                digest = "000000000000000000000000000=";
                salt = "00000000000=";
            }


            byte[] bDigest = base64ToByte(digest);
            byte[] bSalt = base64ToByte(salt);

            // Compute the new DIGEST
            byte[] proposedDigest = getHash(ITERATION_NUMBER, password, bSalt);

            return Arrays.equals(proposedDigest, bDigest) && userExists;
        } catch (IOException ex) {
            throw new Exception("password inaccessible format", ex);
        }
    }


    /**
     * Inserts a new user in the database
     *
     * @param con      Connection An open connection to a databse
     * @param login    String The login of the user
     * @param password String The password of the user
     * @return boolean Returns true if the login and password are ok (not null and length(login)<=100
     * @throws NoSuchAlgorithmException If the algorithm SHA-1 or the SecureRandom is not supported by the JVM
     */
    public boolean createUser(Connection con, String login, String password)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (login != null && password != null && login.length() <= 100) {
            // Uses a secure Random not a simple Random
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // Salt generation 64 bits long
            byte[] bSalt = new byte[8];
            random.nextBytes(bSalt);
            // Digest computation
            byte[] bDigest = getHash(ITERATION_NUMBER, password, bSalt);
            String sDigest = byteToBase64(bDigest);
            String sSalt = byteToBase64(bSalt);

            // TODO insert digest and salt into db
            return true;
        } else {
            return false;
        }
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
    public byte[] getHash(int iterationNb, String password, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
     * From a base 64 representation, returns the corresponding byte[]
     *
     * @param data String The base64 representation
     * @return byte[]
     * @throws IOException
     */
    public static byte[] base64ToByte(String data) throws IOException {
        return Base64.decodeBase64(data);
    }

    /**
     * From a byte[] returns a base 64 representation
     *
     * @param data byte[]
     * @return String
     * @throws IOException
     */
    public static String byteToBase64(byte[] data) {
        return new String(Base64.encodeBase64(data));
    }

}
