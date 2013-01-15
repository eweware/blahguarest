package main.java.com.eweware.service.user.validation;

import main.java.com.eweware.service.base.error.InvalidUserValidationKey;
import main.java.com.eweware.service.base.error.InvalidUserValidationMethodParameters;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.payload.AuthorizedState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/9/12 Time: 1:11 PM
 */
public abstract class UserValidationMethod {

    public static final String VALIDATION_METHOD_DEFAULT_CODE = "n";
    public static final String VALIDATION_METHOD_EMAIL_VALIDATION_CODE = "e";

    public static final Map<String, UserValidationMethod> validationMethodValues = new HashMap<String, UserValidationMethod>(2);

    static {
        UserValidationMethod.validationMethodValues.put(UserValidationMethod.VALIDATION_METHOD_DEFAULT_CODE, new DefaultUserValidationMethod());
        UserValidationMethod.validationMethodValues.put(UserValidationMethod.VALIDATION_METHOD_EMAIL_VALIDATION_CODE, new EmailUserValidationMethod());
    }

    public static UserValidationMethod getValidationMethod(String method) {
        return validationMethodValues.get(method);
    }


    /**
     * Checks whether the validation parameters are appropriate for this kind of validation method
     *
     * @param validationParameters The validation parameters formatted for this particular method
     * @throws InvalidUserValidationMethodParameters
     *          Thrown if the parameters are syntactically invalid
     */
    public abstract void checkParameters(String validationParameters) throws InvalidUserValidationMethodParameters;

    /**
     * Validates the specified key against the given validation parameters
     *
     * @param key                  A key in the format expected by the validator
     * @param validationParameters The validation parameters formatted for this particular method
     */
    public abstract void validateKey(String key, String validationParameters) throws InvalidUserValidationKey;

    public abstract AuthorizedState getDefaultAuthorizationState();

    /**
     * Starts the validation phase. For example, if email validation is required,
     * an email will be sent, etc...
     *
     * @param userId    The id of the user
     * @param groupId   the id of the group
     * @param groupName The display name of the group that the user wants to startValidation against
     * @param key       The key used to startValidation with (e.g., an email address)
     * @return String   Returns a validation code, if appropriate, or null. Validation codes must associated
     * with the user and the group for future reference. This method does not perform that association
     * because user/group relations are state-dependent.
     */
    public abstract String startValidation(String userId, String groupId, String groupName, String key) throws InvalidUserValidationKey, SystemErrorException;
}
