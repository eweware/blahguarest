package main.java.com.eweware.service.search.index.common;

import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
/**
 * @author rk@post.harvard.edu
 */
public class BlahguaFilterIndexReader extends FilterIndexReader {
	
	public BlahguaFilterIndexReader(IndexReader reader) {
		super(reader);
	}

	public void updateInnerReader(IndexReader inner) {
		in = inner;
	}
	
	public IndexReader getInnerReader() {
		return in;
	}
}