package uk.gov.hmcts.reform.prl.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CcdObjectMapper {

    private CcdObjectMapper() {
        throw new IllegalStateException("Utility class");
    }

    private static ObjectMapper om = null;

    public static ObjectMapper getObjectMapper() {
        if (om == null) {
            om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
        }
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enable(SerializationFeature.WRITE_ENUM_KEYS_USING_INDEX);
        om.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        return om;
    }
}
