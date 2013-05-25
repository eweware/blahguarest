package main.java.com.eweware.service.base.cache;

import main.java.com.eweware.service.base.payload.BasePayload;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;

import java.util.Comparator;
import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/8/12 Time: 12:09 PM
 */
public class FieldMapComparator implements Comparator<BasePayload> {

    private final String fieldName;
    private SchemaDataType dataType;
    private Integer sortDirection;

    public FieldMapComparator(String fieldName, Integer sortDirection, SchemaSpec spec) {
        this.fieldName = fieldName;
        this.sortDirection = sortDirection;
        this.dataType = spec.getDataType();
    }

    @Override
    public int compare(BasePayload a, BasePayload b) {
        final Object obj1 = a.get(fieldName);
        final Object obj2 = b.get(fieldName);
        if (obj1 == null || obj2 == null) { // nulls last
            return (obj1 == null && obj2 == null) ? 0 : (obj1 == null) ? -1 : 1;
        } else if (dataType == SchemaDataType.S || dataType == SchemaDataType.ILS || dataType == SchemaDataType.GPS) {
            if (obj1 instanceof String && obj2 instanceof String) {
                return (sortDirection > 0)?((String) obj1).compareTo((String) obj2):((String) obj2).compareTo((String) obj1);
            }
        } else if (dataType == SchemaDataType.R) {
            if (obj1 instanceof Double && obj2 instanceof Double) {
                return (sortDirection > 0)?Double.compare((Double) obj1, (Double) obj2):Double.compare((Double) obj2, (Double) obj1);
            }
        } else if (dataType == SchemaDataType.I) {
            if (obj1 instanceof Integer && obj2 instanceof Integer) {
                final Integer aInt = (Integer) obj1;
                final Integer bInt = (Integer) obj2;
                return aInt > bInt ? sortDirection * 1 : (aInt == bInt) ? 0 : sortDirection * -1;
            }
        } else if (dataType == SchemaDataType.L) {
            if (obj1 instanceof Float && obj2 instanceof Float) {
                final Float aFloat = (Float) obj1;
                final Float bFloat = (Float) obj2;
                return aFloat > bFloat ? sortDirection * 1 : (aFloat == bFloat) ? 0 : sortDirection * -1;
            }
        } else if (dataType == SchemaDataType.B) {
            if (obj1 instanceof Boolean && obj2 instanceof Boolean) {
                final Boolean aBool = (Boolean) obj1;
                final Boolean bBool = (Boolean) obj2;
                return (aBool ^ bBool) ? 0 : (aBool ? sortDirection * -1 : sortDirection);
            }
        } else if (dataType == SchemaDataType.D || dataType == SchemaDataType.DT) {
            if (obj1 instanceof Date && obj2 instanceof Date) {
                return ((Date) obj1).after((Date) obj2) ? sortDirection : ((Date) obj1).before((Date) obj2) ? sortDirection * -1 : 0;
            }
        }
        return 0;
    }
}
