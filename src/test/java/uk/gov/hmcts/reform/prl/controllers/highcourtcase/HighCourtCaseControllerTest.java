package uk.gov.hmcts.reform.prl.controllers.highcourtcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CourtIdentifier;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CourtIdentifierGenerator;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HIGH_COURT;
import static uk.gov.hmcts.reform.prl.utils.RequestBuilder.buildCallbackRequest;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.ID;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.IS_HIGH_COURT_CASE_YES;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_SERVICE_AUTHORIZATION;


@RunWith(MockitoJUnitRunner.Silent.class)
public class HighCourtCaseControllerTest {
    private static final String IS_HIGH_COURT_CASE = "isHighCourtCase";

    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private CourtIdentifierGenerator courtIdentifierGenerator;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EventService eventPublisher;

    @InjectMocks
    private HighCourtCaseController highCourtCaseController;

    @Test(expected = RuntimeException.class)
    public void testHighCourtDecisionWhenAuthorisationFails() {
        // given
        when(authorisationService.isAuthorized(TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION)).thenReturn(false);

        // when
        highCourtCaseController.handleAboutToSubmit(TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION, null);

        // then
        verify(authorisationService).isAuthorized(TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION);
    }


    @Test
    public void testHighCourtDecision() {
        // given
        when(authorisationService.isAuthorized(TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION)).thenReturn(true);
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        data.put(IS_HIGH_COURT_CASE, IS_HIGH_COURT_CASE_YES);
        CaseData caseData = CaseData.builder().id(ID).isHighCourtCase(YesOrNo.Yes).build();
        callbackRequest.setCaseDetails(caseDetails);
        when(objectMapper.convertValue(any(Map.class), eq(CaseData.class))).thenReturn(caseData);
        when(courtIdentifierGenerator.courtIdentifierFromCaseData(any(CaseData.class)))
            .thenReturn(CourtIdentifier.builder().courtIdentifier(HIGH_COURT).build());

        // when
        AboutToStartOrSubmitCallbackResponse response = highCourtCaseController.handleAboutToSubmit(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            callbackRequest
        );

        // then
        assertNotNull(response);
        Map<String, Object> responseData = response.getData();
        assertNotNull(responseData);
        CourtIdentifier courtIdentifier = (CourtIdentifier) responseData.get("courtIdentifier");
        assertEquals(HIGH_COURT, courtIdentifier.getCourtIdentifier());

    }

}
