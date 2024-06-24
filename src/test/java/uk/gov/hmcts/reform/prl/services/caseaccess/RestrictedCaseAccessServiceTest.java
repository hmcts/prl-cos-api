package uk.gov.hmcts.reform.prl.services.caseaccess;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_OR_RESPONDENT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PUBLIC;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_RESTRICTED;
import static uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum.PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum.PUBLIC;
import static uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum.RESTRICTED;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.CASE_SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.PRIVATE_CASE;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.PRIVATE_CONFIRMATION_HEADER;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.PUBLIC_CONFIRMATION_HEADER;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.PUBLIC_CONFIRMATION_SUBTEXT;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.RESTRICTED_CASE;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.RESTRICTED_CONFIRMATION_HEADER;
import static uk.gov.hmcts.reform.prl.services.caseaccess.RestrictedCaseAccessService.RESTRICTED_CONFIRMATION_SUBTEXT;

@RunWith(MockitoJUnitRunner.class)
public class RestrictedCaseAccessServiceTest {

    @Mock
    private AllTabServiceImpl allTabService;

    @InjectMocks
    private RestrictedCaseAccessService restrictedCaseAccessService;

    @Mock
    private CcdCoreCaseDataService coreCaseDataService;

    @Mock
    private ExtendedCaseDataService caseDataService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private RoleAssignmentApi roleAssignmentApi;


    @Test
    public void testInitiateUpdateCaseAccessForRestricted() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(APPLICANT_CASE_NAME, "John Smith" + RESTRICTED_CASE);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(MARK_CASE_AS_RESTRICTED.getValue())
            .caseDetails(CaseDetails.builder()
                             .data(caseDataUpdated).build())
            .build();

        caseDataUpdated = restrictedCaseAccessService.initiateUpdateCaseAccess(callbackRequest);
        assertEquals(RESTRICTED.getValue(), caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
    }

    @Test
    public void testInitiateUpdateCaseAccessForPrivate() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(APPLICANT_CASE_NAME, "John Smith" + PRIVATE_CASE);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(MARK_CASE_AS_PRIVATE.getValue())
            .caseDetails(CaseDetails.builder()
                             .data(caseDataUpdated).build())
            .build();

        caseDataUpdated = restrictedCaseAccessService.initiateUpdateCaseAccess(callbackRequest);
        assertEquals(PRIVATE.getValue(), caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
    }

    @Test
    public void testInitiateUpdateCaseAccessForPublic() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(APPLICANT_CASE_NAME, "John Smith");
        caseDataUpdated.put(APPLICANT_OR_RESPONDENT_CASE_NAME, "John Smith");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(MARK_CASE_AS_PUBLIC.getValue())
            .caseDetails(CaseDetails.builder()
                             .data(caseDataUpdated).build())
            .build();

        caseDataUpdated = restrictedCaseAccessService.initiateUpdateCaseAccess(callbackRequest);
        assertEquals(PUBLIC.getValue(), caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
    }

    @Test
    public void testChangeCaseAccess() throws JsonProcessingException {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(APPLICANT_CASE_NAME, "John Smith" + RESTRICTED_CASE);
        caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, PRIVATE.getValue());

        Map<String, Object> dataClassification = new HashMap<>();
        dataClassification.put("draftConsentOrderFile", "PUBLIC");
        dataClassification.put("effortsMadeWithRespondents", "PUBLIC");
        dataClassification.put("effortsMadeWithRespondents", "PUBLIC");
        when(caseDataService.getDataClassification(anyString())).thenReturn(dataClassification);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(MARK_CASE_AS_RESTRICTED.getValue())
            .caseDetails(CaseDetails.builder()
                             .data(caseDataUpdated).build())
            .build();

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = restrictedCaseAccessService.changeCaseAccess(
            callbackRequest);
        assertEquals(PRIVATE.toString(), aboutToStartOrSubmitCallbackResponse.getSecurityClassification());
    }

    @Test
    public void testChangeCaseAccessRequestSubmittedForRestricted() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, RESTRICTED.getValue());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(MARK_CASE_AS_RESTRICTED.getValue())
            .caseDetails(CaseDetails.builder()
                             .data(caseDataUpdated).build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent("test",
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseData.toMap(
                                                                                                            objectMapper),
                                                                                                        caseData, null
        );

        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            any(StartEventResponse.class),
            any(Classification.class)
        )).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(CaseDetails.builder().build());
        ResponseEntity<SubmittedCallbackResponse> response = restrictedCaseAccessService.changeCaseAccessRequestSubmitted(
            callbackRequest);
        assertEquals(
            RESTRICTED_CONFIRMATION_HEADER + RESTRICTED_CONFIRMATION_SUBTEXT,
            response.getBody().getConfirmationHeader()
        );
    }

    @Test
    public void testChangeCaseAccessRequestSubmittedForPrivate() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, PRIVATE.getValue());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(MARK_CASE_AS_PRIVATE.getValue())
            .caseDetails(CaseDetails.builder()
                             .data(caseDataUpdated).build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent("test",
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseData.toMap(
                                                                                                            objectMapper),
                                                                                                        caseData,
                                                                                                        null
        );

        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            any(StartEventResponse.class),
            any(Classification.class)
        )).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(CaseDetails.builder().build());
        ResponseEntity<SubmittedCallbackResponse> response = restrictedCaseAccessService.changeCaseAccessRequestSubmitted(
            callbackRequest);
        assertEquals(
            PRIVATE_CONFIRMATION_HEADER + RESTRICTED_CONFIRMATION_SUBTEXT,
            response.getBody().getConfirmationHeader()
        );
    }

    @Test
    public void testChangeCaseAccessRequestSubmittedForPublic() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, PUBLIC.getValue());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(MARK_CASE_AS_PUBLIC.getValue())
            .caseDetails(CaseDetails.builder()
                             .data(caseDataUpdated).build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent("test",
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseData.toMap(
                                                                                                            objectMapper),
                                                                                                        caseData,
                                                                                                        null
        );

        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            any(StartEventResponse.class),
            any(Classification.class)
        )).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(CaseDetails.builder().build());
        ResponseEntity<SubmittedCallbackResponse> response = restrictedCaseAccessService.changeCaseAccessRequestSubmitted(
            callbackRequest);
        assertEquals(
            PUBLIC_CONFIRMATION_HEADER + PUBLIC_CONFIRMATION_SUBTEXT,
            response.getBody().getConfirmationHeader()
        );
    }
}

