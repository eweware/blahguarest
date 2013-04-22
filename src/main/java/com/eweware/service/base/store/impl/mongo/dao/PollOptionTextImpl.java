package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.PollOptionTextDAO;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Map;

/**
 * <p>Embedded in a blah.</p>
 * @author rk@post.harvard.edu
 *         Date: 2/15/13 Time: 5:58 PM
 */
public class PollOptionTextImpl extends BaseDAOImpl implements PollOptionTextDAO {

    public PollOptionTextImpl() {
    }

    public PollOptionTextImpl(String tagLine, String text) {
        setTagLine(tagLine);
        setText(text);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return null; // not required as this is an inner object
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        return null;  // not required as this is an inner object
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        return null;  // not required as this is an inner object
    }

    @Override
    public String getTagLine() {
        return (String) get(TAGLINE);
    }

    @Override
    public void setTagLine(String tagLine) {
        put(TAGLINE, tagLine);
    }

    @Override
    public String getText() {
        return (String) get(TEXT);
    }

    @Override
    public void setText(String text) {
        put(TEXT, text);
    }

//    @Override
//    public Map<String, Object> toMap() {
//        return super.toMap();
//    }
}
