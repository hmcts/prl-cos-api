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
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EditAndApproveDraftOrderControllerTest {
    @InjectMocks
    private EditAndApproveDraftOrderController editAndApproveDraftOrderController;

    @Mock
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private ObjectMapper objectMapper;

    private CaseData caseData1;
    private PartyDetails partyDetails;
    private Map<String, Object> caseData;
    private CallbackRequest callbackRequest;
    private List<Element<DraftOrder>> draftOrderCollection;
    private DynamicList dynamicList;
    private Address address;
    private Document document;

    @Before
    public void preRequisites() throws Exception {

        document = Document.builder()
            .documentFileName("test")
            .build();

        partyDetails = PartyDetails.builder().address(Address.builder().build())
            .dateOfBirth(LocalDate.now()).build();
        draftOrderCollection = List.of(element(DraftOrder.builder()
                                                   .orderTypeId("")
                                                   .orderDocument(document)
                                                   .build()));
        dynamicList = ElementUtils.asDynamicList(draftOrderCollection,
                                                 null, DraftOrder::getLabelForOrdersDynamicList
        );
        address = Address.builder().build();
        Element<DraftOrder> draftOrderElement = element(DraftOrder.builder()
                                                            .orderText("test")
                                                            .adminNotes("adminNotes")
                                                            .judgeNotes("judgeNotes")
                                                            .orderDocument(document)
                                                            .otherDetails(OtherDraftOrderDetails.builder()
                                                                              .dateCreated(LocalDateTime.now())
                                                                              .build())
                                                            .build());

        caseData1 = CaseData.builder().id(123456L)
            .courtName("")
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .draftOrderCollection(List.of(draftOrderElement))
            .build();

        caseData = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(draftAnOrderService.getTheOrderDraftString(Mockito.any(CaseData.class))).thenReturn("test");
        when(draftAnOrderService.generateDraftOrderCollection(Mockito.any(CaseData.class))).thenReturn(caseData);
        when(draftAnOrderService.generateSolicitorDraftOrder(
            Mockito.anyString(),
            Mockito.any(CaseData.class)
        )).thenReturn(
            Document.builder().build());
    }

    @Test
    public void testDraftOrderDropDownWhenOrdersArePresent() {

        when(draftAnOrderService.getDraftOrderDynamicList((Mockito.any(List.class)))).thenReturn(Map.of(
            "draftOrdersDynamicList",
            dynamicList
        ));
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = editAndApproveDraftOrderController
            .generateDraftOrderDropDown("Bearer Test", callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("draftOrdersDynamicList"));
    }


    @Test
    public void testPopulateDraftOrderDetails() {

        when(draftAnOrderService.populateSelectedOrder((Mockito.any(CaseData.class)))).thenReturn(Map.of(
            "previewDraftOrder",
            document
        ));
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = editAndApproveDraftOrderController
            .populateDraftOrderDetails("Bearer Test", callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("previewDraftOrder"));
    }

    @Test
    public void testAboutToSubmitPrepareDraftOrderCollection() {
        when(draftAnOrderService.updateDraftOrderCollection((Mockito.any(CaseData.class)))).thenReturn(Map.of(
            "draftOrderCollection",
            caseData1.getDraftOrderCollection()
        ));
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = editAndApproveDraftOrderController
            .prepareDraftOrderCollection("Bearer Test", callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("draftOrderCollection"));
    }

}
