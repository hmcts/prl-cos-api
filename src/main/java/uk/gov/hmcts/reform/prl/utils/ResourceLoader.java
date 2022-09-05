package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceLoader {

    public static String loadJson(final String filePath) throws Exception {
        return new String(loadResource(filePath), Charset.forName("utf-8"));
    }

    public static byte[] loadResource(final String filePath) throws Exception {
        InputStream io = ResourceLoader.class.getClassLoader().getResourceAsStream(filePath);

        if (io == null) {
            throw new IllegalArgumentException(String.format("Could not find resource in path %s", filePath));
        }

        byte[] returnValue =  io.readAllBytes();

        if (io != null) {
            io.close();
        }
        return returnValue;
    }

    public static <T> T loadJsonToObject(String filePath, Class<T> type) {
        try {
            return new ObjectMapper().readValue(loadJson(filePath), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String objectToJson(T object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
