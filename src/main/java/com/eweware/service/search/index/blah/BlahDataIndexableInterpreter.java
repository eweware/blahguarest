package com.eweware.service.search.index.blah;


import com.eweware.service.base.store.dao.BlahDAO;
import proj.zoie.api.indexing.ZoieIndexable;
import proj.zoie.api.indexing.ZoieIndexableInterpreter;
/**
 * @author rk@post.harvard.edu
 */
public class BlahDataIndexableInterpreter implements ZoieIndexableInterpreter<BlahDAO> {

	@Override
	public ZoieIndexable convertAndInterpret(BlahDAO src) {
		return new BlahDataIndexable(src);
	}
}