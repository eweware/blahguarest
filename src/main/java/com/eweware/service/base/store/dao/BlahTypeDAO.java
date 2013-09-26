package com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/23/12 Time: 2:44 PM
 */
public interface BlahTypeDAO extends BaseDAO, BlahTypeDAOConstants {

    /**
     * Returns the displayable name of this blah type
     *
     * @return displayable name of this blah type
     */
    public String getName();

    /**
     * Sets the displayable name of this blah's type
     *
     * @param name The displayable name
     */
    public void setName(String name);

    /**
     * Returns this blah type category id, if any.
     *
     * @return The blah type category id
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType
     */
    public Integer getCategoryId();

    /**
     * Sets this blah type's category id
     *
     * @param categorid The blah type category id
     * @see com.eweware.service.base.store.dao.type.BlahTypeCategoryType
     */
    public void setCategoryId(Integer categorid);
}
