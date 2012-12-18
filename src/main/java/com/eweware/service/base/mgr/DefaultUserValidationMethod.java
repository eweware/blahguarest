package main.java.com.eweware.service.base.mgr;

import main.java.com.eweware.service.base.error.InvalidUserValidationKey;
import main.java.com.eweware.service.base.error.InvalidUserValidationMethodParameters;
import main.java.com.eweware.service.base.payload.AuthorizedState;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 2:21 PM
 */
public class DefaultUserValidationMethod extends UserValidationMethod {
    @Override
    public void checkParameters(String validationParameters) throws InvalidUserValidationMethodParameters {
        // ignore
    }

    @Override
    public void validateKey(String key, String validationParameters) throws InvalidUserValidationKey {
        // ignore
    }

    @Override
    public AuthorizedState getDefaultAuthorizationState() {
        return AuthorizedState.A;
    }

    @Override
    public String startValidation(String userId, String groupId, String groupName, String key) {
        // do nothing
        return null;
    }
}
