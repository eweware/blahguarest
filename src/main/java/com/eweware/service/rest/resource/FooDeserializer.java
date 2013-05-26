package main.java.com.eweware.service.rest.resource;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;

import java.io.IOException;

/**
 * @author rk@post.harvard.edu
 *         Date: 5/26/13 Time: 12:21 PM
 */
public class FooDeserializer extends org.codehaus.jackson.map.JsonDeserializer<Foo> {

    @Override
    public Foo deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        System.out.println("=== Deserializing:\n" + node + "\n===");
        final Foo foo = new Foo();
        Long val = node.get("x").getLongValue();
        foo.put("x", val);
        return foo;
    }
}
