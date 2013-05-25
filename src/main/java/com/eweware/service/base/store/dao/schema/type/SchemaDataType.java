package main.java.com.eweware.service.base.store.dao.schema.type;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/7/12 Time: 10:48 AM
 */
public enum SchemaDataType {

    DB_OBJECT_ID("a Mongo ObjectId",new MongoObjectIdValidator()),
    ILS("indexed list of strings", new IndexedListValidator()),
    ILN("indexed list of numbers", new IndexedListValidator()),
    B("boolean", new BooleanDataTypeValidator()),
    I("integer", new IntegerDataTypeValidator()),
    L("long", new LongDataTypeValidator()),
    R("real", new RealDataTypeValidator()),
    S("string", new StringDataTypeValidator()),
    DT("datetime", new DateTimeDataTypeValidator()),
    D("date", new DateDataTypeValidator()), // yyyy-mm-dd
    GPS("GPS", new GPSDataTypeValidator()),
    E("spec for an embedded schema", new DefaultEmbeddedDataTypeValidator());

    private String description;
    private FieldValidator converter;

    SchemaDataType(String description, FieldValidator converter) {
        this.description = description;
        this.converter = converter;
    }

    public String getDescription() {
        return description;
    }

    public FieldValidator getConverter() {
        return converter;
    }

}
