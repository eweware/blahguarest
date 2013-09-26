package com.eweware.service.base.store.dao;


/**
 * <p>Represents a media object</p>
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:25 PM
 */
public interface MediaDAO extends BaseDAO, MediaDAOConstants {

    /**
     * <p>Returns the media referend type</p>
     *
     * @return String   Returns the media referend type
     * @see com.eweware.service.base.store.dao.type.MediaReferendType
     */
    public String getReferendType();

    /**
     * <p> Sets the media referend's type</p>
     *
     * @param referendType Media referend type (a valid file extension)
     * @see com.eweware.service.base.store.dao.type.MediaReferendType
     */
    public void setReferendType(String referendType);

    /**
     * <p>Returns the media's type (is it an image, video?)</p>
     *
     * @return The media type
     * @see com.eweware.service.base.store.dao.type.MediaType
     */
    public String getType();

    /**
     * <p>Sets the media type</p>
     *
     * @param type The media's type (a MediaType)
     * @see com.eweware.service.base.store.dao.type.MediaType
     */
    public void setType(String type);
}
