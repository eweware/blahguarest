package com.eweware.service.base.store.dao;

/**
 * <p>A poll option is an entity containing information about one of the
 * possible poll options.</p>
 * <p>Field names and value data types for poll option entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 2/15/13 Time: 5:54 PM
 */
public interface PollOptionDAOConstants {

    /**
     * <p>Maximum number of poll options allowed per blah.</p>
     */
    public static final int MAX_POLL_OPTIONS = 10;

    /**
     * <p> The tagline for this option. A string.</p>
     */
    static final String TAGLINE = "G";

    /**
     * <p> The extended text for this option, if any. A string.</p>
     */
    static final String TEXT = "T";
}
