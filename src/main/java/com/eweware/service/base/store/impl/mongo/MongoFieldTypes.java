package main.java.com.eweware.service.base.store.impl.mongo;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:27 PM
 *         <p/>
 *         The incrementalOperation is used to incrementally update a number.
 */
public enum MongoFieldTypes {

    NUMBER("$inc", "$set", "$unset"),
    STRING("$set", "$set", "$unset"),
    DATE("$set", "$set", "$unset"),
    ARRAY("$push", "$set", "$unset"), // TODO way to remove a single element
    SET("$addToSet", "$set", "$unset"), // TODO way to remove a single element
    BOOLEAN("$set", "$set", "$unset");

    private final String incrementalOperation;
    private final String overwriteOperation;
    private final String deleteOperation;

    private MongoFieldTypes(String incrementalOperation, String overwriteOperation, String deleteOperation) {
        this.incrementalOperation = incrementalOperation;
        this.overwriteOperation = overwriteOperation;
        this.deleteOperation = deleteOperation;
    }

    public String getIncrementalOperation() {
        return incrementalOperation;
    }

    public String getOverwriteOperation() {
        return overwriteOperation;
    }

    public String getDeleteOperation() {
        return deleteOperation;
    }
}
