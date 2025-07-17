package uk.gov.hmcts.reform.prl.utils.verifiers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonConfigurationVerifierTest {

    @Test
    void shouldThrowExceptionWhenObjectMapperIsConfiguredIncorrectly() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getFactory().enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);

        assertThrows(IllegalStateException.class, () -> new JacksonConfigurationVerifier(objectMapper),
            "Jackson ObjectMapper is configured with AUTO_CLOSE_JSON_CONTENT enabled. "
                + "This can cause issues with streaming JSON responses and must be disabled.");
    }

    @Test
    void shouldNotThrowExceptionWhenObjectMapperIsConfiguredCorrectly() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);

        assertDoesNotThrow(() -> new JacksonConfigurationVerifier(objectMapper));
    }

}
