package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DraftAnOrderControllerTest {

    @InjectMocks
    private DraftAnOrderController draftAnOrderController;

    @Mock
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private ObjectMapper objectMapper;

    private CaseData caseData1;
    private PartyDetails partyDetails;
    private Map<String, Object> caseData;
    private CallbackRequest callbackRequest;

    @Before
    public void preRequisites() throws Exception {
        partyDetails = PartyDetails.builder().address(Address.builder().build())
            .dateOfBirth(LocalDate.now()).build();

        caseData1 = CaseData.builder().id(123456L)
            .courtName("")
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .build();

        caseData = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(draftAnOrderService.getTheOrderDraftString(Mockito.any(CaseData.class))).thenReturn("test");
        when(draftAnOrderService.generateDraftOrderCollection(Mockito.any(CaseData.class))).thenReturn(caseData);
        when(draftAnOrderService.generateSolicitorDraftOrder(Mockito.anyString(),
                                                             Mockito.any(CaseData.class))).thenReturn(
            Document.builder().build());
    }

    @Test
    public void testPopulateSelectedOrder() throws Exception {
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = draftAnOrderController
            .populateSelectedOrder("", callbackRequest);
        assertEquals("Non-molestation order (FL404A)",
                     aboutToStartOrSubmitCallbackResponse.getData().get("selectedOrderLabel"));
    }

    @Test
    public void testResetFields() {
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = draftAnOrderController.resetFields(
            "",
            callbackRequest
        );
        assertTrue(aboutToStartOrSubmitCallbackResponse.getData().isEmpty());
    }

    @Test
    public void testPrePopulateFields() {
        caseData.put("applicantCaseName", "Hello");
        CallbackRequest callbackRequest1 = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = draftAnOrderController
            .prePopulateFields("", callbackRequest1);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testPrepopulateSolicitorDraftAnOrder() throws Exception {
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = draftAnOrderController
            .prepopulateSolicitorDraftAnOrder("", callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("previewDraftAnOrder"));
    }

    @Test
    public void testPrepareDraftOrderCollection() throws Exception {
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = draftAnOrderController
            .prepareDraftOrderCollection("", callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testDraftAnOrderMidEventCallback() throws Exception {
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = draftAnOrderController
            .draftAnOrderMidEventCallback("", callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("solicitorOrJudgeDraftOrderDoc"));
    }
}
