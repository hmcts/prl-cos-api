package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CREATED_BY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CONFIDENTIAL_DETAILS_PRESENT;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationControllerTest {

    @InjectMocks
    private ServiceOfApplicationController serviceOfApplicationController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
    private final Long caseId = 1234567887654321L;
    private final String eventName = "internal-update-all-tabs";

    @Test
    public void testServiceOfApplicationAboutToStart() throws Exception {

        Map<String, Object> caseData = new HashMap<>();

        caseData.put(SOA_CONFIDENTIAL_DETAILS_PRESENT, YesOrNo.Yes);
        caseData.put(CASE_CREATED_BY, CaseCreatedBy.SOLICITOR);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        when(serviceOfApplicationService.getSoaCaseFieldsMap(callbackRequest.getCaseDetails())).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .handleAboutToStart(callbackRequest);
        assertEquals(YesOrNo.Yes, aboutToStartOrSubmitCallbackResponse.getData().get(SOA_CONFIDENTIAL_DETAILS_PRESENT));
        assertEquals(CaseCreatedBy.SOLICITOR, aboutToStartOrSubmitCallbackResponse.getData().get(CASE_CREATED_BY));
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        CaseData caseData = CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName("xyz").build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();


        when(objectMapper.convertValue(stringObjectMap,  CaseData.class)).thenReturn(caseData);

        when(launchDarklyClient.isFeatureEnabled("soa-access-code-gov-notify")).thenReturn(false);

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);

        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.triggerEvent(jurisdiction, caseType, caseId, eventName, eventData);

        final ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponseResponseEntity = serviceOfApplicationController.handleSubmitted(
            "test auth",
            callbackRequest);

        assertEquals(HttpStatus.OK, submittedCallbackResponseResponseEntity.getStatusCode());
    }

    @Test
    public void testHandleAboutToSubmitWhenProceedToServingNo() throws Exception {
        CaseData caseData = CaseData.builder().id(Long.parseLong(TEST_CASE_ID))
            .serviceOfApplication(ServiceOfApplication.builder().proceedToServing(YesOrNo.No).build())
                .applicantCaseName("xyz").build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();


        when(objectMapper.convertValue(stringObjectMap,  CaseData.class)).thenReturn(caseData);

        when(launchDarklyClient.isFeatureEnabled("soa-access-code-gov-notify")).thenReturn(false);

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);

        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.triggerEvent(jurisdiction, caseType, caseId, eventName, eventData);

        final ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponseResponseEntity = serviceOfApplicationController.handleSubmitted(
            "test auth",
            callbackRequest);

        assertEquals(HttpStatus.OK, submittedCallbackResponseResponseEntity.getStatusCode());
    }
}
