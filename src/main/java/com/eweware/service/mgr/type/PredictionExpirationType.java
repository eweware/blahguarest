package com.eweware.service.mgr.type;

/**
 * <p>Specifies whether a vote for a prediction is pre-expiration or post-expiration.</p>
 * <p>A pre-expiration vote is an agree/disagree/unclear vote.</p>
 * <p>A post-expiration vote is an assertion by a user that the
 * prediction's resolution is correct/incorrect/unclear.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/10/13 Time: 1:59 PM
 */
public enum PredictionExpirationType {

    /**
     * <p>Specifies that the prediction's vote is before the expiration date.</p>
     */
    PRE_EXPIRATION("pre"),

    /**
     * <p>Specifies that the prediction's vote is after the expiration date.</p>
     */
    POST_EXPIRATION("post");

    /**
     * <p>Used in entities as the name of a field containing one of the enum codes.</p>
     */
    public static final String PREDICTION_VOTE_TYPE_FIELD_NAME = "t";

    private final String code;

    PredictionExpirationType(String code) {
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
    public static final PredictionExpirationType find(String code) {
        if (code == null) {return null;}
        for (PredictionExpirationType t : values()) {
            if (code.equals(t.getCode())) {
                return t;
            }
        }
        return null;
    }
}
