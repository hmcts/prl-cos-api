package uk.gov.hmcts.reform.prl.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CcdObjectMapper {

    private static final ObjectMapper om = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enable(SerializationFeature.WRITE_ENUM_KEYS_USING_INDEX);
        return om;
    }
}
