package main.java.com.eweware.service.search.index.user;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.UserDAO;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import proj.zoie.api.indexing.ZoieIndexable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * @author rk@post.harvard.edu
 * A ZoieIndexableInterpreter is a way to convert a data object into a Lucene document
 */
public class UserDataIndexable implements ZoieIndexable {

	private UserDAO user;

	public UserDataIndexable(UserDAO user) {
		this.user = user;
	}

    /**
     * Creates a user dao object from a Lucene user document.
     * TODO This needs to be in sync with the entries indexed by buildIndexingReqs() until a better method is implemented.
     *
     * @param doc the user Lucene document
     * @return UserDAOImpl The user dao
     */
    public static UserDAO makeUserDAOFromDocument(Document doc) throws SystemErrorException {
        Map<String, Object> map = new HashMap<String, Object>();
        final UserDAO dao = MongoStoreManager.getInstance().createUser(map);
        Fieldable field = doc.getFieldable(BaseDAO.ID);
        if (field != null) {
            map.put(UserDAO.ID, field.stringValue());
        } else {
            throw new SystemErrorException("missing id in userDAO");
        }
        field = doc.getFieldable(main.java.com.eweware.service.base.store.dao.UserDAO.DISPLAY_NAME);
        if (field != null) {
            map.put(UserDAO.DISPLAY_NAME, field.stringValue());
        }
        return dao;
    }

    public long getUID() {
        return UUID.nameUUIDFromBytes(user.getId().getBytes()).getLeastSignificantBits();
	}

	public IndexingReq[] buildIndexingReqs() {
		// We always create just one indexing request with one single document.
		// For legacy reasons, we have this API to return an array.
		// This array should contain one and only one indexing request.
		Document doc = new Document();
		doc.add(new Field(BaseDAO.ID, user.getId(), Store.YES, Index.ANALYZED));
		doc.add(new Field(UserDAO.DISPLAY_NAME, user.getDisplayName(), Store.YES, Index.ANALYZED));

		// no need to add the id field, Zoie will manage the id for you
		return new IndexingReq[] { new IndexingReq(doc) };
	}

	// the following methods in this example are kind of hacky,
	// but it is designed to be used when information needed to determine
	// whether documents
	// are to be deleted and/or skipped are only known at runtime

	public boolean isDeleted() {
		return user.getDeleted();
	}

	public boolean isSkip() {
		return false;
	}

	@Override
	public byte[] getStoreValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStorable() {
		// TODO Auto-generated method stub
		return false;
	}
}