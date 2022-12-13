package com.stxs.searchservice.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;

@Component
public class JsonUtils {
    public JsonUtils() {

    }
    public <T> String toJsonString(T object) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stringWriter);
        jsonGenerator.setCodec(new ObjectMapper());
        jsonGenerator.writeObject(object);
        jsonGenerator.close();
        final String s = stringWriter.toString();
        final String replacement = Matcher.quoteReplacement("\\\"");
        final String output = s.replaceAll("\"", replacement);
        System.out.println(output);
        System.out.println(stringWriter.toString());
        return output;
    }
}
