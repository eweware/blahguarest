package main.java.com.eweware.service.rest.resource;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 5/26/13 Time: 12:19 PM
 */

@JsonDeserialize(using = FooDeserializer.class)
public class Foo extends LinkedHashMap<String, Object> implements Serializable {

    public Long getBar() {
        final Object x = get("x");
        System.out.println("=== getBar " + x + ((x instanceof Long) ? " a long" : " not a long") + " ===");
        return (Long) x;
    }
}