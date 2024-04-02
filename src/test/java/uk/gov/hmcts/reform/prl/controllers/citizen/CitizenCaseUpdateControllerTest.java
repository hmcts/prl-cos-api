package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.citizen.AccessCodeRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CitizenCaseUpdateControllerTest {

    @InjectMocks
    private CitizenCaseUpdateController citizenCaseUpdateController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CitizenCaseUpdateService citizenCaseUpdateService;

    @Mock
    private ObjectMapper objectMapper;


    public static final String authToken = "Bearer TestAuthToken";

    public static final String s2sToken = "s2s AuthToken";

    AccessCodeRequest accessCodeRequest = new AccessCodeRequest();

    AccessCodeRequest accessCodeRequestWithHearing = new AccessCodeRequest();

    CaseDetails caseDetails;

    Hearings hearings;

    @Mock
    AllTabServiceImpl allTabService;

    @Before
    public void setUp() {
        accessCodeRequest = accessCodeRequest.toBuilder()
            .caseId("123")
            .accessCode("123")
            .build();

        accessCodeRequestWithHearing = accessCodeRequest.toBuilder()
            .caseId("123")
            .accessCode("123")
            .hearingNeeded("Yes")
            .build();

        hearings = Hearings.hearingsWith().build();

        caseDetails = CaseDetails.builder().id(Long.valueOf("1223")).build();
    }

    @Test
    public void testUpdatePartyDetailsFromCitizen() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.updateCitizenPartyDetails(any(),
            any(),
            any(), any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        CaseData caseData = citizenCaseUpdateController.updatePartyDetailsFromCitizen(UpdateCaseData.builder().build(), any(), any(),
                                                                                      authToken, s2sToken);
        Assert.assertEquals(1223, caseData.getId());
    }

    @Test(expected = RuntimeException.class)
    public void testUpdatePartyDetailsFromCitizenException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.updateCitizenPartyDetails(any(),
                                                                any(),
                                                                any(), any())).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.updatePartyDetailsFromCitizen(UpdateCaseData.builder().build(), any(), any(), authToken, s2sToken);
    }

    @Test(expected = RuntimeException.class)
    public void testUpdatePartyDetailsFromCitizenAuthorizationException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        when(citizenCaseUpdateService.updateCitizenPartyDetails(any(),
                                                                any(),
                                                                any(), any())).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.updatePartyDetailsFromCitizen(UpdateCaseData.builder().build(), any(), any(), authToken, s2sToken);
    }

    @Test
    public void testSaveDraftCitizenApplication() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.saveDraftCitizenApplication(any(),
                                                                any(),
                                                                any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        CaseData caseData = citizenCaseUpdateController.saveDraftCitizenApplication(any(), authToken, s2sToken, any());
        Assert.assertEquals(1223, caseData.getId());
    }

    @Test(expected = RuntimeException.class)
    public void testSaveDraftCitizenApplicationException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.saveDraftCitizenApplication(any(),
                                                                  any(),
                                                                  any())).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.saveDraftCitizenApplication(any(), authToken, s2sToken, any());
    }


    @Test(expected = RuntimeException.class)
    public void testSaveDraftCitizenApplicationAuthorizationException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        when(citizenCaseUpdateService.saveDraftCitizenApplication(any(),
                                                                  any(),
                                                                  any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.saveDraftCitizenApplication(any(), authToken, s2sToken, any());
    }

    //@Test
    public void testSubmitC100Application() throws JsonProcessingException {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                                     .build();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        any(), caseData, null);
        when(allTabService.getStartUpdateForSpecificUserEvent(any(), any(), authToken)).thenReturn(startAllTabsUpdateDataContent);
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.saveDraftCitizenApplication(any(),
                                                                  any(),
                                                                  any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        CaseData caseDataResult = citizenCaseUpdateController.submitC100Application(any(), any(), authToken, s2sToken, any());
        Assert.assertEquals(1223, caseDataResult.getId());
    }

    @Test(expected = RuntimeException.class)
    public void testSubmitC100ApplicationException() throws JsonProcessingException {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.saveDraftCitizenApplication(any(),
                                                                  any(),
                                                                  any())).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.submitC100Application(any(), any(), authToken, s2sToken, any());
    }


    @Test(expected = RuntimeException.class)
    public void testSubmitC100ApplicationAuthoirizationException() throws JsonProcessingException {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        when(citizenCaseUpdateService.saveDraftCitizenApplication(any(),
                                                                  any(),
                                                                  any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.submitC100Application(any(), any(), authToken, s2sToken, any());
    }

    @Test
    public void testDeleteApplicationCitizen() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.deleteApplication(any(),
                                                                  any(),
                                                                  any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        CaseData caseData = citizenCaseUpdateController.deleteApplicationCitizen(any(), authToken, s2sToken, any());
        Assert.assertEquals(1223, caseData.getId());
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteApplicationCitizenException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.deleteApplication(any(),
                                                        any(),
                                                        any())).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.deleteApplicationCitizen(any(), authToken, s2sToken, any());
    }


    @Test(expected = RuntimeException.class)
    public void testDeleteApplicationCitizenAuthorizationException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        when(citizenCaseUpdateService.deleteApplication(any(),
                                                        any(),
                                                        any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.deleteApplicationCitizen(any(), authToken, s2sToken, any());
    }

    @Test
    public void testWithdrawApplicationCitizen() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.withdrawCase(any(),
                                                        any(),
                                                        any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        CaseData caseData = citizenCaseUpdateController.withdrawCase(any(), any(), authToken, s2sToken);
        Assert.assertEquals(1223, caseData.getId());
    }

    @Test(expected = RuntimeException.class)
    public void testWithdrawApplicationCitizenException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(citizenCaseUpdateService.withdrawCase(any(),
                                                   any(),
                                                   any())).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.withdrawCase(any(), any(), authToken, s2sToken);
    }

    @Test(expected = RuntimeException.class)
    public void testWithdrawApplicationCitizenAuthorizationException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        when(citizenCaseUpdateService.withdrawCase(any(),
                                                   any(),
                                                   any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        citizenCaseUpdateController.withdrawCase(any(), any(), authToken, s2sToken);
    }


}
