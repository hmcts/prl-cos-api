package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCourtEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.YesNoNotRequiredEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DirectionOnIssue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.PartiesListGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_RIGHT_TO_ASK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RIGHT_TO_ASK_COURT;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DraftAnOrderServiceTest {

    @InjectMocks
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private ManageOrderService manageOrderService;

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
    private DocumentLanguageService documentLanguageService;

    @Mock
    private LocationRefDataService locationRefDataService;

    @Mock
    private PartiesListGenerator partiesListGenerator;


    @Before
    public void setup() {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
    }

    @Test
    public void testGenerateDraftOrderCollection() {
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        MagistrateLastName magistrateLastName = MagistrateLastName.builder()
            .lastName("Magistrate last")
            .build();

        Element<MagistrateLastName> magistrateLastNameElement = Element.<MagistrateLastName>builder().value(
            magistrateLastName).build();
        List<Element<MagistrateLastName>> magistrateElementList = Collections.singletonList(magistrateLastNameElement);

        List<OrderTypeEnum> orderType = new ArrayList<>();
        orderType.add(OrderTypeEnum.childArrangementsOrder);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));

        DraftOrder draftOrder = DraftOrder.builder()
            .typeOfOrder(SelectTypeOfOrderEnum.interim.getDisplayedValue())
            .orderTypeId(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder.getDisplayedValue())
            .orderDocument(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .orderDocumentWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test title")
                              .dateCreated(LocalDateTime.parse(dtf.format(now)))
                              .status("Draft").build())
            .isTheOrderByConsent(YesOrNo.Yes)
            .wasTheOrderApprovedAtHearing(YesOrNo.Yes)
            .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .recitalsOrPreamble("test preamble")
            .isTheOrderAboutAllChildren(YesNoNotRequiredEnum.yes)
            .orderDirections("test order")
            .furtherDirectionsIfRequired("test further order")
            .childArrangementsOrdersToIssue(orderType)
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .build();

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder()
            .id(UUID.randomUUID())
            .value(draftOrder)
            .build();
        List<Element<DraftOrder>> draftOrderList = Collections.singletonList(draftOrderElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.blankOrderOrDirections)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .selectedOrder("test order")
            .selectTypeOfOrder(SelectTypeOfOrderEnum.general)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("draftSolicitorOrderFilename.pdf")
                                 .build())
            .previewOrderDocWelsh(Document.builder()
                                      .documentUrl(generatedDocumentInfo.getUrl())
                                      .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                      .documentHash(generatedDocumentInfo.getHashToken())
                                      .documentFileName("draftWelshSolicitorFilename")
                                      .build())
            .manageOrders(ManageOrders.builder()
                              .isTheOrderByConsent(YesOrNo.Yes)
                              .recitalsOrPreamble("test recitals")
                              .orderDirections("test orders")
                              .furtherDirectionsIfRequired("test further directions")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .childArrangementsOrdersToIssue(orderType)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .build())
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .isTheOrderAboutAllChildren(YesNoNotRequiredEnum.yes)
            .wasTheOrderApprovedAtHearing(YesOrNo.No)
            .draftOrderCollection(draftOrderList)
            .build();

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap = draftAnOrderService.generateDraftOrderCollection(caseData);
        assertNotNull(stringObjectMap.get("draftOrderCollection"));
    }

    @Test
    public void testGetDraftOrderDynamicList() {
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        Map<String, Object> caseDataMap = draftAnOrderService.getDraftOrderDynamicList(draftOrderCollection, "C100");
        assertEquals("C100", caseDataMap.get("caseTypeOfApplication"));
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderForApplicantSolicitor() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicants(List.of(applicants))
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesOrNo.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        Map<String, Object> caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderForRespondentSolicitor() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder().build())
            .serveOrderData(ServeOrderData.builder().build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        Map<String, Object> caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }


    @Test
    public void testPopulateDraftOrderDocument() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> caseDataMap = draftAnOrderService.populateDraftOrderDocument(
            caseData
        );
        assertNotNull(caseDataMap.get("previewDraftOrder"));
    }

    @Test
    public void testPopulateDraftOrderCustomFields() {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> caseDataMap = draftAnOrderService.populateDraftOrderCustomFields(
            caseData
        );

        assertEquals("test", caseDataMap.get("parentName"));
    }

    @Test
    public void testPopulateCommonDraftOrderFields() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> caseDataMap = draftAnOrderService.populateCommonDraftOrderFields(
            caseData
        );

        assertEquals(CreateSelectOrderOptionsEnum.blankOrderOrDirections, caseDataMap.get("orderType"));
    }

    @Test
    public void testUpdateDraftOrderCollection() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData
        );

        assertEquals(
            JudgeOrMagistrateTitleEnum.districtJudge,
            ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).get(0).getValue().getJudgeOrMagistrateTitle()
        );
    }

    @Test
    public void testGenerateDocumentForC100() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        CaseData caseDataUpdated = draftAnOrderService.generateDocument(callbackRequest,
            caseData
        );

        assertEquals("C100", caseDataUpdated.getCaseTypeOfApplication());
    }

    @Test
    public void testGenerateDocumentForFL401() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        CaseData caseDataUpdated = draftAnOrderService.generateDocument(callbackRequest,
                                                                        caseData
        );
        assertEquals("FL401", caseDataUpdated.getCaseTypeOfApplication());
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_No() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(new ArrayList<>())
            .sdoCafcassOrCymruList(new ArrayList<>())
            .sdoOtherList(new ArrayList<>())
            .sdoPreamblesList(new ArrayList<>())
            .sdoHearingsAndNextStepsList(new ArrayList<>())
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoLocalAuthorityList(new ArrayList<>())
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertFalse(draftAnOrderService.checkStandingOrderOptionsSelected(caseData));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_Yes() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(SdoCourtEnum.crossExaminationEx740,
                                  SdoCourtEnum.crossExaminationQualifiedLegal))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(List.of(SdoDocumentationAndEvidenceEnum.specifiedDocuments,
                                                     SdoDocumentationAndEvidenceEnum.spipAttendance))
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(draftAnOrderService.checkStandingOrderOptionsSelected(caseData));
    }

    @Test
    public void testPopulateStandardDirectionOrderFields() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(SdoCourtEnum.crossExaminationEx740,
                                  SdoCourtEnum.crossExaminationQualifiedLegal))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(List.of(SdoDocumentationAndEvidenceEnum.specifiedDocuments,
                                                     SdoDocumentationAndEvidenceEnum.spipAttendance))
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(partiesListGenerator.buildPartiesList(caseData, new ArrayList<>())).thenReturn(DynamicList.builder().build());

        draftAnOrderService.populateStandardDirectionOrderFields("test-token", caseData, caseDataUpdated);

        assertEquals(RIGHT_TO_ASK_COURT, caseDataUpdated.get("sdoRightToAskCourt"));
    }

    @Test
    public void testPopulateStandardDirectionOrderFieldsNoMatch() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(SdoCourtEnum.transferApplication))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(List.of(SdoOtherEnum.disclosureOfPapers))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.partyRaisedDomesticAbuse))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.miamAttendance
            ))
            .sdoDocumentationAndEvidenceList(List.of(SdoDocumentationAndEvidenceEnum.medicalDisclosure))
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(partiesListGenerator.buildPartiesList(caseData, new ArrayList<>())).thenReturn(DynamicList.builder().build());

        draftAnOrderService.populateStandardDirectionOrderFields("test-token", caseData, caseDataUpdated);

        assertNull(caseDataUpdated.get("sdoRightToAskCourt"));
    }

    @Test
    public void testCheckDirectionOnIssueOptionsSelected_No() {
        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioCourtList(new ArrayList<>())
            .dioCafcassOrCymruList(new ArrayList<>())
            .dioOtherList(new ArrayList<>())
            .dioPreamblesList(new ArrayList<>())
            .dioHearingsAndNextStepsList(new ArrayList<>())
            .dioLocalAuthorityList(new ArrayList<>())
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .directionOnIssue(directionOnIssue)
            .build();

        assertFalse(draftAnOrderService.checkDirectionOnIssueOptionsSelected(caseData));
    }

    @Test
    public void testCheckDirectionOnIssueOptionsSelected_Yes() {
        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioCourtList(List.of(
                DioCourtEnum.transferApplication))
            .dioCafcassOrCymruList(List.of(DioCafcassOrCymruEnum.cafcassCymruSafeguarding,
                                           DioCafcassOrCymruEnum.cafcassSafeguarding))
            .dioOtherList(List.of(DioOtherEnum.parentWithCare))
            .dioPreamblesList(List.of(DioPreamblesEnum.rightToAskCourt))
            .dioHearingsAndNextStepsList(List.of(
                DioHearingsAndNextStepsEnum.caseReviewAtSecondGateKeeping,
                DioHearingsAndNextStepsEnum.updateContactDetails
            ))
            .dioLocalAuthorityList(List.of(DioLocalAuthorityEnum.localAuthorityLetter))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .directionOnIssue(directionOnIssue)
            .build();

        assertTrue(draftAnOrderService.checkStandingOrderOptionsSelected(caseData));
    }

    @Test
    public void testPopulateDirectionOnIssueFields() {
        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioCourtList(List.of(
                DioCourtEnum.transferApplication))
            .dioCafcassOrCymruList(List.of(DioCafcassOrCymruEnum.cafcassCymruSafeguarding,
                                           DioCafcassOrCymruEnum.cafcassSafeguarding))
            .dioOtherList(List.of(DioOtherEnum.parentWithCare))
            .dioPreamblesList(List.of(DioPreamblesEnum.rightToAskCourt))
            .dioHearingsAndNextStepsList(List.of(
                DioHearingsAndNextStepsEnum.caseReviewAtSecondGateKeeping,
                DioHearingsAndNextStepsEnum.updateContactDetails
            ))
            .dioLocalAuthorityList(List.of(DioLocalAuthorityEnum.localAuthorityLetter))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .directionOnIssue(directionOnIssue)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(partiesListGenerator.buildPartiesList(caseData, new ArrayList<>())).thenReturn(DynamicList.builder().build());

        draftAnOrderService.populateDirectionOnIssueFields("test-token", caseData, caseDataUpdated);

        assertEquals(DIO_RIGHT_TO_ASK, caseDataUpdated.get("dioRightToAskCourt"));
    }

    @Test
    public void testPopulateDirectionOnIssueFieldsNoMatch() {
        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioCourtList(List.of(
                DioCourtEnum.transferApplication))
            .dioCafcassOrCymruList(new ArrayList<>())
            .dioOtherList(List.of(DioOtherEnum.disclosureOfPapers))
            .dioPreamblesList(List.of(DioPreamblesEnum.partyRaisedDomesticAbuse))
            .dioHearingsAndNextStepsList(List.of(
                DioHearingsAndNextStepsEnum.allocateNamedJudge
            ))
            .dioLocalAuthorityList(List.of(DioLocalAuthorityEnum.localAuthorityLetter))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .directionOnIssue(directionOnIssue)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(partiesListGenerator.buildPartiesList(caseData, new ArrayList<>())).thenReturn(DynamicList.builder().build());

        draftAnOrderService.populateDirectionOnIssueFields("test-token", caseData, caseDataUpdated);

        assertNull(caseDataUpdated.get("dioRightToAskCourt"));
    }

}
