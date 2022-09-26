package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DraftAnOrderServiceTest {


    @InjectMocks
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private Time dateTime;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ElementUtils elementUtils;

    @Mock
    private CaseInviteManager caseInviteManager;

    private static CaseData caseData;

    private List<Element<OrderDetails>> draftOrderCollection;
    private DynamicList dynamicList;
    private Address address;

    @Before
    public void testDataToUse() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        draftOrderCollection = List.of(element(OrderDetails.builder().orderType("abcd").orderTypeId("1234").orderDocument(
            Document.builder().build()).build()));
        dynamicList =  ElementUtils.asDynamicList(draftOrderCollection, null, OrderDetails::getLabelForOrdersDynamicList);
        address = Address.builder().build();
        caseData = CaseData.builder()
            .replyMessageDynamicList(dynamicList)
            .draftOrdersDynamicList(dynamicList)
            .draftOrderWithTextCollection(List.of(element(DraftOrderDetails.builder().orderTypeId("123").orderDocument(
                Document.builder().build()).build())))
            .dateOrderMade(LocalDate.now())
            .draftOrderCollection(List.of(element(OrderDetails.builder().dateCreated(LocalDateTime.now()).build())))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(UUID.fromString("0f14d0ab-9605-4a62-a9e4-5ed26688389b"));
        UUID selectedValue = UUID.randomUUID();
        when(dateTime.now()).thenReturn(LocalDateTime.now());

        when(dgsService.generateDocument(Mockito.anyString(),Mockito.any(CaseDetails.class),Mockito.any())).thenReturn(generatedDocumentInfo);
    }

    @Test
    public void testPopulateSelectedOrder() {
        Map<String, Object> caseDataMap = draftAnOrderService.populateSelectedOrder(caseData);
        assertThrows(UnsupportedOperationException.class, () -> caseDataMap.get("previewDraftOrder"));
    }

    @Test
    public void testGetDraftOrderDynamicList() {
        Map<String, Object> caseDataMap = draftAnOrderService.getDraftOrderDynamicList(draftOrderCollection);
        assertNotNull(caseDataMap.get("draftOrdersDynamicList"));
    }

    @Test
    public void testGenerateDraftOrderCollection() {
        Map<String, Object> caseDataMap = draftAnOrderService.generateDraftOrderCollection(caseData);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
        assertNotNull(caseDataMap.get("draftOrderWithTextCollection"));
    }

    @Test
    public void testGenerateDraftOrderCollectionElseCondition() {
        caseData = caseData.toBuilder().draftOrderCollection(null).build();
        Map<String, Object> caseDataMap = draftAnOrderService.generateDraftOrderCollection(caseData);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
        assertNotNull(caseDataMap.get("draftOrderWithTextCollection"));
    }

    @Test
    public void testgetTheOrderDraftString() {
        assertNull(draftAnOrderService.getTheOrderDraftString(caseData));
    }

    @Test
    public void testgetTheOrderDraftStringNonMolestationOrder() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .manageOrders(ManageOrders.builder()
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .build();
        assertNotNull(draftAnOrderService.getTheOrderDraftString(caseData));
    }

    @Test
    public void testgetTheOrderDraftStringNonMolestationOrderWithcustomFields() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .dateOrderMade(null)
            .recitalsOrPreamble("")
            .respondentsFL401(PartyDetails.builder().address(address).build())
            .applicantChildDetails(List.of(element(ApplicantChild.builder()
                                                       .fullName("")
                                                       .dateOfBirth(LocalDate.now())
                                                       .build())))
            .manageOrders(ManageOrders.builder()
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .recitalsOrPreamble("")
                              .isTheOrderByConsent(YesOrNo.Yes)
                              .furtherDirectionsIfRequired("")
                              .fl404CustomFields(FL404.builder()
                                                     .fl404bApplicantName("")
                                                     .fl404bCourtName("")
                                                     .fl404bApplicantReference("")
                                                     .fl404bRespondentDob(LocalDate.now())
                                                     .fl404bRespondentName("")
                                                     .fl404bRespondentNotToThreat(List.of(""))
                                                     .fl404bRespondentNotIntimidate(List.of(""))
                                                     .fl404bRespondentNotToTelephone(List.of(""))
                                                     .fl404bRespondentNotToDamageOrThreat(List.of(""))
                                                     .fl404bRespondentNotToDamage(List.of(""))
                                                     .fl404bRespondentNotToEnterProperty(List.of(""))
                                                     .fl404bRespondentNotToThreatChild(List.of(""))
                                                     .fl404bRespondentNotHarassOrIntimidate(List.of(""))
                                                     .fl404bRespondentNotToTelephoneChild(List.of(""))
                                                     .fl404bRespondentNotToEnterSchool(List.of(""))
                                                     .fl404bMentionedProperty("Yes")
                                                     .fl404bAddressOfProperty("Yes")
                                                     .fl404bAddMoreDetailsTelephone("")
                                                     .fl404bDateOrderEnd("")
                                                     .fl404bDateOrderEndTime("")
                                                     .fl404bIsNoticeGiven("WithoutNotice")
                                                     .fl404bDateOfNextHearing("")
                                                     .fl404bTimeOfNextHearing("")
                                                     .fl404bCourtName1("")
                                                     .fl404bOtherCourtAddress(address)
                                                     .fl404bTimeEstimate("")
                                                     .fl404bCostOfApplication("")
                                                     .fl404bIsNoticeGiven("WithoutNotice")
                                                     .build())
                              .build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .build();
        assertNotNull(draftAnOrderService.getTheOrderDraftString(caseData));
    }

    @Test
    public void testGenerateSolicitorDraftOrder() throws Exception {
        Document document = draftAnOrderService.generateSolicitorDraftOrder("", caseData);
        assertEquals(document.getDocumentBinaryUrl(),generatedDocumentInfo.getBinaryUrl());
        assertEquals(document.getDocumentUrl(),generatedDocumentInfo.getUrl());
        assertEquals(document.getDocumentHash(),generatedDocumentInfo.getHashToken());
    }

