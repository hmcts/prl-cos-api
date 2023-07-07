package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.SdoDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DirectionOnIssue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SWANSEA_COURT_NAME;
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

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    private DynamicList dynamicList;
    private DynamicMultiSelectList dynamicMultiSelectList;
    private List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
    private DynamicMultiselectListElement dynamicMultiselectListElement;
    private UUID uuid;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    private final String authToken = "Bearer testAuthtoken";
    private final String serviceAuthToken = "serviceTestAuthtoken";

    private CaseData caseData;
    private List<Element<Child>> listOfChildren;
    private List<Element<MagistrateLastName>> magistrateElementList;
    private List<Element<DraftOrder>> draftOrderList;
    @Mock
    private HearingDataService hearingDataService;

    @Mock
    WelshCourtEmail welshCourtEmail;

    @Before
    public void setup() {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

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
        listOfChildren = Collections.singletonList(wrappedChildren);

        MagistrateLastName magistrateLastName = MagistrateLastName.builder()
            .lastName("Magistrate last")
            .build();

        Element<MagistrateLastName> magistrateLastNameElement = Element.<MagistrateLastName>builder().value(
            magistrateLastName).build();
        magistrateElementList = Collections.singletonList(magistrateLastNameElement);

        List<OrderTypeEnum> orderType = new ArrayList<>();
        orderType.add(OrderTypeEnum.childArrangementsOrder);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));

        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label(" ").build();
        dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();
        dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID + "-" + now)
            .label("test")
            .build();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        DraftOrder draftOrder = DraftOrder.builder()
            .typeOfOrder(SelectTypeOfOrderEnum.interim.getDisplayedValue())
            .orderTypeId(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder.getDisplayedValue())
            .orderDocument(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("DraftFilename.pdf")
                               .build())
            .orderDocumentWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("DraftWelshFilename.pdf")
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
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .orderDirections("test order")
            .furtherDirectionsIfRequired("test further order")
            .furtherInformationIfRequired("test further information")
            .childArrangementsOrdersToIssue(orderType)
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .isTheOrderAboutChildren(YesOrNo.Yes)
            .childOption(DynamicMultiSelectList.builder()
                             .value(List.of(DynamicMultiselectListElement.builder().label("John (Child 1)").build())).build()
            )
            .build();

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder()
            .id(UUID.randomUUID())
            .value(draftOrder)
            .build();
        draftOrderList = Collections.singletonList(draftOrderElement);
        HearingData hearingdata = HearingData.builder()
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        List<Element<HearingData>> hearingDataList  = new ArrayList<>();
        hearingDataList.add(element(hearingdata));
        caseData = CaseData.builder()
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
                                 .documentFileName("DraftFilename.pdf")
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
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .childArrangementsOrdersToIssue(orderType)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .isTheOrderAboutChildren(YesOrNo.Yes)
                              .ordersHearingDetails(hearingDataList)
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().label(
                                                   "John (Child 1)").build())).build()
                              )
                              .cafcassCymruEmail("test@test.com")
                              .build())
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .wasTheOrderApprovedAtHearing(YesOrNo.No)
            .draftOrderCollection(draftOrderList)
            .build();

        when(dateTime.now()).thenReturn(LocalDateTime.now());
    }

    @Test
    public void testToGetDraftOrderDynamicList() {

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap = draftAnOrderService.getDraftOrderDynamicList(caseData);

        assertNotNull(stringObjectMap.get("draftOrdersDynamicList"));
        assertNotNull(stringObjectMap.get("caseTypeOfApplication"));
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
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .orderDirections("test order")
            .furtherDirectionsIfRequired("test further order")
            .furtherInformationIfRequired("test further information")
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
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .childArrangementsOrdersToIssue(orderType)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .build())
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .wasTheOrderApprovedAtHearing(YesOrNo.No)
            .draftOrderCollection(draftOrderList)
            .build();

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(manageOrderService.getCurrentCreateDraftOrderDetails(caseData, "Solicitor")).thenReturn(draftOrder);
        when(manageOrderService.getLoggedInUserType("auth-token")).thenReturn("Solicitor");
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap = draftAnOrderService.generateDraftOrderCollection(caseData, "auth-token");
        assertNotNull(stringObjectMap.get("draftOrderCollection"));
    }

    @Test
    public void testGetDraftOrderDynamicList() {
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(caseData)).thenReturn("test@test.com");
        Map<String, Object> caseDataMap = draftAnOrderService.getDraftOrderDynamicList(caseData);
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

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
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
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "eventId"
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderForRespondentSolicitor() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
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
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "eventId"
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderException() {
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
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(null);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        boolean flag = true;
        try {
            draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
                "test token",
                caseData,
                null
            );
        } catch (Exception ex) {
            flag = false;
        }
        assertFalse(flag);
    }


    @Test
    public void testPopulateDraftOrderDocument() {
        DraftOrder draftOrder = DraftOrder.builder()
            .judgeNotes("test")
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
            caseData,
            "testAuth"
        );

        assertEquals("test", caseDataMap.get("parentName"));
    }


    @Test
    public void testPopulateCommonDraftOrderFields() {
        final String authorisation = "Bearer someAuthorisationToken";
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
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder()
                              .hearingsType(dynamicList)
                              .build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);

        when(manageOrderService.populateHearingsDropdown(authorisation, caseData)).thenReturn(dynamicList);

        Map<String, Object> caseDataMap = draftAnOrderService.populateCommonDraftOrderFields(
            authorisation,
            caseData
        );

        assertEquals(CreateSelectOrderOptionsEnum.blankOrderOrDirections, caseDataMap.get("orderType"));
    }


    @Test
    public void testUpdateDraftOrderCollection() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
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
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            null
        );

        assertEquals(
            JudgeOrMagistrateTitleEnum.districtJudge,
            ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).get(0).getValue().getJudgeOrMagistrateTitle()
        );
    }

    @Test
    public void testUpdateDraftOrderCollectionWithUpdatedStatus() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
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
            .doYouWantToEditTheOrder(YesOrNo.No)
            .manageOrders(ManageOrders.builder()
                              .makeChangesToUploadedOrder(YesOrNo.No)
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            null
        );

        assertNotNull(caseDataMap);
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
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());

        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .children(children)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).c21OrderOptions(
                C21OrderOptionsEnum.c21NoOrderMade).build())
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);

        CaseData caseDataUpdated = draftAnOrderService.generateDocument(
            callbackRequest,
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
        FL404 fl404 = FL404.builder().build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .applicantsFL401(PartyDetails.builder().firstName("test").lastName("test").build())
            .respondentsFL401(PartyDetails.builder().firstName("test")
                                  .lastName("test")
                                  .address(Address.builder().addressLine1("test").county("test").postCode("123").build())
                                  .build())

            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .fl404CustomFields(fl404).c21OrderOptions(
                    C21OrderOptionsEnum.c21NoOrderMade).build())
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

        CaseData caseDataUpdated = draftAnOrderService.generateDocument(
            callbackRequest,
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
            .sdoFurtherList(new ArrayList<>())
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertFalse(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_Yes() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal
            ))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(List.of(
                SdoDocumentationAndEvidenceEnum.specifiedDocuments,
                SdoDocumentationAndEvidenceEnum.spipAttendance
            ))
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .sdoFurtherList(List.of(SdoFurtherInstructionsEnum.newDirection))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData));
    }

    @Test
    public void testPopulateStandardDirectionOrderFields() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal
            ))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(List.of(
                SdoDocumentationAndEvidenceEnum.specifiedDocuments,
                SdoDocumentationAndEvidenceEnum.spipAttendance
            ))
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        draftAnOrderService.populateStandardDirectionOrderDefaultFields("test-token", caseData, caseDataUpdated);

        assertEquals(RIGHT_TO_ASK_COURT, caseDataUpdated.get("sdoRightToAskCourt"));
    }

    @Test
    public void testPopulateStandardDirectionOrderFieldsNoMatch() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(SdoCourtEnum.transferApplication))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(List.of(SdoOtherEnum.disclosureOfPapers))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
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
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        draftAnOrderService.populateStandardDirectionOrderDefaultFields("test-token", caseData, caseDataUpdated);

        assertNotNull(caseDataUpdated.get("sdoRightToAskCourt"));
    }

    @Test
    public void testPopulateStandardDirectionOrderTest() throws JsonProcessingException {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(SdoCourtEnum.transferApplication))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(List.of(SdoOtherEnum.disclosureOfPapers))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.miamAttendance
            ))
            .sdoDocumentationAndEvidenceList(List.of(SdoDocumentationAndEvidenceEnum.medicalDisclosure))
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .build();

        DraftOrder draftOrder = DraftOrder.builder().sdoDetails(SdoDetails.builder().build())
            .orderDocument(Document.builder().documentFileName("abc-welsh.pdf").build())
            .orderDocumentWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        DraftOrder draftOrder1 = DraftOrder.builder().sdoDetails(SdoDetails.builder().build())
            .orderDocument(Document.builder().documentFileName("abd.pdf").build())
            .orderDocumentWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        DraftOrder draftOrder2 = DraftOrder.builder().sdoDetails(SdoDetails.builder().build())
            .orderDocument(Document.builder().documentFileName("abd.pdf").build())
            .orderDocumentWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        Element<DraftOrder> draftOrderElement1 = element(draftOrder1);
        Element<DraftOrder> draftOrderElement2 = element(draftOrder2);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        draftOrderCollection.add(draftOrderElement1);
        draftOrderCollection.add(draftOrderElement2);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .draftOrderCollection(draftOrderCollection)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        SdoDetails sdoDetails = SdoDetails.builder().build();
        String sdoDetailsJson = null;
        Map<String, Object> standardDirectionOrderMap = new HashMap<>();
        when(draftAnOrderService.copyPropertiesToStandardDirectionOrder(sdoDetails)).thenReturn(standardDirectionOrder);
        when(objectMapper.readValue(sdoDetailsJson, StandardDirectionOrder.class)).thenReturn(standardDirectionOrder);
        when(objectMapper.convertValue(standardDirectionOrder, Map.class)).thenReturn(standardDirectionOrderMap);
        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        Map<String, Object> caseDataMap = draftAnOrderService.populateStandardDirectionOrder("test-token", caseData);
        assertEquals(1, ((List<Element<DraftOrder>>) caseDataMap.get("sdoDisclosureOfPapersCaseNumbers")).size());
        //assertNotNull(caseDataUpdated.get("sdoRightToAskCourt"));
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

        assertFalse(DraftAnOrderService.checkDirectionOnIssueOptionsSelected(caseData));
    }

    @Test
    public void testCheckDirectionOnIssueOptionsSelected_Yes() {
        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioCourtList(List.of(
                DioCourtEnum.transferApplication))
            .dioCafcassOrCymruList(List.of(
                DioCafcassOrCymruEnum.cafcassCymruSafeguarding,
                DioCafcassOrCymruEnum.cafcassSafeguarding
            ))
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

        assertTrue(DraftAnOrderService.checkDirectionOnIssueOptionsSelected(caseData));
    }

    @Test
    public void testPopulateDirectionOnIssueFields() {
        DioOtherEnum application = DioOtherEnum.applicationToApplyPermission;
        DioOtherEnum parent = DioOtherEnum.parentWithCare;
        List otherList = new ArrayList<>();

        otherList.add(application);
        otherList.add(parent);

        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioCourtList(List.of(
                DioCourtEnum.transferApplication))
            .dioCafcassOrCymruList(List.of(
                DioCafcassOrCymruEnum.cafcassCymruSafeguarding,
                DioCafcassOrCymruEnum.cafcassSafeguarding
            ))
            .dioOtherList(otherList)
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
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

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
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        draftAnOrderService.populateDirectionOnIssueFields("test-token", caseData, caseDataUpdated);

        assertNull(caseDataUpdated.get("dioRightToAskCourt"));
    }

    @Test
    public void testPopulateCustomFieldsBlankOrderOrDirections() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        assertNull(draftAnOrderService.populateCustomFields(caseData));

    }

    @Test
    public void testPopulateCustomFieldsNonMolestation() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .build();

        assertNull(draftAnOrderService.populateCustomFields(caseData));
    }

    @Test
    public void testPopulateCustomFieldsAppointmentOfGuardian() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .build();

        assertEquals(caseData, draftAnOrderService.populateCustomFields(caseData));

    }

    @Test
    public void testGetGeneratedDocument() {
        GeneratedDocumentInfo generatedDocumentInfo1 = GeneratedDocumentInfo.builder().build();
        assertNotNull(ReflectionTestUtils.invokeMethod(draftAnOrderService, "getGeneratedDocument",
                                                       generatedDocumentInfo1, true, new HashMap<>()
        ));
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderDaForApplicantSolicitor() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc-welsh.pdf").build())
            .orderDocumentWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        DraftOrder draftOrder1 = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abd.pdf").build())
            .orderDocumentWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        DraftOrder draftOrder2 = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abd.pdf").build())
            .orderDocumentWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        Element<DraftOrder> draftOrderElement1 = element(draftOrder1);
        Element<DraftOrder> draftOrderElement2 = element(draftOrder2);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        draftOrderCollection.add(draftOrderElement1);
        draftOrderCollection.add(draftOrderElement2);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<EmailInformation> emailInformationElement = element(EmailInformation.builder().build());
        Element<PostalInformation> postalInformationElement = element(PostalInformation.builder().build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .previewOrderDocWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicantsFL401(partyDetails)
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesOrNo.Yes)
                              .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
                              .emailInformationDA(List.of(emailInformationElement))
                              .postalInformationDA(List.of(postalInformationElement))
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "testevent"
        );

        assertEquals(2, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }

    @Test
    public void testGenerateDraftOrderCollectionForFirstOrder() {
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


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.blankOrderOrDirections)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .selectedOrder("test order")
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
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .childArrangementsOrdersToIssue(orderType)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .build())
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .wasTheOrderApprovedAtHearing(YesOrNo.No)
            .build();

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap = draftAnOrderService.generateDraftOrderCollection(caseData, "auth-token");
        assertNotNull(stringObjectMap.get("draftOrderCollection"));
    }

    @Test
    public void testGetDraftOrderInfo() throws Exception {

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

        Map<String, Object> stringObjectMap = new HashMap<>();

        stringObjectMap = draftAnOrderService.getDraftOrderInfo(authToken, caseData);
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testJudgeOrAdminEditApproveDraftOrderMidEvent() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
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
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        stringObjectMap = draftAnOrderService.judgeOrAdminEditApproveDraftOrderMidEvent(authToken, callbackRequest);
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testJudgeOrAdminEditApproveDraftOrderAboutToSubmit() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
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
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        stringObjectMap = draftAnOrderService.judgeOrAdminEditApproveDraftOrderAboutToSubmit(
            authToken,
            callbackRequest
        );
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testJudgeOrAdminEditApproveDraftOrderAboutToSubmitCaseId() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
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
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("test")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        stringObjectMap = draftAnOrderService.judgeOrAdminEditApproveDraftOrderAboutToSubmit(
            authToken,
            callbackRequest
        );
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testGenerateOrderDocument() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
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
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .children(children)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).c21OrderOptions(
                C21OrderOptionsEnum.c21NoOrderMade).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);

        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);

        stringObjectMap = draftAnOrderService.generateOrderDocument(authToken, callbackRequest);
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testGenerateOrderDocumentCaseId() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());

        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .children(children)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).c21OrderOptions(
                C21OrderOptionsEnum.c21NoOrderMade).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("test")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);

        stringObjectMap = draftAnOrderService.generateOrderDocument(authToken, callbackRequest);
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testCheckIfOrderCanReviewedIfNitApprovedInAdminApprove() {

        Map<String, Object> stringObjectMap = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Created by Admin");
        response.put("reviewRequiredBy", "A judge or legal adviser needs to check the order");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        String errorMessage = DraftAnOrderService.checkIfOrderCanReviewed(callbackRequest, response);

        assertNotNull(errorMessage);
    }

    @Test
    public void testCheckIfOrderCanReviewedIfNitApprovedInEditApprove() {

        Map<String, Object> stringObjectMap = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Created by Judge");
        response.put("reviewRequiredBy", "A manager needs to check the order");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("editAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        String errorMessage = DraftAnOrderService.checkIfOrderCanReviewed(callbackRequest, response);

        assertNull(errorMessage);
    }

    @Test
    public void testResetFields() {

        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> caseDataUpdated = draftAnOrderService.resetFields(callbackRequest);
        assertNotNull(caseDataUpdated);
    }

    private static <T> Element<T> customElement(T element) {
        return Element.<T>builder()
            .id(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .value(element)
            .build();
    }
}
