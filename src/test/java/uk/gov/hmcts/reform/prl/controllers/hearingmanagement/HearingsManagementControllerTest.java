package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.exception.HearingManagementValidationException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingsUpdate;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDateRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingsManagementControllerTest {

    @InjectMocks
    private HearingsManagementController hearingsManagementController;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private HearingManagementService hearingManagementService;

    private HearingRequest hearingRequest;
    @MockBean
    private State state;

    private NextHearingDateRequest nextHearingDateRequest;

    @Before
    public void setUp() {

        hearingRequest = HearingRequest.builder()
            .hearingId("123")
            .caseRef("1669565933090179")
            .hearingUpdate(HearingsUpdate.builder()
                               .hearingResponseReceivedDateTime(LocalDate.parse("2022-11-27"))
                               .hearingEventBroadcastDateTime(LocalDate.parse("2022-11-27"))
                               .nextHearingDate(LocalDate.parse("2022-11-27"))
                               .hearingVenueId("MRD-CRT-0817")
                               .hearingVenueName("Aldershot")
                               .hmcStatus("LISTED")
                               .build())
            .build();
    }

    @Test
    public void shouldUpdateCaseStateWhenCalled() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        State caseState = DECISION_OUTCOME;
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(hearingManagementService).caseStateChangeForHearingManagement(hearingRequest,caseState);

        hearingsManagementController.caseStateUpdateByHearingManagement("s2s token", hearingRequest,caseState);
        assertTrue(true);

    }

    @Test
    public void shouldReturnErrorIfInvalidAuthTokenIsProvided() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        State caseState = DECISION_OUTCOME;
        when(authorisationService.authoriseService(any())).thenReturn(false);
        doNothing().when(hearingManagementService).caseStateChangeForHearingManagement(hearingRequest,caseState);
        assertThrows(
            HearingManagementValidationException.class,
            () -> hearingsManagementController.caseStateUpdateByHearingManagement("s2s token", hearingRequest,caseState)
        );
    }

    @Test
    public void shouldUpdateCaseNextHearingDateWhenCalled() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(hearingManagementService).caseNextHearingDateChangeForHearingManagement(nextHearingDateRequest);

        hearingsManagementController.nextHearingDateUpdateByHearingManagement("auth","s2s token", nextHearingDateRequest);
        assertTrue(true);

    }

    @Test
    public void shouldReturnErrorIfInvalidAuthTokenIsProvidedForNextHearing() throws Exception {
        when(authorisationService.authoriseUser(any())).thenReturn(false);
        when(authorisationService.authoriseService(any())).thenReturn(false);
        doNothing().when(hearingManagementService).caseNextHearingDateChangeForHearingManagement(nextHearingDateRequest);
        assertThrows(
            HearingManagementValidationException.class,
            () -> hearingsManagementController.nextHearingDateUpdateByHearingManagement("auth","s2s token", nextHearingDateRequest)
        );
    }

    @Test
    public void shouldDoNextHearingDetailsCallbackWhenAboutToSubmit() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);

        hearingsManagementController.updateNextHearingDetailsCallback("auth",callbackRequest);
        assertTrue(true);

    }
}
