package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForCourtAdminOrderEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class EditAndApproveDraftOrderControllerTest {

    @Mock
    private  ObjectMapper objectMapper;
    @Mock
    private  DraftAnOrderService draftAnOrderService;

    @Mock
    private EditReturnedOrderService editReturnedOrderService;

    @Mock
    private HearingDataService hearingDataService;

    @Mock
    private HearingService hearingService;

    @Mock
    private ManageOrderService manageOrderService;

    @Mock
    private ManageOrderEmailService manageOrderEmailService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @InjectMocks
    private EditAndApproveDraftOrderController editAndApproveDraftOrderController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    AllTabServiceImpl allTabService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";
    public static Map<String, Object> clientContext = new HashMap<>();

    @Before
    public void setUp() {
        clientContext.put("test", "test");
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        when(hearingDataService.populateHearingDynamicLists(Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any()))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());

        when(hearingDataService.getHearingDataForOtherOrders(Mockito.any(),Mockito.any(),Mockito.any()))
            .thenReturn(List.of(Element.<HearingData>builder().build()));
        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(DraftOrder.builder().build());
    }

    @Test
    public void shouldGenerateDraftOrderDropdown() {
        PartyDetails partyDetails = PartyDetails.builder().firstName("xyz")
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicants(List.of(applicants))
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .generateDraftOrderDropDown(authToken,s2sToken, callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldGenerateDraftOrderDropdownNoDraftOrders() {

        PartyDetails partyDetails = PartyDetails.builder().firstName("xyz")
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .applicants(List.of(applicants))
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)

            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .generateDraftOrderDropDown(authToken,s2sToken,callbackRequest);
        Assert.assertNotNull(response);
    }

    @Ignore
    @Test
    public void shouldPopulateJudgeOrAdminDraftOrder() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrder(authToken,s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldPrepareDraftOrderCollectionWithAdminEditAndApprove() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes)
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        when(dynamicMultiSelectListService
                 .getOrdersAsDynamicMultiSelectList(caseData))
            .thenReturn(DynamicMultiSelectList.builder().build());

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .prepareDraftOrderCollection(authToken,s2sToken,callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldPrepareDraftOrderCollectionWithOutAdminEditAndApprove() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.saveAsDraft)
                                .doYouWantToServeOrder(No).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("test")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData,Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .prepareDraftOrderCollection(authToken,s2sToken,callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldPopulateJudgeOrAdminDraftOrderCustomFields() throws Exception {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("test")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(draftAnOrderService
                 .getSelectedDraftOrderDetails(Mockito.any(), Mockito.any()))
            .thenReturn(DraftOrder.builder().orderType(
                CreateSelectOrderOptionsEnum.blankOrderOrDirections).build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(authToken,s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldPopulateJudgeOrAdminDraftOrderCustomFieldsForEditAndReturnedOrder() throws Exception {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .manageOrders(ManageOrders.builder().orderUploadedAsDraftFlag(Yes).build())
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("editReturnedOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_RETURNED_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(draftAnOrderService
                 .getSelectedDraftOrderDetails(Mockito.any(), Mockito.any()))
            .thenReturn(DraftOrder.builder().orderType(
                CreateSelectOrderOptionsEnum.blankOrderOrDirections).build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(authToken,s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldPopulateJudgeOrAdminDraftOrderCustomFieldsThrowsError() throws Exception {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
                .welshLanguageRequirement(Yes)
                .welshLanguageRequirementApplication(english)
                .languageRequirementApplicationNeedWelsh(Yes)
                .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesLegalAdviser).build())
                .draftOrderDoc(Document.builder()
                        .documentUrl(generatedDocumentInfo.getUrl())
                        .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                        .documentHash(generatedDocumentInfo.getHashToken())
                        .documentFileName("c100DraftFilename.pdf")
                        .build())
                .id(123L)
                .draftOrderDocWelsh(Document.builder()
                        .documentUrl(generatedDocumentInfo.getUrl())
                        .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                        .documentHash(generatedDocumentInfo.getHashToken())
                        .documentFileName("c100DraftWelshFilename")
                        .build())
                .draftOrderCollection(draftOrderCollection)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
                draftOrderCollection,
                null,
                DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .eventId("test")
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData,Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(draftAnOrderService
                .getSelectedDraftOrderDetails(Mockito.any(), Mockito.any()))
                .thenReturn(DraftOrder.builder().orderType(
                        CreateSelectOrderOptionsEnum.blankOrderOrDirections).build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
                .populateJudgeOrAdminDraftOrderCustomFields(authToken,s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldPopulateJudgeOrAdminDraftOrderCustomFieldsThrowsErrorBlank() throws Exception {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
                .welshLanguageRequirement(Yes)
                .justiceLegalAdviserFullName(" ")
                .welshLanguageRequirementApplication(english)
                .languageRequirementApplicationNeedWelsh(Yes)
                .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesLegalAdviser).build())
                .draftOrderDoc(Document.builder()
                        .documentUrl(generatedDocumentInfo.getUrl())
                        .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                        .documentHash(generatedDocumentInfo.getHashToken())
                        .documentFileName("c100DraftFilename.pdf")
                        .build())
                .id(123L)
                .draftOrderDocWelsh(Document.builder()
                        .documentUrl(generatedDocumentInfo.getUrl())
                        .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                        .documentHash(generatedDocumentInfo.getHashToken())
                        .documentFileName("c100DraftWelshFilename")
                        .build())
                .draftOrderCollection(draftOrderCollection)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
                draftOrderCollection,
                null,
                DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .eventId("test")
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData,Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(draftAnOrderService
                .getSelectedDraftOrderDetails(Mockito.any(), Mockito.any()))
                .thenReturn(DraftOrder.builder().orderType(
                        CreateSelectOrderOptionsEnum.blankOrderOrDirections).build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
                .populateJudgeOrAdminDraftOrderCustomFields(authToken,s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testNoOrderPopulateJudgeFields() throws Exception {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("test")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(authToken,s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }


    @Test
    public void  shouldPopulateCommonFields() {
        PartyDetails partyDetails = PartyDetails.builder().firstName("xyz")
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        final String authorisation = "Bearer someAuthorisationToken";
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .applicants(List.of(applicants))
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .manageOrders(ManageOrders.builder()
                              .solicitorOrdersHearingDetails(hearingDataCollection)
                              .build())
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("test")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateCommonFields(authToken, s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void  shouldPopulateCommonFieldsWithDoYouWantToEditField() {
        final String authorisation = "Bearer someAuthorisationToken";
        PartyDetails partyDetails = PartyDetails.builder().firstName("xyz")
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);
        CaseData caseData = CaseData.builder()
                .welshLanguageRequirement(Yes)
                .welshLanguageRequirementApplication(english)
                .applicants(List.of(applicants))
                .languageRequirementApplicationNeedWelsh(Yes)
                .manageOrders(ManageOrders.builder()
                        .solicitorOrdersHearingDetails(hearingDataCollection)
                        .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesLegalAdviser)
                        .build())
                .draftOrderDoc(Document.builder()
                        .documentUrl(generatedDocumentInfo.getUrl())
                        .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                        .documentHash(generatedDocumentInfo.getHashToken())
                        .documentFileName("c100DraftFilename.pdf")
                        .build())
                .id(123L)
                .draftOrderDocWelsh(Document.builder()
                        .documentUrl(generatedDocumentInfo.getUrl())
                        .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                        .documentHash(generatedDocumentInfo.getHashToken())
                        .documentFileName("c100DraftWelshFilename")
                        .build())
                .draftOrderCollection(draftOrderCollection)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
                draftOrderCollection,
                null,
                DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .eventId("test")
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
                .populateCommonFields(authToken, s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void  shouldPopulateCommonFieldsWhereJusticesLegalAdvisorIsChosen() {
        PartyDetails partyDetails = PartyDetails.builder().firstName("xyz")
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        final String authorisation = "Bearer someAuthorisationToken";
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .applicants(List.of(applicants))
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .manageOrders(ManageOrders.builder()
                              .solicitorOrdersHearingDetails(hearingDataCollection)
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesLegalAdviser)
                              .build())
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("test")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateCommonFields(authToken, s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testSaveServeOrderDetails() {

        final String authorisation = "Bearer someAuthorisationToken";

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);


        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(authToken, s2sToken, "clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testSaveServeOrderDetailsForEditAndApprove() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);


        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("editAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(authToken, s2sToken, "clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testSaveServeOrderDetailsForEditAndReturnedOrder() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);


        Element<HearingData> hearingDataElement = Element.<HearingData>builder()
            .value(HearingData.builder()
                       .hearingJudgeNameAndEmail(
                           JudicialUser.builder().build()
                       )
                       .build()).build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("editReturnedOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(authToken, s2sToken, "clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testSaveServeOrderDetailsUpdateDraftOrders() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.saveAsDraft)
                                .doYouWantToServeOrder(No).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .manageOrders(ManageOrders.builder()
                              .solicitorOrdersHearingDetails(hearingDataCollection)
                              .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(authToken, s2sToken, "clcx", callbackRequest);
        Assert.assertNotNull(response);

    }

    @Test
    public void testPopulateSdoOtherFields() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateSdoOtherFields(authToken, s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);

    }


    @Test
    public void testPopulateSdoOtherFields_scenario2() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .standardDirectionOrder(StandardDirectionOrder.builder().sdoPreamblesList(Arrays.asList(SdoPreamblesEnum.rightToAskCourt))
                                        .editedOrderHasDefaultCaseFields(Yes).build())
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateSdoOtherFields(authToken, s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);

    }

    @Test
    public void testPopulateSdoOtherFieldsStandingOrderSelecFalse() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoPreamblesList(new ArrayList<>())
            .sdoHearingsAndNextStepsList(new ArrayList<>())
            .sdoCafcassOrCymruList(new ArrayList<>())
            .sdoLocalAuthorityList(new ArrayList<>())
            .sdoCourtList(new ArrayList<>())
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoOtherList(new ArrayList<>())
            .sdoFurtherList(new ArrayList<>())
            .build();
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(standardDirectionOrder)
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateSdoOtherFields(authToken, s2sToken,"clcx", callbackRequest);
        Assert.assertNotNull(response);

    }

    @Test
    public void testSendEmailNotificationToRecipientsServeOrder() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());


        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), stringObjectMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(123L)
                .data(stringObjectMap)
                .build())
            .build();

        editAndApproveDraftOrderController.sendEmailNotificationToRecipientsServeOrder(authToken, s2sToken, callbackRequest);
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed("Bearer TestAuthToken", caseData, stringObjectMap);
    }

    @Test
    public void testEditAndServeOrderMidEvent() {
        final String authorisation = "Bearer someAuthorisationToken";

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoPreamblesList(new ArrayList<>())
            .sdoHearingsAndNextStepsList(new ArrayList<>())
            .sdoCafcassOrCymruList(new ArrayList<>())
            .sdoLocalAuthorityList(new ArrayList<>())
            .sdoCourtList(new ArrayList<>())
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoOtherList(new ArrayList<>())
            .sdoFurtherList(new ArrayList<>())
            .build();
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(standardDirectionOrder)
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.serveOrderMidEvent(callbackRequest)).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .editAndServeOrderMidEvent(authorisation, s2sToken, callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testExceptionForGenerateDraftOrderDropDown() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .generateDraftOrderDropDown(authToken, s2sToken, callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateJudgeOrAdminDraftOrder() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrder(authToken,s2sToken,"clientContext", callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPrepareDraftOrderCollection() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .prepareDraftOrderCollection(authToken, s2sToken, callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForSaveServeOrderDetails() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(
            () -> editAndApproveDraftOrderController
                .saveServeOrderDetails(authToken, s2sToken, "clcx", callbackRequest),
            RuntimeException.class,
            "Invalid Client"
        );
    }

    @Test
    public void testExceptionForSendEmailNotificationToRecipientsServeOrder() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .sendEmailNotificationToRecipientsServeOrder(authToken, s2sToken, callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testHandleEditAndApproveSubmitted() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("whatToDoWithOrderSolicitor", OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> callbackResponse = editAndApproveDraftOrderController
            .handleEditAndApproveSubmitted(authToken,s2sToken,"clcx",callbackRequest);
        assertNotNull(Objects.requireNonNull(callbackResponse.getBody()).getConfirmationHeader());
    }

    @Test
    public void testHandleEditAndApproveSubmittedByCourtAdmin() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .handleEditAndApproveSubmitted(authToken, s2sToken, "clcx", callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testHandleEditAndApproveSubmittedWhenAskLegalRepChosen() {
        Map<String, Object> stringObjectMap = new HashMap<>();

        stringObjectMap.put("whatToDoWithOrderSolicitor", OrderApprovalDecisionsForSolicitorOrderEnum.askLegalRepToMakeChanges.toString());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(CaseData.builder().build());
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(CaseData.builder().build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(DraftOrder.builder().build());
        CaseData caseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> callbackResponse = editAndApproveDraftOrderController
            .handleEditAndApproveSubmitted(authToken,s2sToken,"clcx", callbackRequest);
        assertNotNull(Objects.requireNonNull(callbackResponse.getBody()).getConfirmationHeader());
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testExceptionForPopulateJudgeOrAdminDraftOrderCustomFields() {
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(authToken, s2sToken,"clcx",  callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateCommonFields() {
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .populateCommonFields(authToken, s2sToken,"clcx", callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForEditAndServeOrderMidEvent() {
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .editAndServeOrderMidEvent(authToken, s2sToken, callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateSdoOtherFields() {
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> editAndApproveDraftOrderController
            .editAndServeOrderMidEvent(authToken, s2sToken, callbackRequest), RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testNoFieldsPopulateUploadOrder() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .isOrderUploadedByJudgeOrAdmin(Yes)
            .build();
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(Collections.singletonList(element(draftOrder)))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(authToken,s2sToken,"clcx", callbackRequest);

        Assert.assertNotNull(response);
        Map<String, Object> updatedCaseDataMap = response.getData();
        Assert.assertNotNull(updatedCaseDataMap.get("draftOrderCollection"));
        Assert.assertEquals("C100", updatedCaseDataMap.get("caseTypeOfApplication"));
    }

    @Test
    public void testSkipUploadConditionWhenDraftOrderIsNull() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .isOrderUploadedByJudgeOrAdmin(Yes)
            .build();
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(Collections.singletonList(element(draftOrder)))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(null);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(authToken,s2sToken,"clcx", callbackRequest);

        Assert.assertNotNull(response);
    }

    @Test
    public void testPopulateCommonFieldsEditedOrder() {
        PartyDetails partyDetails = PartyDetails.builder().firstName("xyz")
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        DraftOrder draftOrder = DraftOrder.builder()
            .isOrderUploadedByJudgeOrAdmin(Yes)
            .build();
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(Collections.singletonList(element(draftOrder)))
            .applicants(List.of(applicants))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .doYouWantToEditTheOrder(Yes)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);
        when(draftAnOrderService.populateCommonDraftOrderFields(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateCommonFields(authToken,s2sToken,"clcx",callbackRequest);

        Assert.assertNotNull(response);
        Map<String, Object> updatedCaseDataMap = response.getData();
        Assert.assertNotNull(updatedCaseDataMap.get("doYouWantToEditTheOrder"));
        Assert.assertEquals("Yes", String.valueOf(updatedCaseDataMap.get("doYouWantToEditTheOrder")));
    }
}
