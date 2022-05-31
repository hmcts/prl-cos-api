package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationControllerTest {


    @InjectMocks
    private ServiceOfApplicationController serviceOfApplicationController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    private ObjectMapper objectMapper;


    @Test
    public void testServiceOfApplicationAboutToStart() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        CaseData caseData1 = CaseData.builder()
            .orderCollection(List.of(Element.<OrderDetails>builder()
                                         .value(OrderDetails.builder().orderType("Test").build())
                                         .build()))
            .build();
        caseData.put("serviceOfApplicationHeader","TestHeader");
        caseData.put("option1","1");
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(serviceOfApplicationService.populateHeader(Mockito.any(CaseData.class), Mockito.anyMap())).thenReturn(caseData);
        when(serviceOfApplicationService.getCollapsableOfSentDocuments()).thenReturn("Collapsable");
        List<String> createdOrders = new ArrayList<>();
        createdOrders.add("Standard directions order");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();


        when(serviceOfApplicationService.getOrderSelectionsEnumValues(Mockito.anyList(), Mockito.anyMap())).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .handleAboutToStart(callbackRequest);
        assertEquals("Collapsable", aboutToStartOrSubmitCallbackResponse.getData().get("sentDocumentPlaceHolder"));
        assertEquals("1", aboutToStartOrSubmitCallbackResponse.getData().get("option1"));
        assertEquals("TestHeader", aboutToStartOrSubmitCallbackResponse.getData().get("serviceOfApplicationHeader"));
    }

    @Test
    public void testServiceOfApplicationAboutToStartWillEmptyCollection() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        CaseData caseData1 = CaseData.builder()
            .orderCollection(List.of(Element.<OrderDetails>builder()
                                         .value(OrderDetails.builder().build())
                                         .build()))
            .build();
        caseData.put("serviceOfApplicationHeader","TestHeader");
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(serviceOfApplicationService.populateHeader(Mockito.any(CaseData.class), Mockito.anyMap())).thenReturn(caseData);
        when(serviceOfApplicationService.getCollapsableOfSentDocuments()).thenReturn("Collapsable");
        when(serviceOfApplicationService.getOrderSelectionsEnumValues(Mockito.anyList(), Mockito.anyMap())).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .handleAboutToStart(callbackRequest);
        assertEquals("Collapsable", aboutToStartOrSubmitCallbackResponse.getData().get("sentDocumentPlaceHolder"));
        assertEquals("TestHeader", aboutToStartOrSubmitCallbackResponse.getData().get("serviceOfApplicationHeader"));
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        CaseData cd = CaseData.builder()
            .respondentCaseInvites(Collections.emptyList())
            .build();

        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        when(objectMapper.convertValue(cd,  Map.class)).thenReturn(caseData);
        when(serviceOfApplicationService.sendEmail(callbackRequest.getCaseDetails())).thenReturn(cd);
        serviceOfApplicationController.handleAboutToSubmit(callbackRequest);
        verify(serviceOfApplicationService).sendEmail(callbackRequest.getCaseDetails());

    }
}
