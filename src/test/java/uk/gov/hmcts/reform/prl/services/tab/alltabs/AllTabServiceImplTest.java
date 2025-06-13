package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class AllTabServiceImplTest {

    @InjectMocks
    AllTabServiceImpl allTabService;

    @Mock
    ApplicationsTabService applicationsTabService;

    @Mock
    IdamClient idamClient;

    @Mock
    ConfidentialityTabService confidentialityTabService;

    @Mock
    CcdCoreCaseDataService ccdCoreCaseDataService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SystemUserService systemUserService;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Mock
    CaseData nocCaseData;

    private StartEventResponse startEventResponse;
    private CaseDetails caseDetails;
    private CaseData caseData;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    private final String eventName = CaseEvent.UPDATE_ALL_TABS.getValue();
    private final String caseId = "1234567891011121";

    @BeforeEach
    void setUp() {
        caseDetails = CaseDetails.builder().id(Long.valueOf("123")).data(Map.of("id", caseId)).build();
        caseData = CaseData.builder().id(Long.parseLong(caseId)).build();
        String eventToken = "eventToken";
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
    }

    @Test
    void testUpdateAllTabsIncludingConfTab() {
        CaseDetails returnedCaseDetails = allTabService.updateAllTabsIncludingConfTab(caseId);
        assertNotNull(returnedCaseDetails);
        verify(ccdCoreCaseDataService, Mockito.times(1)).startUpdate(anyString(), any(), anyString(), anyBoolean());
        verify(ccdCoreCaseDataService, Mockito.times(1)).submitUpdate(
            anyString(),
            any(),
            any(),
            anyString(),
            anyBoolean()
        );
        verify(applicationsTabService, Mockito.times(1)).updateTab(caseData);
        verify(caseSummaryTabService, Mockito.times(1)).updateTab(caseData);
        verify(confidentialityTabService, Mockito.times(1)).updateConfidentialityDetails(caseData);
    }

    @Test
    void testUpdateAllTabsIncludingConfTabWithInvalidCaseId() {
        CaseDetails returnedCaseDetails = allTabService.updateAllTabsIncludingConfTab("");
        assertNull(returnedCaseDetails);
        verify(ccdCoreCaseDataService, Mockito.never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
        verify(ccdCoreCaseDataService, Mockito.never()).submitUpdate(
            anyString(),
            any(),
            any(),
            anyString(),
            anyBoolean()
        );
        verify(applicationsTabService, Mockito.never()).updateTab(caseData);
        verify(caseSummaryTabService, Mockito.never()).updateTab(caseData);
        verify(confidentialityTabService, Mockito.never()).updateConfidentialityDetails(caseData);
    }

    @Test
    void testGetStartUpdateForSpecificEvent() {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
            caseId,
            eventName
        );
        assertNotNull(startAllTabsUpdateDataContent);
        verify(ccdCoreCaseDataService, Mockito.times(1)).startUpdate(anyString(), any(), anyString(), anyBoolean());
    }

    @Test
    void testAllTabFields() {
        assertNotNull(allTabService.getAllTabsFields(nocCaseData));
    }

    @Test
    void testUpdatePartyDetailsForNocC100Applicant() {
        when(nocCaseData.getCaseTypeOfApplication()).thenReturn("C100");
        allTabService.updatePartyDetailsForNoc(
            "auth",
            "caseId",
            startEventResponse,
            EventRequestData.builder().build(), nocCaseData
        );

        verify(ccdCoreCaseDataService, Mockito.times(1)).submitUpdate(
            anyString(),
            any(),
            any(),
            anyString(),
            anyBoolean()
        );
    }

    @Test
    void testUpdatePartyDetailsForNocFL401Applicant() {
        when(nocCaseData.getCaseTypeOfApplication()).thenReturn("FL401");
        allTabService.updatePartyDetailsForNoc(
            "auth",
            "caseId",
            startEventResponse,
            EventRequestData.builder().build(), nocCaseData
        );
        verify(ccdCoreCaseDataService, Mockito.times(1)).submitUpdate(
            anyString(),
            any(),
            any(),
            anyString(),
            anyBoolean()
        );
    }

    @Test
    void testGetStartUpdateForSpecificUserEvent() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().roles(List.of(Roles.SOLICITOR.getValue())).id(
            "123").build());
        when(ccdCoreCaseDataService.startUpdate(AUTH_TOKEN, null, caseId, true))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificUserEvent(
            caseId,
            eventName,
            AUTH_TOKEN
        );
        assertNotNull(startAllTabsUpdateDataContent);
    }

    @Test
    void testGetStartUpdateForSpecificUserEventCitizen() {

        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().roles(List.of(Roles.CITIZEN.getValue())).id(
            "123").build());
        when(ccdCoreCaseDataService.startUpdate(AUTH_TOKEN, null, caseId, false))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificUserEvent(
            caseId,
            eventName,
            AUTH_TOKEN
        );
        assertNotNull(startAllTabsUpdateDataContent);
    }

    @Test
    void testSubmitUpdateForSpecificUserEvent() {
        CaseDetails caseDetails1 = allTabService.submitUpdateForSpecificUserEvent(
            AUTH_TOKEN,
            caseId,
            startEventResponse,
            EventRequestData.builder().build(),
            new HashMap<>(),
            UserDetails.builder().roles(List.of(Roles.SOLICITOR.getValue())).build()
        );
        assertNotNull(caseDetails1);
    }

    @Test
    void testSubmitUpdateForSpecificUserEventCitizen() {
        CaseDetails caseDetails1 = allTabService.submitUpdateForSpecificUserEvent(
            AUTH_TOKEN,
            caseId,
            startEventResponse,
            EventRequestData.builder().build(),
            new HashMap<>(),
            UserDetails.builder().roles(List.of(Roles.CITIZEN.getValue())).build()
        );
        assertNotNull(caseDetails1);
    }

    @Test
    void testUpdatePartyDetailsForNocC100ApplicantForMiamPolicyUpgradeDocumentMap() {
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse))
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuDomesticAbuseEvidenceDocument(List.of(Element.<DomesticAbuseEvidenceDocument>builder().build()))
            .build();
        caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .caseTypeOfApplication("C100")
            .build();
        allTabService.updatePartyDetailsForNoc(
            "auth",
            "caseId",
            startEventResponse,
            EventRequestData.builder().build(), caseData
        );

        verify(ccdCoreCaseDataService, Mockito.times(1)).submitUpdate(
            anyString(),
            any(),
            any(),
            anyString(),
            anyBoolean()
        );
    }

    @Test
    void testUpdatePartyDetailsForNocC100ApplicantForMiamPolicyUpgradeDocumentMap2() {
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(Yes)
            .mpuApplicantAttendedMiam(Yes)
            .mpuClaimingExemptionMiam(Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(Yes)
            .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance))
            .mpuDocFromDisputeResolutionProvider(Document.builder().build())
            .mpuIsDomesticAbuseEvidenceProvided(Yes)
            .mpuDomesticAbuseEvidenceDocument(List.of(Element.<DomesticAbuseEvidenceDocument>builder().build()))
            .build();
        caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .caseTypeOfApplication("C100")
            .build();
        allTabService.updatePartyDetailsForNoc(
            "auth",
            "caseId",
            startEventResponse,
            EventRequestData.builder().build(), caseData
        );

        verify(ccdCoreCaseDataService, Mockito.times(1)).submitUpdate(
            anyString(),
            any(),
            any(),
            anyString(),
            anyBoolean()
        );
    }
}
