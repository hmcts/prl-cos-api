package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;
import static uk.gov.hmcts.reform.prl.controllers.ConfidentialityCheckController.NO_PACKS_AVAILABLE_FOR_CONFIDENTIAL_DETAILS_CHECK;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConfidentialityCheckControllerTest {

    @InjectMocks
    private ConfidentialityCheckController confidentialityCheckController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void testPackAvailable() {

        CaseData caseData = CaseData.builder().id(12345L).serviceOfApplication(ServiceOfApplication.builder()
                                                                                   .unServedApplicantPack(SoaPack.builder().packDocument(
                                                                                       List.of(element(Document.builder().documentBinaryUrl(
                                                                                           "abc").documentFileName("ddd").build()))).build())
                                                                                   .build()).build();

        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = confidentialityCheckController
            .confidentialCheckAboutToStart(callbackRequest);
        assertNull(aboutToStartOrSubmitCallbackResponse.getErrors());
    }

    @Test
    public void respondentTestPackAvailable() {

        CaseData caseData = CaseData.builder().id(12345L).serviceOfApplication(ServiceOfApplication.builder()
                                                                                   .unServedRespondentPack(SoaPack.builder().packDocument(
                                                                                       List.of(element(Document.builder().documentBinaryUrl(
                                                                                           "abc").documentFileName("ddd").build()))).build())
                                                                                   .build()).build();

        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = confidentialityCheckController
            .confidentialCheckAboutToStart(callbackRequest);
        assertNull(aboutToStartOrSubmitCallbackResponse.getErrors());
    }

    @Test
    public void OtherPeopleTestPackAvailable() {

        CaseData caseData = CaseData.builder().id(12345L).serviceOfApplication(ServiceOfApplication.builder()
                                                                                   .unServedOthersPack(SoaPack.builder().packDocument(
                                                                                       List.of(element(Document.builder().documentBinaryUrl(
                                                                                           "abc").documentFileName("ddd").build()))).build())
                                                                                   .build()).build();

        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = confidentialityCheckController
            .confidentialCheckAboutToStart(callbackRequest);
        assertNull(aboutToStartOrSubmitCallbackResponse.getErrors());
    }

    @Test
    public void testNoPackAvailable() {

        CaseData caseData = CaseData.builder().id(12345L).serviceOfApplication(ServiceOfApplication.builder()
                                                                              .build()).build();

        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = confidentialityCheckController
            .confidentialCheckAboutToStart(callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getErrors());
        assertTrue(aboutToStartOrSubmitCallbackResponse.getErrors().contains(NO_PACKS_AVAILABLE_FOR_CONFIDENTIAL_DETAILS_CHECK));
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse = ResponseEntity.noContent().build();
        when(serviceOfApplicationService.processConfidentialityCheck("", callbackRequest)).thenReturn(submittedCallbackResponse);
        assertNull(confidentialityCheckController.handleSubmittedNew("test auth",callbackRequest));
    }
}
