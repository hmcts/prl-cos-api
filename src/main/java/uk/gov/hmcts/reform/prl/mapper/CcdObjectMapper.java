package uk.gov.hmcts.reform.prl.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CcdObjectMapper {

    private static final ThreadLocal<ObjectMapper> om = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.enable(SerializationFeature.WRITE_ENUM_KEYS_USING_INDEX);
            return objectMapper;
        }
    };

    /**
     * Flyweight thread local objectMapper.  Users of this should not keep a reference to this.
     * @return an ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return om.get();
    }
}
