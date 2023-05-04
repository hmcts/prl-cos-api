package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.servedSavedOrders;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class EditAndApproveDraftOrderControllerTest {

    @Mock
    private  ObjectMapper objectMapper;
    @Mock
    private  DraftAnOrderService draftAnOrderService;
    @Mock
    private ManageOrderService manageOrderService;
    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;
    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;
    @Mock
    private ManageOrderEmailService manageOrderEmailService;

    @Mock
    HearingDataService hearingDataService;

    @Mock
    private Time dateTime;

    @InjectMocks
    private EditAndApproveDraftOrderController editAndApproveDraftOrderController;

    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    private LocalDateTime now;
    private DynamicMultiSelectList dynamicMultiSelectList;
    private DynamicList dynamicList;
    private UUID uuid;

    @Before
    public void setUp() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        now = dateTime.now();
        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label(" ").build();
        dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID + "-" + now)
            .label("test")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(
            Mockito.any(CaseData.class),
            Mockito.anyString()
        ))
            .thenReturn(dynamicMultiSelectList);
        when(dynamicMultiSelectListService
                 .getStringFromDynamicMultiSelectList(Mockito.any(DynamicMultiSelectList.class)))
            .thenReturn("testChild");

    }

    @Test
    public void shouldGenerateDraftOrderDropdown() {

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


        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController.generateDraftOrderDropDown(callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldGenerateDraftOrderDropdownNoDraftOrders() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
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


        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController.generateDraftOrderDropDown(callbackRequest);
        Assert.assertNotNull(response);
    }

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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController.populateJudgeOrAdminDraftOrder(callbackRequest);
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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData)).thenReturn(caseDataMap);
        when(dynamicMultiSelectListService
                 .getOrdersAsDynamicMultiSelectList(caseData, servedSavedOrders.getDisplayedValue()))
            .thenReturn(DynamicMultiSelectList.builder().build());

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .prepareDraftOrderCollection("test",callbackRequest);
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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .prepareDraftOrderCollection("test",callbackRequest);
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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData)).thenReturn(caseDataMap);
        when(draftAnOrderService
                 .getSelectedDraftOrderDetails(caseData))
            .thenReturn(DraftOrder.builder().orderType(
                CreateSelectOrderOptionsEnum.blankOrderOrDirections).build());

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields("test",callbackRequest);
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
        when(draftAnOrderService.getDraftOrderDynamicList(caseData)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData)).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields("test",callbackRequest);
        Assert.assertNotNull(response);
    }


    @Test
    public void  shouldPopulateCommonFields() throws Exception {
        final String authorisation = "Bearer someAuthorisationToken";
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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData)).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateCommonFields(authorisation, callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testSaveServeOrderDetails() {

        final String authorisation = "Bearer someAuthorisationToken";

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> ordersHearingDetails = Collections.singletonList(childElement);

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
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.finalizeSaveToServeLater)
                                .doYouWantToServeOrder(Yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .manageOrders(ManageOrders.builder()
                              .solicitorOrdersHearingDetails(ordersHearingDetails)
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

        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("test")
            .label("test")
            .build();

        DynamicList dynamicList3 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(dynamicListElement);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElements)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList1)
                .hearingListedLinkedCases(dynamicList1)
                .retrievedHearingDates(dynamicList1)
                .retrievedHearingChannels(dynamicList3)
                .retrievedCourtLocations(dynamicList1)
                .retrievedVideoSubChannels(dynamicList1)
                .retrievedTelephoneSubChannels(dynamicList1)
                .retrievedCourtLocations(dynamicList1)
                .hearingListedLinkedCases(dynamicList1)
                .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(hearingDataService.populateHearingDynamicLists(authorisation,
                                                            callbackRequest.getCaseDetails().getId().toString(), caseData))
            .thenReturn(hearingDataPrePopulatedDynamicLists);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(authorisation, callbackRequest);
        Assert.assertNotNull(response);

    }

    @Test
    public void testSaveServeOrderDetailsUpdateDraftOrders() {

        final String authorisation = "Bearer someAuthorisationToken";

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> ordersHearingDetails = Collections.singletonList(childElement);

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
            .manageOrders(ManageOrders.builder()
                              .solicitorOrdersHearingDetails(ordersHearingDetails)
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .whatDoWithOrder(WhatToDoWithOrderEnum.saveAsDraft)
                                .doYouWantToServeOrder(No).build())
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

        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("test")
            .label("test")
            .build();

        DynamicList dynamicList3 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(dynamicListElement);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElements)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList1)
                .hearingListedLinkedCases(dynamicList1)
                .retrievedHearingDates(dynamicList1)
                .retrievedHearingChannels(dynamicList3)
                .retrievedCourtLocations(dynamicList1)
                .retrievedVideoSubChannels(dynamicList1)
                .retrievedTelephoneSubChannels(dynamicList1)
                .retrievedCourtLocations(dynamicList1)
                .hearingListedLinkedCases(dynamicList1)
                .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(hearingDataService.populateHearingDynamicLists(authorisation,
                                                            callbackRequest.getCaseDetails().getId().toString(), caseData))
            .thenReturn(hearingDataPrePopulatedDynamicLists);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(authorisation, callbackRequest);
        Assert.assertNotNull(response);

    }


    @Test
    public void testPopulateSdoFields() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.miamAttendance))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .sdoCourtList(List.of(SdoCourtEnum.crossExaminationEx740))
            .sdoDocumentationAndEvidenceList(List.of(SdoDocumentationAndEvidenceEnum.medicalDisclosure))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoFurtherList(List.of(SdoFurtherInstructionsEnum.newDirection))
            .build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .standardDirectionOrder(standardDirectionOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        Assert.assertNotNull(editAndApproveDraftOrderController.populateSdoOtherFields("test token", callbackRequest).getData());

    }

    @Test
    public void testPopulateSdoFieldsWithNoOptionSelected() throws Exception {
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
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .standardDirectionOrder(standardDirectionOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        Assert.assertEquals(
            "Please select at least one options from below",
            editAndApproveDraftOrderController.populateSdoOtherFields("test token", callbackRequest).getErrors().get(0)
        );

    }

    @Test
    public void testSubmitEmailValidation() throws Exception {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(YesOrNo.Yes).build())
            .courtName("testcourt")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController.sendEmailNotificationToRecipientsServeOrder(
            "Test authtoken",
            callbackRequest
        );
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed(callbackRequest.getCaseDetails());
    }

    @Test
    public void testCheckOnlyC47aOrderSelectedToServeForC47A() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("testC47A")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .emailInformationCA(List.of(Element.<uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
            .childOption(
                DynamicMultiSelectList.builder()
                    .value(List.of(DynamicMultiselectListElement.builder().label("John (Child 1)").build())).build()
            )
            .build();

        Element<uk.gov.hmcts.reform.prl.models.OrderDetails> orders = Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder().id(uuid).value(
            uk.gov.hmcts.reform.prl.models.OrderDetails
                .builder()
                .orderDocument(Document
                                   .builder()
                                   .build())
                .dateCreated(now)
                .orderTypeId(TEST_UUID)
                .otherDetails(
                    uk.gov.hmcts.reform.prl.models.OtherOrderDetails.builder().build())
                .build()).build();
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = manageOrderService.checkOnlyC47aOrderSelectedToServe(callbackRequest);
        Assert.assertTrue(stringObjectMap.isEmpty());
    }

}
