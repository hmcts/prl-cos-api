package uk.gov.hmcts.reform.prl.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class NonEmptyObjectMapper {

    private NonEmptyObjectMapper() {
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

        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        om.registerModule(new ParameterNamesModule());
        om.enable(SerializationFeature.INDENT_OUTPUT);

        return om;
    }
}
