package uk.gov.hmcts.reform.prl.controllers.closingcase;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import static org.mockito.Mockito.*;

class ReturnToPreviousStateControllerTest {

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private AllTabServiceImpl tabService;

    @Mock
    private EventService eventService;

    private ReturnToPreviousStateController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ReturnToPreviousStateController(
            new ObjectMapper(),
            eventService,
            authorisationService,
            tabService
        );
    }

    @Test
    void shouldCallUpdateAllTabsIncludingConfTab() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .state("DECISION_OUTCOME")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        caseDetails.setId(12345L);
        callbackRequest.setCaseDetails(caseDetails);

        String authorisation = "authorisation";
        controller.handleSubmitted(authorisation, callbackRequest);

        verify(tabService, times(1)).updateAllTabsIncludingConfTab("12345");
        verifyNoMoreInteractions(tabService);
    }
}