//    @Test
//    public void testListOfOrdersCreated() {
//        CaseData caseData = CaseData.builder()
//            .id(12345L)
//            .caseTypeOfApplication("FL401")
//            .applicantCaseName("Test Case 45678")
//            .fl401FamilymanCaseNumber("familyman12345")
//            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
//            .build();
//        List<String> createdOrders = List.of("Blank order (FL404B)",
//                                             "Standard directions order",
//                                             "Blank order or directions (C21)",
//                                             "Blank order or directions (C21) - to withdraw application",
//                                             "Child arrangements, specific issue or prohibited steps order (C43)",
//                                             "Parental responsibility order (C45A)",
//                                             "Special guardianship order (C43A)",
//                                             "Notice of proceedings (C6) (Notice to parties)",
//                                             "Notice of proceedings (C6a) (Notice to non-parties)",
//                                             "Transfer of case to another court (C49)",
//                                             "Appointment of a guardian (C47A)",
//                                             "Non-molestation order (FL404A)",
//                                             "Occupation order (FL404)",
//                                             "Power of arrest (FL406)",
//                                             "Amended, discharged or varied order (FL404B)",
//                                             "General form of undertaking (N117)",
//                                             "Notice of proceedings (FL402)",
//                                             "Blank order (FL404B)",
//                                             "Other (upload an order)");
//        Map<String, Object> responseMap = serviceOfApplicationService.getOrderSelectionsEnumValues(createdOrders, new HashMap<>());
//        assertEquals(18,responseMap.values().size());
//        assertEquals("1", responseMap.get("option1"));
//
//    }
//
//    @Test
//    public void testCollapasableGettingPopulated() {
//
//        String responseMap = serviceOfApplicationService.getCollapsableOfSentDocuments();
//
//        assertNotNull(responseMap);
//
//    }
//
//    @Test
//    public void testSendViaPost() throws Exception {
//        CaseData caseData = CaseData.builder()
//            .id(12345L)
//            .caseTypeOfApplication("C100")
//            .applicantCaseName("Test Case 45678")
//            .fl401FamilymanCaseNumber("familyman12345")
//            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
//            .build();
//        Map<String,Object> casedata = new HashMap<>();
//        casedata.put("caseTyoeOfApplication","C100");
//        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
//        CaseDetails caseDetails = CaseDetails
//            .builder()
//            .id(123L)
//            .state(CASE_ISSUE.getValue())
//            .data(casedata)
//            .build();
//        CaseData caseData1 = serviceOfApplicationService.sendPost(caseDetails,"test auth");
//        verify(serviceOfApplicationPostService).sendDocs(Mockito.any(CaseData.class),Mockito.anyString());
//    }
//
//    @Test
//    public void testSendViaPostNotInvoked() throws Exception {
//        CaseData caseData = CaseData.builder()
//            .id(12345L)
//            .caseTypeOfApplication("FL401")
//            .applicantCaseName("Test Case 45678")
//            .fl401FamilymanCaseNumber("familyman12345")
//            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
//            .build();
//        Map<String,Object> casedata = new HashMap<>();
//        casedata.put("caseTyoeOfApplication","C100");
//        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
//        CaseDetails caseDetails = CaseDetails
//            .builder()
//            .id(123L)
//            .state(CASE_ISSUE.getValue())
//            .data(casedata)
//            .build();
//        CaseData caseData1 = serviceOfApplicationService.sendPost(caseDetails,"test auth");
//        verifyNoInteractions(serviceOfApplicationPostService);
//    }
//
//    @Test
//    public void testSendViaEmailC100() throws Exception {
//        CaseData caseData = CaseData.builder()
//            .id(12345L)
//            .caseTypeOfApplication("C100")
//            .applicantCaseName("Test Case 45678")
//            .fl401FamilymanCaseNumber("familyman12345")
//            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
//            .build();
//        Map<String,Object> casedata = new HashMap<>();
//        casedata.put("caseTyoeOfApplication","C100");
//        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
//        when(caseInviteManager.generatePinAndSendNotificationEmail(Mockito.any(CaseData.class))).thenReturn(caseData);
//        CaseDetails caseDetails = CaseDetails
//            .builder()
//            .id(123L)
//            .state(CASE_ISSUE.getValue())
//            .data(casedata)
//            .build();
//        CaseData caseData1 = serviceOfApplicationService.sendEmail(caseDetails);
//        verify(serviceOfApplicationEmailService).sendEmailC100(Mockito.any(CaseDetails.class));
//    }
//
//    @Test
//    public void testSendViaEmailFl401() throws Exception {
//        CaseData caseData = CaseData.builder()
//            .id(12345L)
//            .caseTypeOfApplication("FL401")
//            .applicantCaseName("Test Case 45678")
//            .fl401FamilymanCaseNumber("familyman12345")
//            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
//            .build();
//        Map<String,Object> casedata = new HashMap<>();
//        casedata.put("caseTyoeOfApplication","C100");
//        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
//        when(caseInviteManager.generatePinAndSendNotificationEmail(Mockito.any(CaseData.class))).thenReturn(caseData);
//        CaseDetails caseDetails = CaseDetails
//            .builder()
//            .id(123L)
//            .state(CASE_ISSUE.getValue())
//            .data(casedata)
//            .build();
//        CaseData caseData1 = serviceOfApplicationService.sendEmail(caseDetails);
//        verify(serviceOfApplicationEmailService).sendEmailFL401(Mockito.any(CaseDetails.class));
//    }
}
