package uk.gov.hmcts.reform.prl.clients.hearing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
public class HearingApiBypassClientTest {

    @Autowired(required = false)
    private HearingApiClient hearingApiClient;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("hearing.preview.bypass.enabled", () -> "true");
    }

    @Test
    void shouldReturnHearings() {
        assertThat(hearingApiClient)
            .isNotNull();
        Hearings hearingDetails = hearingApiClient.getHearingDetails(anyString(), anyString(), anyString());
        assertThat(hearingDetails)
            .isNotNull();
        assertThat(hearingDetails.getHmctsServiceCode())
            .isEqualTo("ABA5");
        assertThat(hearingDetails.getCaseHearings())
            .hasSize(2);
    }

    @Test
    void shouldThrowException() {
        assertThat(hearingApiClient)
            .isNotNull();
        assertThatThrownBy(() -> hearingApiClient.getFutureHearings(anyString(), anyString(), anyString()))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Feign call not supported from bypass api");
    }
}
