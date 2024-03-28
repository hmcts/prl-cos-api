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
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfidentialCheckFailed;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.class)
public class CitizenCaseUpdateServiceTest {

    @InjectMocks
    CitizenCaseUpdateService citizenCaseUpdateService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    CitizenPartyDetailsMapper citizenPartyDetailsMapper;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String caseId = "case id";
    public static final String eventId = "confirmYourDetails";

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
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);

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
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, "citizenCreate", authToken)).thenReturn(startAllTabsUpdateDataContent);
        when(citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, CitizenUpdatedCaseData.builder().build(),
            CaseEvent.CITIZEN_CASE_CREATE, authToken)).thenReturn(citizenUpdatePartyDataContent);
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
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = new CitizenUpdatePartyDataContent(caseDetails, caseData);
        when(allTabService.getStartUpdateForSpecificUserEvent(caseId, "citizenCreate", authToken)).thenReturn(startAllTabsUpdateDataContent);
        Assert.assertNull(citizenCaseUpdateService.updateCitizenPartyDetails(authToken, caseId, "citizenCreate", CitizenUpdatedCaseData.builder().build()));
    }
}
