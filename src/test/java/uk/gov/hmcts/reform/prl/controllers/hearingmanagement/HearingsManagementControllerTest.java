package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.exception.HearingManagementValidationException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingsUpdate;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDateRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingsManagementControllerTest {

    @InjectMocks
    private HearingsManagementController hearingsManagementController;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private HearingManagementService hearingManagementService;

    private HearingRequest hearingRequest;

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

        nextHearingDateRequest = nextHearingDateRequest.builder()
            .caseRef("1669565933090179")
            .nextHearingDetails(NextHearingDetails.builder()
                               .nextHearingDate(LocalDateTime.parse("2023-04-13T09:00:00"))
                               .hearingId("2000004862")
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
        String caseState = "test";
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(hearingManagementService).caseStateChangeForHearingManagement(hearingRequest,caseState);
        hearingsManagementController.caseStateUpdateByHearingManagement("s2s token", hearingRequest, caseState);
        assertTrue(true);

    }

    @Test
    public void shouldReturnErrorIfInvalidAuthTokenIsProvided() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        String caseState = "test";
        when(authorisationService.authoriseService(any())).thenReturn(false);
        doNothing().when(hearingManagementService).caseStateChangeForHearingManagement(hearingRequest,caseState);
        assertThrows(
            HearingManagementValidationException.class,
            () ->  hearingsManagementController.caseStateUpdateByHearingManagement("s2s token", hearingRequest, caseState)
        );
    }

    @Test
    public void shouldUpdateCaseNextHearingDateWhenCalled() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        doNothing().when(hearingManagementService).caseNextHearingDateChangeForHearingManagement(nextHearingDateRequest);

        hearingsManagementController.nextHearingDateUpdateByHearingManagement("s2s token", nextHearingDateRequest);
        assertTrue(true);

    }

    @Test
    public void shouldReturnErrorIfInvalidAuthTokenIsProvidedForNextHearing() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(false);
        doNothing().when(hearingManagementService).caseNextHearingDateChangeForHearingManagement(nextHearingDateRequest);
        assertThrows(
            HearingManagementValidationException.class,
            () -> hearingsManagementController.nextHearingDateUpdateByHearingManagement("s2s token", nextHearingDateRequest)
        );
    }

}
