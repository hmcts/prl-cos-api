package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForCourtAdminOrderEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
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

    public static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";
    public static Map<String, Object> clientContext = new HashMap<>();
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    private static final String ENCODEDSTRING = "eyJjbGllbnRfY29udGV4dCI6eyJ1c2VyX3Rhc2siOnsidGFza19kYXRhIjp7ImlkIjoiNmI"
        + "xYzcyOWEtNTYzMC0xMWVmLWEwZDMtZWFmMDM2YWQ5MjBkIiwibmFtZSI6IlJldmlldyBhbmQgQXBwcm92ZSBMZWdhbCBy"
        + "ZXAgT3JkZXIiLCJhc3NpZ25lZSI6ImQ1YjIwOTEzLTc4ZWEtNDZkMi1iNjVjLTVlMTExZDllN2Y4NCIsInR5cGUiOiJyZXZpZXdTb2"
        + "xpY2l0b3JPcmRlclByb3ZpZGVkIiwidGFza19zdGF0ZSI6ImFzc2lnbmVkIiwidGFza19zeXN0ZW0iOiJTRUxGIiwic2VjdXJpdHlfY"
        + "2xhc3NpZmljYXRpb24iOiJQVUJMSUMiLCJ0YXNrX3RpdGxlIjoiUmV2aWV3IGFuZCBBcHByb3ZlIExlZ2FsIHJlcCBPcmRlciAtIFBhcmV"
        + "udGFsIHJlc3BvbnNpYmlsaXR5IG9yZGVyIChDNDVBKSAtIDkgQXVnIDIwMjQsMDk6MTggQU0iLCJjcmVhdGVkX2RhdGUiOiIyMDI0LTA4LT"
        + "A5VDA5OjE5OjAyKzAwMDAiLCJkdWVfZGF0ZSI6IjIwMjQtMDgtMTZUMTc6MDA6MDArMDAwMCIsImxvY2F0aW9uX25hbWUiOiJTd2Fuc2V"
        + "hIiwibG9jYXRpb24iOiIyMzQ5NDYiLCJleGVjdXRpb25fdHlwZSI6IkNhc2UgTWFuYWdlbWVudCBUYXNrIiwianVyaXNkaWN0aW9uIjoiUF"
        + "JJVkFURUxBVyIsInJlZ2lvbiI6IjciLCJjYXNlX3R5cGVfaWQiOiJQUkxBUFBTIiwiY2FzZV9pZCI6IjE3MjI2MTAyNzYwMDE2ODMiLCJjYXN"
        + "lX2NhdGVnb3J5IjoiUHJpdmF0ZSBMYXcgLSBDMTAwIiwiY2FzZV9uYW1lIjoiQzEwMCBXQSBMSU5LSU5HIiwiYXV0b19hc3NpZ25lZCI6ZmFsc2U"
        + "sIndhcm5pbmdzIjpmYWxzZSwid2FybmluZ19saXN0Ijp7InZhbHVlcyI6W119LCJjYXNlX21hbmFnZW1lbnRfY2F0ZWdvcnkiOiJQcml2YXRlIExh"
        + "dyAtIEMxMDAiLCJ3b3JrX3R5cGVfaWQiOiJkZWNpc2lvbl9tYWtpbmdfd29yayIsIndvcmtfdHlwZV9sYWJlbCI6IkRlY2lzaW9uLW1ha2luZyB3b3Jr"
        + "IiwicGVybWlzc2lvbnMiOnsidmFsdWVzIjpbIlJlYWQiLCJPd24iLCJDbGFpbSIsIlVuY2xhaW0iLCJVbmNsYWltQXNzaWduIiwiVW5hc3NpZ25DbGF"
        + "pbSJdfSwiZGVzY3JpcHRpb24iOiJbUmV2aWV3IGFuZCBBcHByb3ZlIExlZ2FsIHJlcCBPcmRlcl0oL2Nhc2VzL2Nhc2UtZGV0YWlscy8ke1tDQVNFX1JFRk"
        + "VSRU5DRV19L3RyaWdnZXIvZWRpdEFuZEFwcHJvdmVBbk9yZGVyL2VkaXRBbmRBcHByb3ZlQW5PcmRlcjEpIiwicm9sZV9jYXRlZ29yeSI6IkpVRElDSUFMIiwi"
        + "YWRkaXRpb25hbF9wcm9wZXJ0aWVzIjp7Im9yZGVySWQiOiIwNDhhNmI3ZS1lMmM1LTRlNmYtOGY4MS1mNDkyNmM1OWJiNzQifSwibWlub3JfcHJpb3JpdHkiOjU"
        + "wMCwibWFqb3JfcHJpb3JpdHkiOjUwMDAsInByaW9yaXR5X2RhdGUiOiIyMDI0LTA4LTE2VDE3OjAwOjAwKzAwMDAifSwiY29tcGxldGVfdGFzayI6dHJ1ZX19fQ==";

    @BeforeEach
    void setUp() {
        clientContext.put("test", "test");
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
    }

    @Test
    void shouldGenerateDraftOrderDropdown() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), AUTH_TOKEN)).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .generateDraftOrderDropDown(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldGenerateDraftOrderDropdownNoDraftOrders() {

        PartyDetails partyDetails = PartyDetails.builder().firstName("xyz")
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .applicants(List.of(applicants))
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)

            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .generateDraftOrderDropDown(AUTH_TOKEN,S2S_TOKEN,"clcx",callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldPopulateJudgeOrAdminDraftOrder() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrder(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldPrepareDraftOrderCollectionWithAdminEditAndApprove() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .prepareDraftOrderCollection(AUTH_TOKEN,S2S_TOKEN,PrlAppsConstants.ENGLISH,callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldPrepareDraftOrderCollectionWithOutAdminEditAndApprove() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("test")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .prepareDraftOrderCollection(AUTH_TOKEN,S2S_TOKEN,PrlAppsConstants.ENGLISH,callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldPopulateJudgeOrAdminDraftOrderCustomFields() throws Exception {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("test")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), AUTH_TOKEN)).thenReturn(caseDataMap);
        when(draftAnOrderService.getDraftOrderInfo("test", caseData, draftOrderElement.getValue())).thenReturn(caseDataMap);
        when(draftAnOrderService
                 .getSelectedDraftOrderDetails(Mockito.any(), Mockito.any(),
                                               Mockito.anyString(),
                                               Mockito.anyString()
                 ))
            .thenReturn(DraftOrder.builder().orderType(
                CreateSelectOrderOptionsEnum.blankOrderOrDirections).build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldPopulateJudgeOrAdminDraftOrderCustomFieldsForEditAndReturnedOrder() throws Exception {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("editReturnedOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldPopulateJudgeOrAdminDraftOrderCustomFieldsThrowsError() throws Exception {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
                .eventId("test")
                .caseDetails(CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
                .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void shouldPopulateJudgeOrAdminDraftOrderCustomFieldsThrowsErrorBlank() throws Exception {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
                .eventId("test")
                .caseDetails(CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
                .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void testNoOrderPopulateJudgeFields() throws Exception {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("test")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }


    @Test
    void  shouldPopulateCommonFields() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("test")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(any(), any(), any(), any())).thenReturn(caseDataMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateCommonFields(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void  shouldPopulateCommonFieldsWithDoYouWantToEditField() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
                .eventId("test")
                .caseDetails(CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(any(), any(), any(), any())).thenReturn(caseDataMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
                .populateCommonFields(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void  shouldPopulateCommonFieldsWhereJusticesLegalAdvisorIsChosen() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("test")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.populateCommonDraftOrderFields(any(), any(), any(), any())).thenReturn(caseDataMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateCommonFields(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void testSaveServeOrderDetails() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void testSaveServeOrderDetailsForEditAndApprove() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString("048a6b7e-e2c5-4e6f-8f81-f4926c59bb74"))
            .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder().dateCreated(LocalDateTime.now()).build()).build())
            .build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);


        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("test")
            .build();
        DynamicMultiSelectList dummyDynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(
                dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dummyDynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesNoNotApplicable.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .ordersHearingDetails(List.of(element(HearingData.builder()
                                                      .hearingDateConfirmOptionEnum(
                                                          HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
                                                      .build())))
            .build();
        List<Element<PartyDetails>> parties = new ArrayList<>();
        parties.add(element(UUID.fromString(TEST_UUID), PartyDetails.builder().build()));
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .applicants(parties)
            .respondents(parties)
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
            .manageOrders(manageOrders)
            .build();

        objectMapper.registerModule(new JavaTimeModule());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        when(manageOrderService.getLoggedInUserType(AUTH_TOKEN)).thenReturn(UserRoles.JUDGE.name());
        caseDataMap.put(DRAFT_ORDER_COLLECTION, List.of(Element.builder().build()));
        when(draftAnOrderService.updateDraftOrderCollection(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("editAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(new HashMap<>())
                             .build())
            .build();
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(AUTH_TOKEN, S2S_TOKEN, ENCODEDSTRING, callbackRequest);
        assertNotNull(response);
    }

    @Test
    void testSaveServeOrderDetailsForEditAndApproveCaseManager() {

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString("048a6b7e-e2c5-4e6f-8f81-f4926c59bb74"))
            .value(DraftOrder.builder().orderTypeId("test")
                       .otherDetails(OtherDraftOrderDetails.builder().dateCreated(LocalDateTime.now()).build()).build()).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);


        Element<HearingData> hearingDataElement = Element.<HearingData>builder().build();

        List<Element<HearingData>> hearingDataCollection = new ArrayList<>();
        hearingDataCollection.add(hearingDataElement);

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("test")
            .build();
        DynamicMultiSelectList dummyDynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(
                dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dummyDynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesNoNotApplicable.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .ordersHearingDetails(List.of(element(HearingData.builder()
                                                      .hearingDateConfirmOptionEnum(
                                                          HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
                                                      .build())))
            .build();
        List<Element<PartyDetails>> parties = new ArrayList<>();
        parties.add(element(UUID.fromString(TEST_UUID), PartyDetails.builder().build()));
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .manageOrders(ManageOrders.builder().solicitorOrdersHearingDetails(hearingDataCollection).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicants(parties)
            .respondents(parties)
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
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(caseData);
        when(manageOrderService.getLoggedInUserType(AUTH_TOKEN)).thenReturn(UserRoles.CASEMANAGER.name());
        stringObjectMap.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("editAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(AUTH_TOKEN, S2S_TOKEN, ENCODEDSTRING, callbackRequest);
        assertNotNull(response);
    }

    @Test
    void testSaveServeOrderDetailsForEditAndReturnedOrder() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString("048a6b7e-e2c5-4e6f-8f81-f4926c59bb74"))
            .value(DraftOrder.builder().orderTypeId("test")
                       .otherDetails(OtherDraftOrderDetails.builder().dateCreated(LocalDateTime.now()).build()).build()).build();
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

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("editReturnedOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(any())).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);
        assertNotNull(response);
    }

    @Test
    void testSaveServeOrderDetailsUpdateDraftOrders() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .saveServeOrderDetails(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);
        assertNotNull(response);

    }

    @Test
    void testPopulateSdoOtherFields() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateSdoOtherFields(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);

    }


    @Test
    void testPopulateSdoOtherFields_scenario2() {

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
            .standardDirectionOrder(StandardDirectionOrder.builder().sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
                                        .editedOrderHasDefaultCaseFields(Yes).build())
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateSdoOtherFields(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);

    }

    @Test
    void testPopulateSdoOtherFieldsStandingOrderSelectFalse() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateSdoOtherFields(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(response);

    }

    @Test
    void testSendEmailNotificationToRecipientsServeOrder() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes)
                              .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
                              .build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .draftOrderCollection(draftOrderCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());


        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(AUTH_TOKEN,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), stringObjectMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                .id(123L)
                .data(stringObjectMap)
                .build())
            .build();

        editAndApproveDraftOrderController.sendEmailNotificationToRecipientsServeOrder(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed("Bearer TestAuthToken", caseData, stringObjectMap);
    }

    @Test
    void testEditAndServeOrderMidEvent() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(manageOrderService.serveOrderMidEvent(callbackRequest)).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .editAndServeOrderMidEvent(authorisation, S2S_TOKEN, callbackRequest);
        assertNotNull(response);
    }

    @Test
    void testExceptionForGenerateDraftOrderDropDown() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .generateDraftOrderDropDown(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPopulateJudgeOrAdminDraftOrder() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrder(AUTH_TOKEN,S2S_TOKEN,"clientContext", callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPrepareDraftOrderCollection() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .prepareDraftOrderCollection(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH,callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForSaveServeOrderDetails() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .saveServeOrderDetails(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForSendEmailNotificationToRecipientsServeOrder() {

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

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .sendEmailNotificationToRecipientsServeOrder(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testHandleEditAndApproveSubmitted() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("whatToDoWithOrderSolicitor", OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
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
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(AUTH_TOKEN,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> callbackResponse = editAndApproveDraftOrderController
            .handleEditAndApproveSubmitted(AUTH_TOKEN,S2S_TOKEN,"clcx",callbackRequest);
        assertNotNull(Objects.requireNonNull(callbackResponse.getBody()).getConfirmationHeader());
    }

    @Test
    void testHandleEditAndApproveSubmittedByCourtAdmin() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .handleEditAndApproveSubmitted(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testHandleEditAndApproveSubmittedWhenAskLegalRepChosen() {
        Map<String, Object> stringObjectMap = new HashMap<>();

        stringObjectMap.put("whatToDoWithOrderSolicitor", OrderApprovalDecisionsForSolicitorOrderEnum.askLegalRepToMakeChanges.toString());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
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
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any(),
                                                              Mockito.anyString(),
                                                              Mockito.anyString()
        )).thenReturn(DraftOrder.builder().build());
        CaseData caseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .manageOrders(ManageOrders.builder()
                              .whatToDoWithOrderSolicitor(OrderApprovalDecisionsForSolicitorOrderEnum.askLegalRepToMakeChanges)
                              .build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(AUTH_TOKEN,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> callbackResponse = editAndApproveDraftOrderController
            .handleEditAndApproveSubmitted(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);
        assertNotNull(Objects.requireNonNull(callbackResponse.getBody()).getConfirmationHeader());
    }



    @Test
    void testExceptionForPopulateJudgeOrAdminDraftOrderCustomFields() {
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN, S2S_TOKEN,"clcx",  callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPopulateCommonFields() {
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> editAndApproveDraftOrderController
            .populateCommonFields(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForEditAndServeOrderMidEvent() {
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            editAndApproveDraftOrderController.editAndServeOrderMidEvent(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testNoFieldsPopulateUploadOrder() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .isOrderUploadedByJudgeOrAdmin(Yes)
            .build();
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(Collections.singletonList(element(draftOrder)))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any(),
                                                              Mockito.anyString(),
                                                              Mockito.anyString()
        )).thenReturn(draftOrder);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);

        assertNotNull(response);
        Map<String, Object> updatedCaseDataMap = response.getData();
        assertNotNull(updatedCaseDataMap.get("draftOrderCollection"));
        assertEquals("C100", updatedCaseDataMap.get("caseTypeOfApplication"));
    }

    @Test
    void testSkipUploadConditionWhenDraftOrderIsNull() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .isOrderUploadedByJudgeOrAdmin(Yes)
            .build();
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(Collections.singletonList(element(draftOrder)))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any(),
                                                              Mockito.anyString(),
                                                              Mockito.anyString()
        )).thenReturn(null);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateJudgeOrAdminDraftOrderCustomFields(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest);

        assertNotNull(response);
    }

    @Test
    void testPopulateCommonFieldsEditedOrder() {
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any(),
                                                              Mockito.anyString(),
                                                              Mockito.anyString()
        )).thenReturn(draftOrder);
        when(draftAnOrderService.populateCommonDraftOrderFields(Mockito.any(), Mockito.any(), Mockito.any(), any())).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse response = editAndApproveDraftOrderController
            .populateCommonFields(AUTH_TOKEN,S2S_TOKEN,"clcx",callbackRequest);

        assertNotNull(response);
        Map<String, Object> updatedCaseDataMap = response.getData();
        assertNotNull(updatedCaseDataMap.get("doYouWantToEditTheOrder"));
        assertEquals("Yes", String.valueOf(updatedCaseDataMap.get("doYouWantToEditTheOrder")));
    }
}
