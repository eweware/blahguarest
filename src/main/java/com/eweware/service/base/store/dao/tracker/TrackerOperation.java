package main.java.com.eweware.service.base.store.dao.tracker;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/22/12 Time: 2:44 PM
 */
public enum TrackerOperation {

    CREATE_BLAH("c"),
    UPDATE_BLAH("u"),
    CREATE_COMMENT("C"),
    UPDATE_COMMENT("U"),
    CREATE_USER("r"),
    USER_TO_GROUP_STATE_CHANGE("k"),
    CREATE_GROUP("g"),
    CREATE_GROUP_TYPE("G");

    private String code;

    private TrackerOperation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
