package uk.gov.hmcts.reform.prl.clients;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class HearingApiClientIntegrationTest {

    @Autowired
    private HearingApiClient hearingApiClient;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("hearing.preview.bypass.enabled", () -> "false");
    }

    @Test
    void shouldThrowException() {
        assertThat(hearingApiClient)
            .isNotNull();

        assertThatThrownBy(() -> hearingApiClient.getHearingDetails(
            "auth",
            "serviceAuth",
            "12345"))
            .isInstanceOf(feign.FeignException.class);
    }
}
