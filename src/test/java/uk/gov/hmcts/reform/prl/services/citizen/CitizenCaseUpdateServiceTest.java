package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.caseflags.request.LanguageSupportCaseNotesRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfidentialCheckFailed;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_CREATE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.class)
public class CitizenCaseUpdateServiceTest {

    @InjectMocks
    CitizenCaseUpdateService citizenCaseUpdateService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    AddCaseNoteService addCaseNoteService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CitizenPartyDetailsMapper citizenPartyDetailsMapper;

    @Mock
    PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @Mock
    MiamPolicyUpgradeService miamPolicyUpgradeService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String caseId = "case id";
    public static final String eventId = "confirmYourDetails";
    private static PartyDetails partyDetails;

    @Test
    public void testUpdateCitizenPartyDetailsConfirmYourDetails() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().personalServiceBy("courtAdmin").build())
                                      .applicationServedYesNo(YesOrNo.Yes)
                                      .build()).build();

        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, eventId, authToken)).thenReturn(startAllTabsUpdateDataContent);
        when(citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, CitizenUpdatedCaseData.builder().build(),
            CaseEvent.CONFIRM_YOUR_DETAILS, authToken)).thenReturn(citizenUpdatePartyDataContent);
        when(allTabService.updateAllTabsIncludingConfTab(caseId)).thenReturn(CaseDetails.builder().build());
        Assert.assertNotNull(citizenCaseUpdateService.updateCitizenPartyDetails(authToken, caseId, "confirmYourDetails",
            CitizenUpdatedCaseData.builder().build()));
    }

    @Test
    public void testUpdateCitizenPartyDetailsCreateCase() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().personalServiceBy("courtAdmin").build())
                                      .applicationServedYesNo(YesOrNo.Yes)
                                      .build()).build();

        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, "citizenCreate", authToken)).thenReturn(startAllTabsUpdateDataContent);
        when(citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, CitizenUpdatedCaseData.builder().build(),
                                                              CITIZEN_CASE_CREATE, authToken)).thenReturn(citizenUpdatePartyDataContent);
        when(allTabService.submitUpdateForSpecificUserEvent(anyString(), anyString(), any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());
        Assert.assertNotNull(citizenCaseUpdateService.updateCitizenPartyDetails(authToken, caseId, "citizenCreate",
                                                                                CitizenUpdatedCaseData.builder().build()));
    }

    @Test
    public void testUpdateCitizenPartyDetailsReturnsNull() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().personalServiceBy("courtAdmin").build())
                                      .applicationServedYesNo(YesOrNo.Yes)
                                      .build()).build();

        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, "citizenCreate", authToken))
            .thenReturn(startAllTabsUpdateDataContent);
        Assert.assertNull(citizenCaseUpdateService.updateCitizenPartyDetails(authToken, caseId, "citizenCreate",
                                                                             CitizenUpdatedCaseData.builder().build()));
    }

    @Test
    public void testSaveDraftCitizenApplication() throws IOException {
        C100RebuildData c100RebuildData = getC100RebuildData();
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().personalServiceBy("courtAdmin").build())
                                      .applicationServedYesNo(YesOrNo.Yes)

                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, "citizenSaveC100DraftInternal", authToken))
            .thenReturn(startAllTabsUpdateDataContent);
        /*when(citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, CitizenUpdatedCaseData.builder().build(),
                                                              CITIZEN_CASE_CREATE, authToken)).thenReturn(citizenUpdatePartyDataContent);*/
        when(allTabService.submitUpdateForSpecificUserEvent(any(), any(), any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());
        Assert.assertNotNull(citizenCaseUpdateService.saveDraftCitizenApplication(caseId,
                                                                                  caseData, authToken));
    }

    @Test
    public void testDeleteApplication() throws IOException {
        C100RebuildData c100RebuildData = getC100RebuildData();

        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().personalServiceBy("courtAdmin").build())
                                      .applicationServedYesNo(YesOrNo.Yes)

                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, "deleteApplication", authToken)).thenReturn(startAllTabsUpdateDataContent);
        /*when(citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, CitizenUpdatedCaseData.builder().build(),
                                                              CITIZEN_CASE_CREATE, authToken)).thenReturn(citizenUpdatePartyDataContent);*/
        when(allTabService.submitUpdateForSpecificUserEvent(any(), any(), any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());
        Assert.assertNotNull(citizenCaseUpdateService.deleteApplication(caseId, caseData, authToken));
    }

    @Test
    public void testSubmitApplication() throws IOException {
        C100RebuildData c100RebuildData = getC100RebuildData();
        partyDetails = PartyDetails.builder().build();
        Long caseIdSubmit = 12345L;

        CaseData caseData = CaseData.builder().id(caseIdSubmit)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .c100RebuildData(c100RebuildData)
            .applicants(List.of(element(partyDetails)))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().personalServiceBy("courtAdmin").build())
                                      .applicationServedYesNo(YesOrNo.Yes)

                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseDetails,
            caseData,
            UserDetails.builder().build()
        );
        when(citizenPartyDetailsMapper.buildUpdatedCaseData(
            any(),
            any()
        )).thenReturn(caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(anyString(), anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitUpdateForSpecificUserEvent(any(), any(), any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().id(caseIdSubmit).build());
        when(objectMapper.convertValue(any(CaseData.class), eq(Map.class))).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateAndStoreCaseFlags(String.valueOf(caseIdSubmit)))
            .thenReturn(CaseDetails.builder().id(caseIdSubmit).build());
        Assert.assertNotNull(citizenCaseUpdateService.submitCitizenC100Application(
            authToken,
            String.valueOf(caseId),
            "citizenSaveC100DraftInternal",
            caseData
        ));
    }

    @Test
    public void testWithdrawCaseApplication() throws IOException {
        C100RebuildData c100RebuildData = getC100RebuildData();
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(YesOrNo.Yes)
            .withDrawApplicationReason("Test data")
            .build();

        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .withDrawApplicationData(withdrawApplication).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, "citizenCaseWithdraw", authToken)).thenReturn(startAllTabsUpdateDataContent);
        /*when(citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, CitizenUpdatedCaseData.builder().build(),
                                                              CITIZEN_CASE_CREATE, authToken)).thenReturn(citizenUpdatePartyDataContent);*/
        when(allTabService.submitUpdateForSpecificUserEvent(any(), any(), any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());
        when(allTabService.updateAllTabsIncludingConfTab(caseId)).thenReturn(CaseDetails.builder().build());
        Assert.assertNotNull(citizenCaseUpdateService.withdrawCase(caseData, caseId, authToken));
    }

    @Test
    public void testaddLanguageSupportCaseNotes() throws IOException {
        C100RebuildData c100RebuildData = getC100RebuildData();
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails, caseData, null);

        when(allTabService.getStartUpdateForSpecificUserEvent(any(),any(),any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitUpdateForSpecificUserEvent(any(), any(), any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());
        when(addCaseNoteService.getCurrentCaseNoteDetails(any(),any(),any())).thenReturn(CaseNoteDetails.builder().build());
        LanguageSupportCaseNotesRequest languageSupportCaseNotesRequest = LanguageSupportCaseNotesRequest.builder().languageSupportNotes("test")
            .partyIdamId("1234567").build();

        Assert.assertNotNull(citizenCaseUpdateService.addLanguageSupportCaseNotes(caseId, authToken,languageSupportCaseNotesRequest));
    }

    private static C100RebuildData getC100RebuildData() throws IOException {
        C100RebuildData c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
            .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op.json"))
            .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam.json"))
            .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu.json"))
            .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd.json"))
            .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra.json"))
            .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs.json"))
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .applicantPcqId(PrlAppsConstants.TEST_UUID)
            .build();
        return c100RebuildData;
    }
}

