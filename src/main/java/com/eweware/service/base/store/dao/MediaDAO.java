package main.java.com.eweware.service.base.store.dao;


/**
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:25 PM
 */
public interface MediaDAO extends BaseDAO, MediaDAOConstants {

    public String getType();

    public void setType(String type);

    public String getBucketName();

    public String setBucketName();

    public String getPathname();

    public void setPathname(String pathname);
}
