package uk.gov.hmcts.reform.prl.clients.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.util.Collections;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class HearingApiBypassClientTest {

    private HearingApiClient hearingApiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        hearingApiClient = new HearingApiBypassClient(objectMapper);
    }

    @Test
    void testHearingDetails() {
        Hearings hearingDetails = hearingApiClient.getHearingDetails(
            "auth",
            "serviceAuth",
            "12345");

        assertThat(hearingDetails)
            .isNotNull();
        assertThat(hearingDetails.getHmctsServiceCode())
            .isEqualTo("ABA5");
        assertThat(hearingDetails.getCaseHearings())
            .hasSize(2);
    }

    @Test
    void testHearingDetailsException() throws JsonProcessingException {
        ObjectMapper spy = spy(objectMapper);
        hearingApiClient = new HearingApiBypassClient(spy);
        doThrow(new RuntimeException("Failed")).when(spy).readValue(anyString(), eq(Hearings.class));

        assertThatThrownBy(() -> hearingApiClient.getHearingDetails(
            "auth",
            "serviceAuth",
            "12345")
        ).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed");
    }

    @Test
    void testCaseLinkedData() {
        exceptionAssertion(() -> () -> hearingApiClient.getCaseLinkedData(
            "auth",
            "serviceAuth",
            CaseLinkedRequest.caseLinkedRequestWith().build()
        ));
    }

    @Test
    void testNextHearingDate() {
        exceptionAssertion(() -> () -> hearingApiClient.getNextHearingDate(
            "auth",
            "serviceAuth",
            "12345"
        ));
    }

    @Test
    void testFutureHearings() {
        exceptionAssertion(() -> () -> hearingApiClient.getFutureHearings(
            "auth",
            "serviceAuth",
            "12345"
        ));
    }

    @Test
    void testHearingsByListOfCaseIds() {
        exceptionAssertion(() -> () -> hearingApiClient.getHearingsByListOfCaseIds(
            "auth",
            "serviceAuth",
            Collections.emptyMap()
        ));
    }

    @Test
    void testHearingsForAllCaseIdsWithCourtVenue() {
        exceptionAssertion(() -> () -> hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(
            "auth",
            "serviceAuth",
            Collections.emptyList()
        ));
    }

    @Test
    void testListedHearingsForAllCaseIdsOnCurrentDate() {
        exceptionAssertion(() -> () -> hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(
            "auth",
            "serviceAuth",
            Collections.emptyList()
        ));
    }

    @Test
    void createAutomatedHearing() {
        exceptionAssertion(() -> () -> hearingApiClient.createAutomatedHearing(
            "auth",
            "serviceAuth",
            AutomatedHearingCaseData.automatedHearingCaseDataBuilder().build()
        ));
    }

    private void exceptionAssertion(Supplier<ThrowableAssert.ThrowingCallable> shouldRaiseThrowable) {
        assertThatThrownBy(shouldRaiseThrowable.get())
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Feign call not supported from bypass api");
    }
}
