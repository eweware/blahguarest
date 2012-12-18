package main.java.com.eweware.service.search.index.blah;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.BasePayload;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.CommentDAO;
import main.java.com.eweware.service.base.store.dao.schema.CommentSchema;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import proj.zoie.api.indexing.ZoieIndexable;

import java.util.Date;
import java.util.UUID;

/**
 * @author rk@post.harvard.edu
 */

public class BlahCommentDataIndexable implements ZoieIndexable {

    private static CommentSchema cachedSchema;

    private CommentDAO comment;

    public BlahCommentDataIndexable(CommentDAO comment) {
        this.comment = comment;
    }

    public long getUID() {
        return UUID.nameUUIDFromBytes(comment.getId().getBytes()).getLeastSignificantBits();
    }

    private CommentSchema getSchema() {
        if (cachedSchema != null) {
            return cachedSchema;
        }
        cachedSchema = CommentSchema.getSchema(LocaleId.en_us);
        return cachedSchema;
    }

    public IndexingReq[] buildIndexingReqs() {

        // We always create just one indexing request with one single document.
        // For legacy reasons, we have this API to return an array.
        // This array should contain one and only one indexing request.

        // Fetch object rather than the accessors when the data type is not a string
        // because sometimes it won't be.

        Document doc = new Document();
//        for (Map.Entry<String, SchemaSpec> entry : getSchema().getFieldNameToSpecMap().entrySet()) {
//            final String fieldName = entry.getKey();
//            final SchemaSpec spec = entry.getValue();
//            final Object value = comment.get(fieldName);
//            if (spec.isNumeric()) { // TODO  spec could indicate desired Index and Store values
//                doc.add(new Field(fieldName, value.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
//            } else if (spec.isCalendar()) {
//                doc.add(new Field(fieldName,
//                        (value instanceof Date) ? DateUtils.formatDateTime((Date) comment.get(fieldName)) : value.toString(),
//                        Store.YES, Index.NOT_ANALYZED_NO_NORMS));
//            } else if (spec.isString()) {
//                doc.add(new Field(fieldName, value.toString(), Store.YES,
//                        (fieldName.equals(CommentDAO.TEXT) ? Index.ANALYZED : Index.NOT_ANALYZED_NO_NORMS)));
//            }
//        }

        doc.add(new Field(BaseDAO.ID, comment.getId(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        if (comment.getAuthorId() != null) {
            doc.add(new Field(CommentDAO.AUTHOR_ID, comment.getAuthorId(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        if (comment.getBlahId() != null) {
            doc.add(new Field(CommentDAO.BLAH_ID, comment.getBlahId(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        if (comment.getText() != null) {
            doc.add(new Field(CommentDAO.TEXT, comment.getText(), Store.YES, Index.ANALYZED));
        }
        for (String fieldName : new String[]{CommentDAO.BLAH_VOTE, CommentDAO.COMMENT_VOTES, CommentDAO.COMMENT_UP_VOTES, CommentDAO.COMMENT_DOWN_VOTES, CommentDAO.VIEWS, CommentDAO.OPENS}) {
            final Object tmp = comment.get(fieldName);
            if (tmp != null) {
                doc.add(new Field(fieldName, tmp.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
            }
        }
        for (String fieldName : new String[]{CommentDAO.CREATED, CommentDAO.UPDATED}) {
            final Object tmp = comment.get(fieldName);
            doc.add(new Field(fieldName,
                    (tmp instanceof Date) ? DateUtils.formatDateTime((Date) comment.get(fieldName)) : tmp.toString(),
                    Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        // no need to add the id field, Zoie will manage the id for you
        return new IndexingReq[]{new IndexingReq(doc)};
    }


    public static final BasePayload fromCommentDocument(Document doc) {
        return null;
    }

    public boolean isDeleted() {
        return comment.getDeleted();
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
