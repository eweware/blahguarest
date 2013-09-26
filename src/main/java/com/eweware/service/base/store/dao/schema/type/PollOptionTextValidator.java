package com.eweware.service.base.store.dao.schema.type;

import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.store.dao.PollOptionDAOConstants;
import com.eweware.service.base.store.dao.schema.SchemaSpec;
import com.eweware.service.base.store.impl.mongo.dao.PollOptionTextImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Validates and converts embedded PollOptionTextDAO objects.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/11/13 Time: 1:44 PM
 */
public class PollOptionTextValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) throws SystemErrorException {
        List<Map<String, Object>> v = (List<Map<String, Object>>) value;
        List<PollOptionTextImpl> result = new ArrayList<PollOptionTextImpl>();
        for (Map<String, Object> item : v) {
            result.add(new PollOptionTextImpl((String) item.get(PollOptionDAOConstants.TAGLINE), (String) item.get(PollOptionDAOConstants.TEXT)));
        }
        return result;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        final boolean isList = value instanceof List<?>;
        if (isList) {
            final List<?> list = (List<?>) value;
            if (list.size() != 0) {
                final boolean isMap = (list.get(0) instanceof Map<?, ?>);
                if (isMap) {
                    final Map<?, ?> map = (Map<?, ?>) list.get(0);
                    if (map.size() > 0) {
                        final Map.Entry<?, ?> entry = map.entrySet().iterator().next();
                        return (entry.getKey() instanceof String) && (entry.getValue() instanceof String);
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } else {
               return true;
            }
        }
        return false;
    }
}
