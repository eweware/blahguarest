package com.eweware.service.base.store.dao.type;

/**
 * <p>Specifies the type of validation method.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/2/13 Time: 1:57 PM
 */
public enum RecoveryMethodType {

    /**
     * Recovery is performed through email.
     */
    EMAIL("e"),

    /**
     * Recovery is performed in some default manner.
     */
    DEFAULT("d");

    private final String code;

    RecoveryMethodType(String code) {
        this.code = code;
    }

    /**
     * <p>This code is what's stored in the DB, etc.</p>
     * @return The recovery method code
     */
    public String getCode() {
        return code;
    }
}
