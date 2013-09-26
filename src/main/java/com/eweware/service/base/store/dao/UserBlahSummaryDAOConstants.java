package com.eweware.service.base.store.dao;

/**
 * <p>Fields and corresponding data types for entity holding information about a user's activity with respect to blahs.</p>
 * <p><b>TODO: needs docs</b></p>
 * @author rk@post.harvard.edu
 *         Date: 9/12/12 Time: 1:42 PM
 */
public interface UserBlahSummaryDAOConstants {

    static final String BLAHS_OPENED_BY_SELF = "bos";
    static final String BLAHS_OPENED_BY_OTHERS = "boo";

    static final String BLAHS_CREATED_BY_SELF = "bcs";
    static final String BLAHS_CREATED_BY_OTHERS = "bco";

    static final String BLAHS_VOTED_UP_BY_SELF = "bvus";
    static final String BLAHS_VOTED_UP_BY_OTHERS = "bvuo";

    static final String BLAHS_VOTED_DOWN_BY_SELF = "bvds";
    static final String BLAHS_VOTED_DOWN_BY_OTHERS = "bvdo";

    static final String COMMENTS_CREATED_BY_SELF = "ccs";
    static final String COMMENTS_CREATED_BY_OTHERS = "cco";

    static final String COMMENTS_VOTED_UP_BY_SELF = "cvus";
    static final String COMMENTS_VOTED_UP_BY_OTHERS = "cvuo";

    static final String COMMENTS_VOTED_DOWN_BY_SELF = "cvds";
    static final String COMMENTS_VOTED_DOWN_BY_OTHERS = "cvdo";

    // Calculated by stats:
    static final String PEOPLE_VIEWED = "V";
    static final String PEOPLE_OPENED = "O";
    static final String PEOPLE_COMMENTED = "C";

}
