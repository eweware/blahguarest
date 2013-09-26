package com.eweware.service.search.index.blah;

import com.eweware.service.base.store.dao.CommentDAO;
import proj.zoie.api.indexing.ZoieIndexable;
import proj.zoie.api.indexing.ZoieIndexableInterpreter;


public class BlahCommentDataIndexableInterpreter implements ZoieIndexableInterpreter<CommentDAO> {

	@Override
	public ZoieIndexable convertAndInterpret(CommentDAO src) {
		return new BlahCommentDataIndexable(src);
	}
}