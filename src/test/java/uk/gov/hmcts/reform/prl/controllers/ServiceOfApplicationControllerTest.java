package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationControllerTest {

    @InjectMocks
    private ServiceOfApplicationController serviceOfApplicationController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private WelshCourtEmail welshCourtEmail;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Ignore
    @Test
    public void testServiceOfApplicationAboutToStart() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("first")
            .lastName("last")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first")
            .representativeLastName("last")
            .build();
        Map<String, Object> caseData = new HashMap<>();
        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(partyDetails)))
            .respondents(List.of(element(partyDetails)))
            .orderCollection(List.of(Element.<OrderDetails>builder()
                                         .value(OrderDetails.builder()
                                                    .otherDetails(OtherOrderDetails.builder().orderCreatedDate("").build())
                                                    .orderType("Test").build())
                                         .build()))
            .build();
        caseData.put("serviceOfApplicationHeader","TestHeader");
        caseData.put("option1","1");
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);

        when(serviceOfApplicationService.getCollapsableOfSentDocuments()).thenReturn("Collapsable");
        List<String> createdOrders = new ArrayList<>();
        createdOrders.add("Standard directions order");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        String courtEmail = "test1@test.com";
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(any())).thenReturn(courtEmail);
        when(serviceOfApplicationService.getOrderSelectionsEnumValues(Mockito.anyList(), Mockito.anyMap())).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .handleAboutToStart(callbackRequest);
        assertEquals("Collapsable", aboutToStartOrSubmitCallbackResponse.getData().get("sentDocumentPlaceHolder"));
        assertEquals("1", aboutToStartOrSubmitCallbackResponse.getData().get("option1"));
        assertEquals("TestHeader", aboutToStartOrSubmitCallbackResponse.getData().get("serviceOfApplicationHeader"));
    }

    @Ignore
    @Test
    public void testServiceOfApplicationAboutToStartWithEmptyCollection() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .orderCollection(List.of(Element.<OrderDetails>builder()
                                         .value(OrderDetails.builder()
                                                    .otherDetails(OtherOrderDetails.builder().orderCreatedDate("").build())
                                                    .orderType("Test").build())
                                         .build()))
            .build();
        caseData.put("serviceOfApplicationHeader","TestHeader");
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);

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

    @Ignore
    @Test
    public void testHandleAboutToSubmit() throws Exception {
        CaseData cd = CaseData.builder()
            .caseInvites(Collections.emptyList())
            .build();

        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        when(objectMapper.convertValue(cd,  Map.class)).thenReturn(caseData);
        when(serviceOfApplicationService.sendEmail(callbackRequest.getCaseDetails())).thenReturn(cd);
        serviceOfApplicationController.handleSubmitted("test auth",callbackRequest);
        verify(serviceOfApplicationService).sendEmail(callbackRequest.getCaseDetails());

    }
}
