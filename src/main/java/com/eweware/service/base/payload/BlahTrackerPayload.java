package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BlahTrackerDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahTrackerSchema;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 6:58 PM
 *
 *         Dummy
 */
public class BlahTrackerPayload extends BasePayload implements BlahTrackerDAOConstants {

    protected static final BaseSchema getSchema() {
        return BlahTrackerSchema.getSchema(LocaleId.en_us);
    }

    public BlahTrackerPayload() {
        super();
    }

    public BlahTrackerPayload(String id) {
        super(id);
    }

    public BlahTrackerPayload(Map<String, Object> map) {
        super(map);
    }
}
