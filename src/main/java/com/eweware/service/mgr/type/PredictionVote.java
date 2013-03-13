package main.java.com.eweware.service.mgr.type;

/**
 * <p>The type of vote cast by a user for a prediction.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/10/13 Time: 2:07 PM
 */
public enum PredictionVote {

    /**
     * <p>Pre-expiration: The user agrees with the prediction.</p>
     *  <p>Post-expiration: The user thought that the prediction was correct.</p>
     */
    YES("y"),

    /**
     * <p>Pre-expiration: The user disagrees with the prediction.</p>
     *  <p>Post-expiration: The user thought that the prediction was incorrect.</p>
     */
    NO("n"),

    /**
     * <p>Pre-expiration: The user thought that the prediction was unclear.</p>
     *  <p>Post-expiration: The user thought that the prediction's resolution was unclear.</p>
     */
    UNCLEAR("u");

    /**
     * <p>Used in entities as the name of a field containing one of the enum codes.</p>
     */
    public static final String PREDICTION_VOTE_FIELD_NAME = "v";

    private final String code;

    PredictionVote(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * <p>Returns the type given the code.</p>
     * @param code  The code
     * @return  The type or null if the code is invalid.
     */
    public static final PredictionVote find(String code) {
        if (code == null) {return null;}
        for (PredictionVote t : values()) {
            if (code.equals(t.getCode())) {
                return t;
            }
        }
        return null;
    }
}
