package main.java.com.eweware.service.user.validation;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidUserValidationKey;
import main.java.com.eweware.service.base.error.InvalidUserValidationMethodParameters;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.payload.AuthorizedState;
import main.java.com.eweware.service.mgr.MailManager;
import main.java.com.eweware.service.base.mgr.SystemManager;

import java.util.regex.Pattern;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 2:20 PM
 */
public class EmailUserValidationMethod extends UserValidationMethod {

    // Ensure that email domain/subdomain
    private static final Pattern parametersPattern = Pattern.compile("^.*?\\..*$");  // checks just for a domain

    // Check email address against this pattern
    public static final Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    // WRS-118: allow eweware.com join any group  TODO get rid of this
    private static final String MAGIC_DOMAIN = "eweware.com";

    /**
     * An email address containing a ".-" in the username field is treated as a test email address and will be disregarded
     * TODO come up with a better scheme for managing test account emails?
     */
    private static final Pattern testEmailPattern = Pattern.compile("^.*\\.\\-.*?\\@.*$");

    @Override
    public void checkParameters(String params) throws InvalidUserValidationMethodParameters {
        if (!parametersPattern.matcher(params).matches()) {
            throw new InvalidUserValidationMethodParameters("EmailUserValidationMethod: invalid parameters='" + params + "'");
        }
    }

    @Override
    public void validateKey(String key, String validationParameters) throws InvalidUserValidationKey {
        if (validationParameters == null) {
            throw new InvalidUserValidationKey("missing validation parameters", ErrorCodes.INVALID_INPUT);
        }
        if (key == null) {
            throw new InvalidUserValidationKey("missing key", ErrorCodes.MISSING_EMAIL_ADDRESS);
        }
        if (!emailPattern.matcher(key).matches()) {
            throw new InvalidUserValidationKey("key is not a syntactically valid email address=" + key, ErrorCodes.INVALID_EMAIL_ADDRESS);
        }
        String[] templates = validationParameters.split(" ");
        final int startDest = key.indexOf('@');
        final String dest = key.substring(startDest + 1, key.length());
        for (String t : templates) {
            if (t.equalsIgnoreCase(dest)) {
                return;
            }
        }
        if (MAGIC_DOMAIN.equals(dest)) {  // TODO See WRS-118: remove before beta
            return;
        }
        throw new InvalidUserValidationKey("user email is not in one of the approved validation email destinations for this group", ErrorCodes.INVALID_EMAIL_ADDRESS);
    }

    @Override
    public AuthorizedState getDefaultAuthorizationState() {
        return AuthorizedState.P;
    }

    @Override
    public String startValidation(String userId, String groupId, String groupName, String key) throws InvalidUserValidationKey, SystemErrorException {
        final String validationCode = makeValidationCode(userId, groupId, key);
        sendValidationCodeEmail(key, groupName, validationCode);
        return validationCode;
    }

    private void sendValidationCodeEmail(String emailAddress, String groupName, String validationCode) throws InvalidUserValidationKey {
        if (testEmailPattern.matcher(emailAddress).matches()) {
            System.out.println("MailManager: Not sending email to test account recipient=" + emailAddress);
        } else {
            // TODO definitely queue email call and config reply email
            try {  // won't error if MailManager has not started
                MailManager.getInstance().send(emailAddress, makeRegistrationSubjectForUser(groupName), makeValidationMessageForUser(validationCode, groupName));
            } catch (Exception e) {
                throw new InvalidUserValidationKey("Couldn't send validation email to user at " + emailAddress, e, ErrorCodes.VALIDATION_EMAIL_NOT_SENT_DUE_TO_MAIL_SYSTEM_ERROR);
            }
        }
    }

    private String makeRegistrationSubjectForUser(String groupname) {
        final StringBuilder b = new StringBuilder("Your Validation Code To Join " + groupname);
        return b.toString();
    }

    private String makeValidationMessageForUser(String code, String groupname) {
        final StringBuilder body = new StringBuilder("Hi Blahgua Member!\n\nYour registration with the ");
        body.append(groupname);
        body.append(" group is pending.\n\nTo activate yourself in this group, please follow these simple steps:\n");
        body.append("\n  1. Go to your device or web browser and open the Blahgua");
        body.append("\n  2. Tap (or click) on Setup");
        body.append("\n  3. Tap (or click) on '" + groupname + "'");
        body.append("\n  4. Enter the validation code exactly as shown here, including each character and symbol: " + code);
        body.append("\n  5. Tap (or click) the Validate button");
        body.append("\n\nAfter validating yourself, you will have joined the " + groupname + " group.");
        body.append("\n\nHave fun and leave the sheep alone.");
        body.append("\n\nBaaaaaa,\nBlack Sheep");
        body.append("\n\nPRIVACY NOTICE: above all else, we always safeguard your privacy. Your email address has not been stored and cannot be determined by anyone for the lifetime of your account.");
        body.append("\n\nPLEASE DO NOT REPLY TO THIS EMAIL");
        return body.toString();
    }

    /**
     * Could simply return the userId, but that would be permanent and would
     * expose an internal id. Could be a combination of userId, groupId, and
     * emailAddress (each unique), but that seems cumbersome.
     * Hence, NUMBER simply create a secure random id (Base64) with the
     * expectation that the probability of collision is remote given
     * that the collision probability negligible and, a forteriori,
     * that the code expires in a day or so.
     *
     * @param userId       In case it is needed
     * @param groupId      In case it is needed
     * @param emailAddress In case it is needed
     * @return String   A "unique" validation code
     * @throws main.java.com.eweware.service.base.error.InvalidUserValidationKey
     */
    private String makeValidationCode(String userId, String groupId, String emailAddress) throws SystemErrorException {
        return SystemManager.getInstance().makeShortRandomCode();   // TODO localize here?
    }
}
