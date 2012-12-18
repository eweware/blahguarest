package main.java.com.eweware.service.search.index.common;

import org.apache.lucene.search.DocIdSet;
import proj.zoie.api.ZoieIndexReader;
import proj.zoie.api.indexing.IndexReaderDecorator;

import java.io.IOException;

/**
 * @author rk@post.harvard.edu
 * An IndexDecorator is a way for clients to decorate a given ZoieIndexReader to a custom IndexReader type, e.g. FilterIndexReader class in Lucene.
 * This is not mandatory, client for most cases can just use the returned ZoieIndexReader.
 */
public class BlahguaIndexReaderDecorator implements IndexReaderDecorator<BlahguaFilterIndexReader> {

	@Override
	public BlahguaFilterIndexReader decorate(ZoieIndexReader<BlahguaFilterIndexReader> indexReader) throws IOException {
		return new BlahguaFilterIndexReader(indexReader);
	}

	@Override
	public BlahguaFilterIndexReader redecorate(BlahguaFilterIndexReader decorated, ZoieIndexReader<BlahguaFilterIndexReader> copy, boolean withDeletes) throws IOException {
		// underlying segment has not changed, just change the inner reader
		decorated.updateInnerReader(copy);
		return decorated;
	}

	@Override
	public void setDeleteSet(BlahguaFilterIndexReader reader, DocIdSet docIds) {
		// TODO Auto-generated method stub
	}
}