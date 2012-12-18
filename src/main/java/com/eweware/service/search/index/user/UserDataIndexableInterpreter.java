package main.java.com.eweware.service.search.index.user;


import main.java.com.eweware.service.base.store.dao.UserDAO;
import proj.zoie.api.indexing.ZoieIndexable;
import proj.zoie.api.indexing.ZoieIndexableInterpreter;

/**
 * @author rk@post.harvard.edu
 */
public class UserDataIndexableInterpreter implements ZoieIndexableInterpreter<UserDAO> {

	@Override
	public ZoieIndexable convertAndInterpret(UserDAO src) {
		return new UserDataIndexable(src);
	}
}