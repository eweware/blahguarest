package main.java.com.eweware.service.search.index.blah;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.BlahPayload;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.BlahDAO;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import proj.zoie.api.indexing.ZoieIndexable;

import java.util.Date;
import java.util.UUID;


/**
 * @author rk@post.harvard.edu
 */
public class BlahDataIndexable implements ZoieIndexable {

    private static BlahSchema cachedSchema;

    private BlahDAO blah;

    public BlahDataIndexable(BlahDAO blah) {
        this.blah = blah;
    }

    private BlahSchema getSchema() {
        if (cachedSchema != null) {
            return cachedSchema;
        }
        cachedSchema = BlahSchema.getSchema(LocaleId.en_us);
        return cachedSchema;
    }

    public IndexingReq[] buildIndexingReqs() {

        // We always create just one indexing request with one single document.
        // For legacy reasons, we have this API to return an array.
        // This array should contain one and only one indexing request.

        // Fetch object rather than the accessors when the data type is not a string
        // because sometimes it won't be.

//        Document doc = new Document();
//        for (Map.Entry<String, SchemaSpec> entry : getSchema().getFieldNameToSpecMap().entrySet()) {
//            final String fieldName = entry.getKey();
//            final SchemaSpec spec = entry.getValue();
//            final Object value = blah.get(fieldName);
//            if (spec.isNumeric()) { // TODO  spec could indicate desired Index and Store values
//                doc.add(new Field(fieldName, value.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
//            } else if (spec.isCalendar()) {
//                doc.add(new Field(fieldName,
//                        (value instanceof Date) ? DateUtils.formatDateTime((Date) blah.get(fieldName)) : value.toString(),
//                        Store.YES, Index.NOT_ANALYZED_NO_NORMS));
//            } else if (spec.isString()) {
//                doc.add(new Field(fieldName, value.toString(), Store.YES,
//                        (fieldName.equals(BlahDAO.TEXT) || fieldName.equals(BlahDAO.BODY) ? Index.ANALYZED : Index.NOT_ANALYZED_NO_NORMS)));
//            }
//        }


        Document doc = new Document();
        doc.add(new Field(BaseDAO.ID, blah.getId(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        if (blah.getAuthorId() != null) {
            doc.add(new Field(BlahDAO.AUTHOR_ID, blah.getAuthorId(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        if (blah.getText() != null) {
            doc.add(new Field(BlahDAO.TEXT, blah.getText(), Store.YES, Index.ANALYZED));
        }
        if (blah.getBody() != null) {
            doc.add(new Field(BlahDAO.BODY, blah.getBody(), Store.YES, Index.ANALYZED));
        }
        Object tmp = blah.get(BlahDAO.UP_VOTES);
        if (tmp != null) {
            doc.add(new Field(BlahDAO.UP_VOTES, tmp.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        tmp = blah.get(BlahDAO.DOWN_VOTES);
        if (tmp != null) {
            doc.add(new Field(BlahDAO.DOWN_VOTES, tmp.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        tmp = blah.get(BlahDAO.VIEWS);
        if (tmp != null) {
            doc.add(new Field(BlahDAO.VIEWS, tmp.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        tmp = blah.get(BlahDAO.OPENS);
        if (tmp != null) {
            doc.add(new Field(BlahDAO.OPENS, tmp.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        String type = blah.getTypeId();
        if (type != null) {
            doc.add(new Field(BlahDAO.TYPE_ID, blah.getTypeId(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        tmp = blah.get(BlahDAO.CREATED);
        if (tmp != null) {
            doc.add(new Field(BlahDAO.CREATED,
                    (tmp instanceof Date) ? DateUtils.formatDateTime(blah.getCreated()) : tmp.toString(),
                    Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }
        tmp = blah.get(BlahDAO.UPDATED);
        if (tmp != null) {
            doc.add(new Field(BlahDAO.UPDATED,
                    (tmp instanceof Date) ? DateUtils.formatDateTime(blah.getUpdated()) : tmp.toString(),
                    Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        }

        // no need to add the id field, Zoie will manage the id for you
        return new IndexingReq[]{new IndexingReq(doc)};
    }


    /**
     * Creates a payload object from a Blah Lucene document.
     * TODO The fields must be kept in sync with buildIndexingReqs until a better method is implemented.
     *
     * @param doc The Lucene Document containing blah fields
     * @return The blah payload
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public static BlahPayload fromBlahDocument(Document doc) throws SystemErrorException {
        BlahPayload blah = new BlahPayload();
        Fieldable field = doc.getFieldable(BaseDAO.ID);
        if (field != null) {
            blah.setId(field.stringValue());
        }
        field = doc.getFieldable(BlahDAO.AUTHOR_ID);
        if (field != null) {
            blah.setAuthorId(field.stringValue());
        }
        field = doc.getFieldable(BlahDAO.TEXT);
        if (field != null) {
            blah.setText(field.stringValue());
        }
        field = doc.getFieldable(BlahDAO.BODY);
        if (field != null) {
            blah.setBody(field.stringValue());
        }
        field = doc.getFieldable(BlahDAO.VOTES);
        if (field != null) {
            blah.setVotes(new Integer(field.stringValue()));
        }
        field = doc.getFieldable(BlahDAO.UP_VOTES);
        if (field != null) {
            blah.setUpVotes(new Integer(field.stringValue()));
        }
        field = doc.getFieldable(BlahDAO.DOWN_VOTES);
        if (field != null) {
            blah.setDownVotes(new Integer(field.stringValue()));
        }
        field = doc.getFieldable(BlahDAO.VIEWS);
        if (field != null) {
            blah.setViews(new Integer(field.stringValue()));
        }
        field = doc.getFieldable(BlahDAO.OPENS);
        if (field != null) {
            blah.setOpens(new Integer(field.stringValue()));
        }
        field = doc.getFieldable(BlahDAO.TYPE_ID);
        if (field != null) {
            blah.setTypeId(field.stringValue());
        }
        field = doc.getFieldable(BlahDAO.CREATED);
        if (field != null) {
            blah.setCreated(field.stringValue());
        }
        field = doc.getFieldable(BlahDAO.UPDATED);
        if (field != null) {
            blah.setUpdated(field.stringValue());
        }
        return blah;
    }

    public long getUID() {
        return UUID.nameUUIDFromBytes(blah.getId().getBytes()).getLeastSignificantBits();
    }

    public boolean isDeleted() {
        final Boolean deleted = blah.getDeleted();
        return deleted;
    }

    public boolean isSkip() {
        return false;
    }

    @Override
    public byte[] getStoreValue() {
        return null;
    }

    @Override
    public boolean isStorable() {
        return false;
    }
}