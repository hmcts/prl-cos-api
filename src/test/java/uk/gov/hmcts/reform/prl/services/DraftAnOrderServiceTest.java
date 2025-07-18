package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCourtEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForCourtAdminOrderEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DomesticAbuseOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DraftOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.FcOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.enums.sdo.AllocateOrReserveJudgeEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.exception.ManageOrderRuntimeException;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.languagecontext.UserLanguage;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.models.wa.AdditionalProperties;
import uk.gov.hmcts.reform.prl.models.wa.ClientContext;
import uk.gov.hmcts.reform.prl.models.wa.TaskData;
import uk.gov.hmcts.reform.prl.models.wa.UserTask;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.PartiesListGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_RIGHT_TO_ASK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_TEMPLATE_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_NOT_AVAILABLE_FL401;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_NOT_AVAILABLE_FL401_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RIGHT_TO_ASK_COURT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SWANSEA_COURT_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.ADMIN_EDIT_AND_APPROVE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.Event.EDIT_AND_APPROVE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.Event.EDIT_RETURNED_ORDER;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum.dateReservedWithListAssit;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.nonMolestation;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.partyToProvideDetailsCmyru;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.partyToProvideDetailsOnly;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.safeguardingCafcassCymru;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.safeguardingCafcassOnly;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.section7Report;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum.addNewPreamble;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum.afterSecondGateKeeping;
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
    RoleAssignmentService roleAssignmentService;

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

    @Mock
    private HearingService hearingService;
    @Mock
    CaseUtils caseUtils;

    @Mock
    private UserService userService;

    @Mock
    private DocumentSealingService documentSealingService;

    private DynamicList dynamicList;
    private DynamicMultiSelectList dynamicMultiSelectList;
    private List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
    private DynamicMultiselectListElement dynamicMultiselectListElement;
    private UUID uuid;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    private final String authToken = "Bearer testAuthtoken";
    private final String serviceAuthToken = "serviceTestAuthtoken";
    private static final String BOLD_BEGIN = "<span class='heading-h3'>";
    private static final String BOLD_END = "</span>";

    private CaseData caseData;
    private List<Element<Child>> listOfChildren;
    private List<Element<MagistrateLastName>> magistrateElementList;
    private List<Element<DraftOrder>> draftOrderList;

    private String clientContextCoded;
    @Mock
    private HearingDataService hearingDataService;

    @Mock
    WelshCourtEmail welshCourtEmail;

    @Before
    public void setup() throws JsonProcessingException {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(documentLanguage);
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
                              .build())
            .isTheOrderByConsent(Yes)
            .wasTheOrderApprovedAtHearing(Yes)
            .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .recitalsOrPreamble("test preamble")
            .isTheOrderAboutAllChildren(Yes)
            .orderDirections("test order")
            .furtherDirectionsIfRequired("test further order")
            .furtherInformationIfRequired("test further information")
            .childArrangementsOrdersToIssue(orderType)
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .isTheOrderAboutChildren(Yes)
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
        List<Element<HearingData>> hearingDataList = new ArrayList<>();
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
                              .isTheOrderByConsent(Yes)
                              .recitalsOrPreamble("test recitals")
                              .orderDirections("test orders")
                              .furtherDirectionsIfRequired("test further directions")
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .childArrangementsOrdersToIssue(orderType)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .isTheOrderAboutChildren(Yes)
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
            .wasTheOrderApprovedAtHearing(No)
            .draftOrderCollection(draftOrderList)
            .build();

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.getHearingDataForSelectedHearing(any(), any(), anyString()))
            .thenReturn(List.of(element(HearingData.builder().build())));
        uuid = UUID.fromString(TEST_UUID);
        when(manageOrderService.populateCustomOrderFields(Mockito.any(), Mockito.any(), any())).thenReturn(caseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().forename("test")
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());

        WaMapper waMapper = WaMapper.builder()
            .clientContext(ClientContext.builder()
                               .userLanguage(UserLanguage.builder().language("en").build())
                               .userTask(UserTask.builder()
                                             .completeTask(true)
                                             .taskData(TaskData.builder()
                                                           .id("test")
                                                           .name("test")
                                                           .additionalProperties(AdditionalProperties.builder()
                                                                                     .orderId(UUID.randomUUID().toString())
                                                                                     .hearingId("999999")
                                                                                     .build())
                                                           .build())
                                             .build())
                               .build())
            .build();
        String json = new ObjectMapper().writeValueAsString(waMapper);
        clientContextCoded = Base64.getEncoder().encodeToString(json.getBytes());
    }

    @Test
    public void testToGetDraftOrderDynamicListForAdmin() {

        Map<String, Object> stringObjectMap = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(ElementUtils.element(
            caseData.getDraftOrderCollection().get(0).getId(),
            caseData.getDraftOrderCollection().get(0).getValue().toBuilder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                              .dateCreated(LocalDateTime.now())
                                                                                              .createdBy("test title")
                                                                                              .reviewRequiredBy(
                                                                                                  AmendOrderCheckEnum
                                                                                                      .judgeOrLegalAdvisorCheck)
                                                                                              .status(OrderStatusEnum.createdByCA
                                                                                                          .getDisplayedValue())
                                                                                              .isJudgeApprovalNeeded(No)
                                                                                              .build()).build()
        ));

        CaseData updatedCaseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(manageOrderService.getLoggedInUserType(authToken)).thenReturn(UserRoles.COURT_ADMIN.name());

        stringObjectMap = draftAnOrderService.getDraftOrderDynamicList(updatedCaseData,
                                                                       Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId(),
                                                                       clientContextCoded,
                                                                       authToken);

        assertNotNull(stringObjectMap.get("draftOrdersDynamicList"));
        assertNotNull(stringObjectMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testToGetDraftOrderDynamicListForAdmin_OldCases() {

        Map<String, Object> stringObjectMap = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(ElementUtils.element(
            caseData.getDraftOrderCollection().get(0).getId(),
            caseData.getDraftOrderCollection().get(0).getValue().toBuilder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                              .dateCreated(LocalDateTime.now())
                                                                                              .createdBy("test title")
                                                                                              .reviewRequiredBy(
                                                                                                  AmendOrderCheckEnum
                                                                                                      .judgeOrLegalAdvisorCheck)
                                                                                              .status(OrderStatusEnum.reviewedByJudge
                                                                                                          .getDisplayedValue())
                                                                                              .build()).build()
        ));

        CaseData updatedCaseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(manageOrderService.getLoggedInUserType(authToken)).thenReturn(UserRoles.COURT_ADMIN.name());

        stringObjectMap = draftAnOrderService.getDraftOrderDynamicList(updatedCaseData,
                                                                       Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId(),
                                                                       clientContextCoded,
                                                                       authToken);

        assertNotNull(stringObjectMap.get("draftOrdersDynamicList"));
        assertNotNull(stringObjectMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testToGetDraftOrderDynamicListFilterByHearingWhenWaHearingListed() {

        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(ElementUtils.element(
            caseData.getDraftOrderCollection().get(0).getId(),
            caseData.getDraftOrderCollection().get(0).getValue().toBuilder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                              .dateCreated(LocalDateTime.now())
                                                                                              .createdBy("test title")
                                                                                              .reviewRequiredBy(
                                                                                                  AmendOrderCheckEnum
                                                                                                      .judgeOrLegalAdvisorCheck)
                                                                                              .status(OrderStatusEnum.createdByCA
                                                                                                          .getDisplayedValue())
                                                                                              .isJudgeApprovalNeeded(No)
                                                                                              .build())
                .manageOrderHearingDetails(List.of(element(HearingData.builder().hearingId("999999").build()))).build()
        ));

        draftOrderCollection.add(ElementUtils.element(
            caseData.getDraftOrderCollection().get(0).getId(),
            caseData.getDraftOrderCollection().get(0).getValue().toBuilder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                              .dateCreated(LocalDateTime.now())
                                                                                              .createdBy("test title")
                                                                                              .reviewRequiredBy(
                                                                                                  AmendOrderCheckEnum
                                                                                                      .judgeOrLegalAdvisorCheck)
                                                                                              .status(OrderStatusEnum.createdByCA
                                                                                                          .getDisplayedValue())
                                                                                              .isJudgeApprovalNeeded(No)
                                                                                              .build())
                .manageOrderHearingDetails(List.of(element(HearingData.builder().hearingId("888888").build()))).build()
        ));

        CaseData updatedCaseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(manageOrderService.getLoggedInUserType(authToken)).thenReturn(UserRoles.COURT_ADMIN.name());
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap = draftAnOrderService.getDraftOrderDynamicList(updatedCaseData,
                                                                       Event.HEARING_EDIT_AND_APPROVE_ORDER.getId(),
                                                                       clientContextCoded,
                                                                       authToken);

        assertNotNull(stringObjectMap.get("draftOrdersDynamicList"));
        assertNotNull(stringObjectMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testGenerateDraftOrderCollection() {


        MagistrateLastName magistrateLastName = MagistrateLastName.builder()
            .lastName("Magistrate last")
            .build();

        Element<MagistrateLastName> magistrateLastNameElement = Element.<MagistrateLastName>builder().value(
            magistrateLastName).build();
        List<OrderTypeEnum> orderType = new ArrayList<>();
        orderType.add(OrderTypeEnum.childArrangementsOrder);
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .parentalResponsibilityDetails("test")
            .build();
        List<Element<MagistrateLastName>> magistrateElementList = Collections.singletonList(magistrateLastNameElement);
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));

        DraftOrder draftOrder = DraftOrder.builder()
            .manageOrderHearingDetails(List.of(element(HearingData.builder().build())))
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
            .isTheOrderByConsent(Yes)
            .wasTheOrderApprovedAtHearing(Yes)
            .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .recitalsOrPreamble("test preamble")
            .isTheOrderAboutAllChildren(Yes)
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
        when(dateTime.now()).thenReturn(LocalDateTime.now());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
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
                              .isTheOrderByConsent(Yes)
                              .recitalsOrPreamble("test recitals")
                              .orderDirections("test orders")
                              .furtherDirectionsIfRequired("test further directions")
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .childArrangementsOrdersToIssue(orderType)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .loggedInUserType("Solicitor")
                              .hasJudgeProvidedHearingDetails(Yes)
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .judgeOrMagistratesLastName("judge last")
            .justiceLegalAdviserFullName("Judge full")
            .magistrateLastName(magistrateElementList)
            .wasTheOrderApprovedAtHearing(No)
            .draftOrderCollection(draftOrderList)
            .build();
        when(manageOrderService.getCurrentCreateDraftOrderDetails(any(), anyString(),any())).thenReturn(draftOrder);
        when(manageOrderService.getLoggedInUserType("auth-token")).thenReturn("Solicitor");
        List<Element<DraftOrder>> result = draftAnOrderService.generateDraftOrderCollection(caseData, "auth-token");
        System.out.println(result);
        assertNotNull(result);
    }

    @Test
    public void testGetDraftOrderDynamicListForJudge() {
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(ElementUtils.element(
            caseData.getDraftOrderCollection().get(0).getId(),
            caseData.getDraftOrderCollection().get(0).getValue().toBuilder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                              .dateCreated(LocalDateTime.now())
                                                                                              .createdBy("test title")
                                                                                              .isJudgeApprovalNeeded(No)
                                                                                              .reviewRequiredBy(
                                                                                                  AmendOrderCheckEnum
                                                                                                      .noCheck)
                                                                                              .status(OrderStatusEnum.createdByCA
                                                                                                          .getDisplayedValue())
                                                                                              .isJudgeApprovalNeeded(Yes)
                                                                                              .build()).build()
        ));

        CaseData updatedCaseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(updatedCaseData)).thenReturn("test@test.com");
        when(manageOrderService.getLoggedInUserType(authToken)).thenReturn(UserRoles.JUDGE.name());

        Map<String, Object> caseDataMap = draftAnOrderService.getDraftOrderDynamicList(
            updatedCaseData,
            EDIT_AND_APPROVE_ORDER.getId(),
            clientContextCoded,
            authToken
        );
        assertEquals("C100", caseDataMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testGetDraftOrderDynamicListForJudge_OldCases() {
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(ElementUtils.element(
            caseData.getDraftOrderCollection().get(0).getId(),
            caseData.getDraftOrderCollection().get(0).getValue().toBuilder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                              .dateCreated(LocalDateTime.now())
                                                                                              .createdBy("test title")
                                                                                              .reviewRequiredBy(
                                                                                                  AmendOrderCheckEnum
                                                                                                      .noCheck)
                                                                                              .status(OrderStatusEnum.draftedByLR
                                                                                                          .getDisplayedValue())
                                                                                              .build()).build()
        ));

        CaseData updatedCaseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(updatedCaseData)).thenReturn("test@test.com");
        when(manageOrderService.getLoggedInUserType(authToken)).thenReturn(UserRoles.JUDGE.name());

        Map<String, Object> caseDataMap = draftAnOrderService.getDraftOrderDynamicList(
            updatedCaseData,
            EDIT_AND_APPROVE_ORDER.getId(),
            clientContextCoded,
            authToken
        );
        assertEquals("C100", caseDataMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderForApplicantSolicitor() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .dateOrderMade(LocalDate.parse("2022-02-16"))
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        List<Element<OrderDetails>> elementList = new ArrayList<>();
        elementList.add(element(OrderDetails.builder().dateCreated(LocalDateTime.now()).build()));
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .orderCollection(elementList)
            .draftOrderCollection(draftOrderCollection)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicants(List.of(applicants))
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "editReturnedOrder",
            PrlAppsConstants.ENGLISH
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }


    @Test
    public void testRemoveDraftUploadedOrderAndAddToFinalOrderForApplicantSolicitor() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .isOrderUploadedByJudgeOrAdmin(Yes)
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
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(documentSealingService.sealDocument(Mockito.any(Document.class), Mockito.any(CaseData.class), Mockito.anyString()))
            .thenReturn(Document.builder().build());
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "eventId",
            PrlAppsConstants.ENGLISH
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }


    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderWithEditYes() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .approvalDate(LocalDate.now())
            .dateOrderMade(LocalDate.now())
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .isOrderUploadedByJudgeOrAdmin(No)
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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .draftOrderCollection(draftOrderCollection)
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicants(List.of(applicants))
            .selectTypeOfOrder(SelectTypeOfOrderEnum.general)
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(FINAL_TEMPLATE_WELSH, "");
        when(documentLanguageService.docGenerateLang(any())).thenReturn(DocumentLanguage.builder()
                                                                            .isGenWelsh(true).isGenEng(false).build());
        when(manageOrderService.getOrderTemplateAndFile(any())).thenReturn(fieldMap);
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "eventId",
            PrlAppsConstants.ENGLISH
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }

    @Test
    public void testPopulateCommonDraftOrderFieldsWithHearingDetails() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .isOrderCreatedBySolicitor(No)
                .judgeNotes("test")
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .manageOrderHearingDetails(List.of(element(HearingData.builder()
                                                           .confirmedHearingDates(DynamicList.builder().build()).build())))
            .build();

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString(TEST_UUID))
            .value(draftOrder).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder()
                              .hearingsType(dynamicList)
                              .build())
            .draftOrdersDynamicList(TEST_UUID)
            .build();
        when(elementUtils.getDynamicListSelectedValue(Mockito.any(), Mockito.any())).thenReturn(UUID.fromString(
            TEST_UUID));
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(
            anyString(),
            anyString(),
            any(),
            any()
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().retrievedHearingDates(DynamicList.builder().build()).build());
        Map<String, Object> caseDataMap = draftAnOrderService.populateCommonDraftOrderFields(
            authToken,
            caseData,
            draftOrder,
            PrlAppsConstants.ENGLISH,
            Optional.empty()
        );
        assertNotNull(caseDataMap);
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderForRespondentSolicitor() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder().build())
            .serveOrderData(ServeOrderData.builder().build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "eventId",
            PrlAppsConstants.ENGLISH
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
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicants(List.of(applicants))
            .doYouWantToEditTheOrder(Yes)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(No).build())
            .build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(null);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        boolean flag = true;
        try {
            draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
                "test token",
                caseData,
                null,
                PrlAppsConstants.ENGLISH
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
        Map<String, Object> caseDataMap = draftAnOrderService.populateDraftOrderDocument(caseData, authToken, null,
                                                                                         ADMIN_EDIT_AND_APPROVE_ORDER.getId());
        assertNotNull(caseDataMap.get("previewDraftOrder"));
    }

    @Test
    public void testPopulateDraftOrderDocumentWithHearingPage() {
        DraftOrder draftOrder = DraftOrder.builder()
            .judgeNotes("test")
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
            caseData,
            authToken,
            null,
            ADMIN_EDIT_AND_APPROVE_ORDER.getId()
        );
        assertNotNull(caseDataMap.get("previewDraftOrder"));
    }

    @Test
    public void testPopulateDraftOrderDocumentWithHearingPageEmptyJudgeNotes() {
        DraftOrder draftOrder = DraftOrder.builder()
                .orderDocument(Document.builder().documentFileName("abc.pdf").build())
                .otherDetails(OtherDraftOrderDetails.builder()
                        .dateCreated(LocalDateTime.now())
                        .createdBy("test")
                        .build())
                .c21OrderOptions(C21OrderOptionsEnum.c21other)
                .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
            caseData,
            authToken,
            null,
            ADMIN_EDIT_AND_APPROVE_ORDER.getId()
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
            .manageOrdersApplicant("applicant")
            .manageOrdersApplicantReference("aapRef")
            .manageOrdersRespondent("respondent")
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
        Map<String, Object> caseDataMap = draftAnOrderService.populateDraftOrderCustomFields(caseData,
                                                                                             draftOrder);
        assertEquals("test", caseDataMap.get("parentName"));
    }

    @Test
    public void testPopulateDraftOrderCustomFieldsWithSdo() {
        DraftOrder draftOrder = DraftOrder.builder().sdoDetails(SdoDetails.builder()
                                                                    .sdoAfterSecondGatekeeping("yes").build())
            .parentName("test")
            .orderDocument(Document.builder().documentFileName("abc-welsh.pdf").build())
            .orderDocumentWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
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
        when(objectMapper.convertValue(
            draftOrder.getSdoDetails(),
            Map.class
        )).thenReturn(new HashMap<String, Object>());
        Map<String, Object> caseDataMap = draftAnOrderService.populateDraftOrderCustomFields(caseData,
                                                                                             draftOrder);
        assertEquals(null, caseDataMap.get("parentName"));
    }

    @Test
    public void testPopulateDraftOrderCustomFieldsWithHearingPage() {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Map<String, Object> caseDataMap = draftAnOrderService.populateDraftOrderCustomFields(caseData, draftOrder);

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

        Map<String, Object> caseDataMap = draftAnOrderService.populateCommonDraftOrderFields(authorisation,
            caseData, draftOrder, PrlAppsConstants.ENGLISH, Optional.empty());

        assertEquals(CreateSelectOrderOptionsEnum.blankOrderOrDirections, caseDataMap.get("orderType"));
    }


    @Test
    public void testPopulateCommonDraftOrderFieldsWithHearingPage() {
        final String authorisation = "Bearer someAuthorisationToken";
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .isTheOrderAboutAllChildren(No)
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
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);

        when(manageOrderService.populateHearingsDropdown(authorisation, caseData)).thenReturn(dynamicList);

        Map<String, Object> caseDataMap = draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData,
            draftOrder, PrlAppsConstants.ENGLISH, Optional.empty());

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
            .isOrderCreatedBySolicitor(Yes)
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
            .doYouWantToEditTheOrder(Yes)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .whatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe)
                              .build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            Event.EDIT_AND_APPROVE_ORDER.getId(),
            null
        );

        assertEquals(
            JudgeOrMagistrateTitleEnum.districtJudge,
            ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).get(0).getValue().getJudgeOrMagistrateTitle()
        );
    }

    @Test
    public void testUpdateDraftOrderCollection1() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .isOrderCreatedBySolicitor(No)
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
        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        ordersHearingDetails.add(element(HearingData.builder().build()));
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .doYouWantToEditTheOrder(No)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .makeChangesToUploadedOrder(Yes)
                              .ordersHearingDetails(ordersHearingDetails)
                              .build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            null,
            null
        );

        assertNotNull(caseDataMap);
    }

    @Test
    public void testUpdateDraftOrderCollection2() {
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
            .doYouWantToEditTheOrder(No)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .makeChangesToUploadedOrder(Yes)
                              .build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            null,
            null
        );

        assertNotNull(caseDataMap);
    }

    @Test
    public void testUpdateDraftOrderCollectionForAutoHearing() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderTypeId("1234")
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.blank)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .c21OrderOptions(C21OrderOptionsEnum.c21NoOrderMade)
            .isOrderCreatedBySolicitor(Yes)
            .isAutoHearingReqPending(Yes)
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
        Element<PartyDetails> applicants = element(partyDetails);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .doYouWantToEditTheOrder(Yes)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .whatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe)
                              .whatToDoWithOrderSolicitor(OrderApprovalDecisionsForSolicitorOrderEnum.editTheOrderAndServe)
                              .build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .applicants(List.of(applicants))
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.JUDGE.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            authToken,
            Event.EDIT_AND_APPROVE_ORDER.getId(),
            TEST_UUID
        );

        assertEquals(
            Yes,
            ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).get(0).getValue().getIsAutoHearingReqPending()
        );
    }

    @Test
    public void testUpdateDraftOrderCollectionForAutoHearing1() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderTypeId("123")
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .isOrderCreatedBySolicitor(Yes)
            .isAutoHearingReqPending(Yes)
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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .doYouWantToEditTheOrder(Yes)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .whatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe)
                              .whatToDoWithOrderSolicitor(OrderApprovalDecisionsForSolicitorOrderEnum.editTheOrderAndServe)
                              .build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.CASEMANAGER.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            authToken,
            Event.EDIT_AND_APPROVE_ORDER.getId(),
            TEST_UUID
        );

        assertEquals(
            Yes,
            ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).get(0).getValue().getIsAutoHearingReqPending()
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
            .approvalDate(LocalDate.parse("2022-02-16"))
            .build();

        DraftOrder draftOrder1 = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .approvalDate(LocalDate.parse("2022-02-17"))
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        Element<DraftOrder> draftOrderElement1 = element(draftOrder1);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        draftOrderCollection.add(draftOrderElement1);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .doYouWantToEditTheOrder(No)
            .manageOrders(ManageOrders.builder()
                              .makeChangesToUploadedOrder(No)
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            null,
            null
        );

        assertNotNull(caseDataMap);
    }

    @Test
    public void testUpdateDraftOrderCollectionForDocmosis() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .approvalDate(LocalDate.parse("2022-02-16"))
            .build();

        DraftOrder draftOrder1 = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .approvalDate(LocalDate.parse("2022-02-17"))
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        Element<DraftOrder> draftOrderElement1 = element(draftOrder1);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        draftOrderCollection.add(draftOrderElement1);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .doYouWantToEditTheOrder(No)
            .manageOrders(ManageOrders.builder()
                              .makeChangesToUploadedOrder(Yes)
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(hearingDataService.getHearingDataForSelectedHearing(any(), any(), anyString()))
            .thenReturn(List.of(element(HearingData.builder()
                                            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
                                            .build())));
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            null,
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

        caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .children(children)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).c21OrderOptions(
                C21OrderOptionsEnum.c21NoOrderMade).build())
            .createSelectOrderOptions(nonMolestation)
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
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);

        CaseData caseDataUpdated = draftAnOrderService.updateCustomFieldsWithApplicantRespondentDetails(
            callbackRequest,
            caseData,
            null,
            PrlAppsConstants.ENGLISH
        );

        assertEquals("C100", caseDataUpdated.getCaseTypeOfApplication());
    }

    @Test
    public void testGenerateDocumentForFL401() {

        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test")
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = element(UUID.fromString(TEST_UUID), draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        FL404 fl404 = FL404.builder().build();

        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .applicantsFL401(PartyDetails.builder().firstName("test").lastName("test").build())
            .respondentsFL401(PartyDetails.builder().firstName("test")
                                  .lastName("test")
                                  .address(Address.builder().addressLine1("test").county("test").postCode("123").build())
                                  .build())
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .fl404CustomFields(fl404).c21OrderOptions(
                    C21OrderOptionsEnum.c21NoOrderMade).build())
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populateCustomOrderFields(any(), Mockito.any(), Mockito.any())).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        CaseData caseDataUpdated = draftAnOrderService.updateCustomFieldsWithApplicantRespondentDetails(
            callbackRequest,
            caseData,
            null,
            PrlAppsConstants.ENGLISH
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
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertFalse(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
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
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_No7() {
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
            .sdoFurtherList(new ArrayList<>())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_No6() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal
            ))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(new ArrayList<>())
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
            .sdoFurtherList(new ArrayList<>())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_No5() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal
            ))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(new ArrayList<>())
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .sdoFurtherList(new ArrayList<>())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_No4() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(new ArrayList<>())
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(new ArrayList<>())
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .sdoFurtherList(new ArrayList<>())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_No3() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(new ArrayList<>())
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru))
            .sdoOtherList(new ArrayList<>())
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoLocalAuthorityList(new ArrayList<>())
            .sdoFurtherList(new ArrayList<>())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testCheckStandingOrderOptionsSelected_No2() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(new ArrayList<>())
            .sdoCafcassOrCymruList(new ArrayList<>())
            .sdoOtherList(new ArrayList<>())
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoLocalAuthorityList(new ArrayList<>())
            .sdoFurtherList(new ArrayList<>())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }


    @Test
    public void testCheckStandingOrderOptionsSelected_No1() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(new ArrayList<>())
            .sdoCafcassOrCymruList(new ArrayList<>())
            .sdoOtherList(new ArrayList<>())
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt))
            .sdoHearingsAndNextStepsList(new ArrayList<>())
            .sdoDocumentationAndEvidenceList(new ArrayList<>())
            .sdoLocalAuthorityList(new ArrayList<>())
            .sdoFurtherList(new ArrayList<>())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        assertTrue(DraftAnOrderService.checkStandingOrderOptionsSelected(caseData, new ArrayList<>(), PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testPopulateStandardDirectionOrderFields() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal,
                SdoCourtEnum.crossExaminationEx741,
                SdoCourtEnum.crossExaminationProhibition
            ))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru,
                                           partyToProvideDetailsOnly, partyToProvideDetailsCmyru,
                                           section7Report, safeguardingCafcassOnly, safeguardingCafcassCymru
            ))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt, afterSecondGateKeeping, addNewPreamble))
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
        caseData = CaseData.builder()
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
    public void testPopulateStandardDirectionOrderFields1() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal
            ))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru,
                                           partyToProvideDetailsOnly, partyToProvideDetailsCmyru,
                                           section7Report, safeguardingCafcassOnly, safeguardingCafcassCymru
            ))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt, afterSecondGateKeeping, addNewPreamble))
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
            .sdoFhdraHearingDetails(HearingData.builder()
                                        .additionalHearingDetails("test")
                                        .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
                                        .hearingListedLinkedCases(DynamicList.builder()
                                                                      .listItems(List.of(DynamicListElement
                                                                                             .builder()
                                                                                             .code("123")
                                                                                             .build()))
                                                                      .build())
                                        .build())
            .sdoInstructionsFilingPartiesDynamicList(DynamicList.builder()
                                                         .listItems(List.of(DynamicListElement.builder()
                                                                                .code("asd")
                                                                                .build()))
                                                         .build())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());
        when(hearingDataService.populateHearingDynamicLists(
            Mockito.anyString(),
            Mockito.anyString(),
            any(),
            any(Hearings.class)
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder()
                            .hearingListedLinkedCases(DynamicList.builder()
                                                          .listItems(List.of(DynamicListElement.builder()
                                                                                 .code("123")
                                                                                 .build()))
                                                          .build())
                            .build());
        Map<String, Object> caseDataUpdated = new HashMap<>();

        draftAnOrderService.populateStandardDirectionOrderDefaultFields("test-token", caseData, caseDataUpdated);

        assertEquals(RIGHT_TO_ASK_COURT, caseDataUpdated.get("sdoRightToAskCourt"));
    }



    @Test
    public void testPopulateStandardDirectionOrderFields2() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoCourtList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal
            ))
            .sdoCafcassOrCymruList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru,
                                           partyToProvideDetailsOnly, partyToProvideDetailsCmyru,
                                           section7Report, safeguardingCafcassOnly, safeguardingCafcassCymru
            ))
            .sdoOtherList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesList(List.of(SdoPreamblesEnum.rightToAskCourt, afterSecondGateKeeping, addNewPreamble))
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
            .sdoCourtTempList(List.of(
                SdoCourtEnum.crossExaminationEx740,
                SdoCourtEnum.crossExaminationQualifiedLegal
            ))
            .sdoOtherTempList(List.of(SdoOtherEnum.parentWithCare))
            .sdoPreamblesTempList(List.of(SdoPreamblesEnum.rightToAskCourt, afterSecondGateKeeping, addNewPreamble))
            .sdoLocalAuthorityTempList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .sdoCafcassOrCymruTempList(List.of(SdoCafcassOrCymruEnum.safeguardingCafcassCymru,
                                               partyToProvideDetailsOnly, partyToProvideDetailsCmyru,
                                               section7Report, safeguardingCafcassOnly, safeguardingCafcassCymru
            ))
            .sdoHearingsAndNextStepsTempList(List.of(
                SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping,
                SdoHearingsAndNextStepsEnum.hearingNotNeeded,
                SdoHearingsAndNextStepsEnum.joiningInstructions,
                SdoHearingsAndNextStepsEnum.updateContactDetails
            ))
            .sdoDocumentationAndEvidenceTempList(List.of(
                SdoDocumentationAndEvidenceEnum.specifiedDocuments,
                SdoDocumentationAndEvidenceEnum.spipAttendance
            ))
            .listElementsSetToDefaultValue(Yes)
            .sdoFhdraHearingDetails(HearingData.builder()
                                        .additionalHearingDetails("test")
                                        .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
                                        .hearingListedLinkedCases(DynamicList.builder()
                                                                      .listItems(List.of(DynamicListElement
                                                                                             .builder()
                                                                                             .code("123")
                                                                                             .build()))
                                                                      .build())
                                        .build())
            .sdoInstructionsFilingPartiesDynamicList(DynamicList.builder()
                                                         .listItems(List.of(DynamicListElement.builder()
                                                                                .code("asd")
                                                                                .build()))
                                                         .build())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();

        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());
        when(hearingDataService.populateHearingDynamicLists(
            Mockito.anyString(),
            Mockito.anyString(),
            any(),
            any(Hearings.class)
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder()
                            .hearingListedLinkedCases(DynamicList.builder()
                                                          .listItems(List.of(DynamicListElement.builder()
                                                                                 .code("123")
                                                                                 .build()))
                                                          .build())
                            .build());
        Map<String, Object> caseDataUpdated = new HashMap<>();

        draftAnOrderService.populateStandardDirectionOrderDefaultFields("test-token", caseData, caseDataUpdated);

        assertNotEquals(RIGHT_TO_ASK_COURT, caseDataUpdated.get("sdoRightToAskCourt"));
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
        caseData = CaseData.builder()
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
    public void testPopulateStandardDirectionOrderFieldsWithSdoInstructionsFilingParties() {
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
            .sdoInstructionsFilingPartiesDynamicList(DynamicList.builder().listItems(new ArrayList<>()).build())
            .build();
        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .courtName(SWANSEA_COURT_NAME)
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
            .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
            .sdoTransferApplicationCourtDynamicList(DynamicList.builder().build())
            .sdoUrgentHearingDetails(HearingData.builder().build())
            .sdoPermissionHearingDetails(HearingData.builder().build())
            .sdoSecondHearingDetails(HearingData.builder().build())
            .sdoFhdraHearingDetails(HearingData.builder().build())
            .sdoDraHearingDetails(HearingData.builder().build())
            .sdoSettlementHearingDetails(HearingData.builder().build())
            .sdoDirectionsForFactFindingHearingDetails(
                HearingData.builder()
                    .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                    .build())
            .build();

        caseData = CaseData.builder()
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
        when(objectMapper.convertValue(any(), Mockito.eq(Map.class))).thenReturn(standardDirectionOrderMap);
        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(
            Mockito.anyString(),
            Mockito.anyString(),
            any(),
            any(Hearings.class)
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(hearingDataService.getHearingDataForSdo(any(), any(), any())).thenReturn(HearingData.builder().build());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        Map<String, Object> caseDataMap = draftAnOrderService.populateStandardDirectionOrder(
            "test-token",
            caseData,
            true,
            null,
            null
        );
        assertEquals(1, ((List<Element<DraftOrder>>) caseDataMap.get("sdoDisclosureOfPapersCaseNumbers")).size());
        //assertNotNull(caseDataUpdated.get("sdoRightToAskCourt"));
    }


    @Test
    public void testPopulateStandardDirectionOrderTestScenario2() throws JsonProcessingException {
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
            .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
            .sdoTransferApplicationCourtDynamicList(DynamicList.builder().build())
            .sdoUrgentHearingDetails(HearingData.builder().build())
            .sdoPermissionHearingDetails(HearingData.builder().build())
            .sdoSecondHearingDetails(HearingData.builder().build())
            .sdoFhdraHearingDetails(HearingData.builder().build())
            .sdoDraHearingDetails(HearingData.builder().build())
            .sdoSettlementHearingDetails(HearingData.builder().build())
            .editedOrderHasDefaultCaseFields(Yes)
            .sdoDirectionsForFactFindingHearingDetails(
                HearingData.builder()
                    .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                    .build())
            .build();

        caseData = CaseData.builder()
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
        when(objectMapper.convertValue(any(), Mockito.eq(Map.class))).thenReturn(standardDirectionOrderMap);
        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(
            Mockito.anyString(),
            Mockito.anyString(),
            any(),
            any(Hearings.class)
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(hearingDataService.getHearingDataForSdo(any(), any(), any())).thenReturn(HearingData.builder().build());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        Map<String, Object> caseDataMap = draftAnOrderService.populateStandardDirectionOrder(
            "test-token",
            caseData,
            true,
            null,
            null
        );
        assertEquals(1, ((List<Element<DraftOrder>>) caseDataMap.get("sdoDisclosureOfPapersCaseNumbers")).size());
        //assertNotNull(caseDataUpdated.get("sdoRightToAskCourt"));
    }


    @Test
    public void testPopulateStandardDirectionOrderTestScenario3() throws JsonProcessingException {
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
            .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
            .sdoTransferApplicationCourtDynamicList(DynamicList.builder().build())
            .sdoUrgentHearingDetails(HearingData.builder().build())
            .sdoPermissionHearingDetails(HearingData.builder().build())
            .sdoSecondHearingDetails(HearingData.builder().build())
            .sdoFhdraHearingDetails(HearingData.builder().build())
            .sdoDraHearingDetails(HearingData.builder().build())
            .sdoSettlementHearingDetails(HearingData.builder().build())
            .editedOrderHasDefaultCaseFields(Yes)
            .sdoDirectionsForFactFindingHearingDetails(
                HearingData.builder()
                    .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                    .build())
            .build();

        caseData = CaseData.builder()
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
        when(objectMapper.convertValue(any(), Mockito.eq(Map.class))).thenReturn(standardDirectionOrderMap);
        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(
            Mockito.anyString(),
            Mockito.anyString(),
            any(),
            any(Hearings.class)
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(hearingDataService.getHearingDataForSdo(any(), any(), any())).thenReturn(HearingData.builder().build());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        Map<String, Object> caseDataMap = draftAnOrderService.populateStandardDirectionOrder(
            "test-token",
            caseData,
            false,
            null,
            ADMIN_EDIT_AND_APPROVE_ORDER.getId()
        );
        assertEquals(1, ((List<Element<DraftOrder>>) caseDataMap.get("sdoDisclosureOfPapersCaseNumbers")).size());
        //assertNotNull(caseDataUpdated.get("sdoRightToAskCourt"));
    }

    @Test
    public void testPopulateStandardDirectionOrderTestWithExp() throws JsonProcessingException {
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

        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .draftOrderCollection(draftOrderCollection)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        SdoDetails sdoDetails = SdoDetails.builder().build();
        String sdoDetailsJson = null;
        Map<String, Object> standardDirectionOrderMap = new HashMap<>();
        when(draftAnOrderService.copyPropertiesToStandardDirectionOrder(sdoDetails)).thenReturn(standardDirectionOrder);
        when(objectMapper.readValue(
            sdoDetailsJson,
            StandardDirectionOrder.class
        )).thenThrow(JsonProcessingException.class);
        when(objectMapper.convertValue(standardDirectionOrder, Map.class)).thenReturn(standardDirectionOrderMap);
        when(locationRefDataService.getCourtLocations("test-token")).thenReturn(new ArrayList<>());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(partiesListGenerator.buildPartiesList(
            caseData,
            new ArrayList<>()
        )).thenReturn(DynamicList.builder().build());

        assertExpectedException(() -> {
            draftAnOrderService.populateStandardDirectionOrder("test-token", caseData, false, null, ADMIN_EDIT_AND_APPROVE_ORDER.getId());
        }, ManageOrderRuntimeException.class, "Failed to parse SDO order details");
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
        caseData = CaseData.builder()
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
        caseData = CaseData.builder()
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
                DioHearingsAndNextStepsEnum.updateContactDetails,
                DioHearingsAndNextStepsEnum.participationDirections,
                DioHearingsAndNextStepsEnum.permissionHearing,
                DioHearingsAndNextStepsEnum.positionStatement
            ))
            .dioLocalAuthorityList(List.of(DioLocalAuthorityEnum.localAuthorityLetter))
            .build();

        caseData = CaseData.builder()
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

        caseData = CaseData.builder()
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
        caseData = CaseData.builder()
            .id(12345L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        assertNull(draftAnOrderService.populateCustomFields(caseData));

    }

    @Test
    public void testPopulateCustomFieldsNonMolestation() {
        caseData = CaseData.builder()
            .id(12345L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .build();

        assertNull(draftAnOrderService.populateCustomFields(caseData));
    }

    @Test
    public void testPopulateCustomFieldsAppointmentOfGuardian() {
        caseData = CaseData.builder()
            .id(12345L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .build();

        assertEquals(caseData, draftAnOrderService.populateCustomFields(caseData));

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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .previewOrderDocWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
                              .emailInformationDA(List.of(emailInformationElement))
                              .postalInformationDA(List.of(postalInformationElement))
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            EDIT_AND_APPROVE_ORDER.getId(),
            PrlAppsConstants.ENGLISH
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


        caseData = CaseData.builder()
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
                              .isTheOrderByConsent(Yes)
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
            .wasTheOrderApprovedAtHearing(No)
            .build();

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        List<Element<DraftOrder>> result = draftAnOrderService.generateDraftOrderCollection(caseData, "auth-token");
        assertNotNull(result);
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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = draftAnOrderService.getDraftOrderInfo(authToken, caseData, draftOrder);
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testGetDraftOrderInfoWithHearingData() throws Exception {

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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().build()))).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = draftAnOrderService.getDraftOrderInfo(authToken, caseData, draftOrder);
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testJudgeOrAdminEditApproveDraftOrderMidEvent() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
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
        caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(stringObjectMap);
        stringObjectMap = draftAnOrderService.getEligibleServeOrderDetails(authToken, callbackRequest, PrlAppsConstants.ENGLISH);
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testJudgeOrAdminEditApproveDraftOrderAboutToSubmit() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
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
        caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(stringObjectMap).build())
            .eventId("adminEditAndApproveAnOrder")
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(stringObjectMap);
        stringObjectMap = draftAnOrderService.adminEditAndServeAboutToSubmit(
            authToken,
            callbackRequest,
            PrlAppsConstants.ENGLISH
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
        caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
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
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(stringObjectMap);
        stringObjectMap = draftAnOrderService.adminEditAndServeAboutToSubmit(
            authToken,
            callbackRequest,
            PrlAppsConstants.ENGLISH
        );
        assertNotNull(stringObjectMap);
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

    @Test
    public void testExceptionForPopulateSdoFields() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .build();

        DraftOrder draftOrder1 = DraftOrder.builder()
            .build();

        DraftOrder draftOrder2 = DraftOrder.builder()
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        Element<DraftOrder> draftOrderElement1 = element(draftOrder1);
        Element<DraftOrder> draftOrderElement2 = element(draftOrder2);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        draftOrderCollection.add(draftOrderElement1);
        draftOrderCollection.add(draftOrderElement2);

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .draftOrderCollection(draftOrderCollection)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        SdoDetails sdoDetails = SdoDetails.builder().build();
        String sdoDetailsJson = null;
        Map<String, Object> standardDirectionOrderMap = new HashMap<>();
        when(draftAnOrderService.copyPropertiesToStandardDirectionOrder(sdoDetails)).thenThrow(JsonProcessingException.class);

        assertExpectedException(() -> {
            draftAnOrderService.populateStandardDirectionOrder("test-token", caseData, true, null, ADMIN_EDIT_AND_APPROVE_ORDER.getId());
        }, UnsupportedOperationException.class, "Could not find order");
    }

    @Test
    public void testPopulateDraftOrderFieldsWhenDraftAnOrderForC100() throws Exception {
        caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderByConsent(Yes)
                              .recitalsOrPreamble("test recitals")
                              .orderDirections("test orders")
                              .furtherDirectionsIfRequired("test further directions")
                              .c21OrderOptions(C21OrderOptionsEnum.c21other)
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .isTheOrderAboutChildren(Yes)
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().label(
                                                   "John (Child 1)").build())).build()
                              )
                              .cafcassCymruEmail("test@test.com")
                              .build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoAfterSecondGatekeeping("test")
                                        .sdoAllocateOrReserveJudge(AllocateOrReserveJudgeEnum.allocatedTo)
                                        .build())
            .selectedOrder("ABC")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        )).thenReturn(caseData);
        when(manageOrderService.populateCustomOrderFields(any(), Mockito.any(), Mockito.any())).thenReturn(caseData);
        Assert.assertEquals(
            stringObjectMap,
            draftAnOrderService.handlePopulateDraftOrderFields(callbackRequest, authToken, null, PrlAppsConstants.ENGLISH)
        );
    }

    @Test
    public void testPopulateDraftOrderFieldsWhenDraftAnOrderForFL401() throws Exception {
        caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantCaseName("Jo Davis & Jon Smith")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.powerOfArrest)
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderByConsent(Yes)
                              .recitalsOrPreamble("test recitals")
                              .orderDirections("test orders")
                              .furtherDirectionsIfRequired("test further directions")
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .isTheOrderAboutChildren(Yes)
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().label(
                                                   "John (Child 1)").build())).build()
                              )
                              .cafcassCymruEmail("test@test.com")
                              .build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoAfterSecondGatekeeping("test")
                                        .sdoAllocateOrReserveJudge(AllocateOrReserveJudgeEnum.allocatedTo)
                                        .build())
            .selectedOrder("ABC")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        )).thenReturn(caseData);
        when(manageOrderService.populateCustomOrderFields(any(), Mockito.any(), Mockito.any())).thenReturn(caseData);
        Assert.assertEquals(
            stringObjectMap,
            draftAnOrderService.handlePopulateDraftOrderFields(callbackRequest, authToken, null, PrlAppsConstants.ENGLISH)
        );
    }

    @Test
    public void testPopulateDraftOrderFieldsWhenUploadAnOrder() throws Exception {
        caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .standardDirectionOrder(StandardDirectionOrder.builder().sdoCourtList(new ArrayList<>()).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        )).thenReturn(caseData);
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(
            Mockito.anyString(),
            Mockito.anyString(),
            any(),
            any(Hearings.class)
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(manageOrderService.populateCustomOrderFields(any(), Mockito.any(), Mockito.any())).thenReturn(caseData);
        stringObjectMap.putAll(manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        ));
        Assert.assertEquals(
            stringObjectMap,
            draftAnOrderService.handlePopulateDraftOrderFields(callbackRequest, authToken, null, PrlAppsConstants.ENGLISH)
        );
    }

    @Test
    public void testPopulateDraftOrderFieldsWhenCreateAnOrderFOrFl401Case() throws Exception {
        caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .selectedOrder(TEST_UUID)
            .standardDirectionOrder(StandardDirectionOrder.builder().sdoCourtList(new ArrayList<>()).build())
            .manageOrders(ManageOrders.builder()
                              .isTheOrderByConsent(Yes)
                              .recitalsOrPreamble("test recitals")
                              .orderDirections("test orders")
                              .furtherDirectionsIfRequired("test further directions")
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .isTheOrderAboutChildren(Yes)
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().label(
                                                   "John (Child 1)").build())).build()
                              )
                              .cafcassCymruEmail("test@test.com")
                              .build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoAfterSecondGatekeeping("test")
                                        .sdoAllocateOrReserveJudge(AllocateOrReserveJudgeEnum.reservedTo)
                                        .build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        )).thenReturn(caseData);
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(
            Mockito.anyString(),
            Mockito.anyString(),
            any(),
            any(Hearings.class)
        ))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(manageOrderService.populateCustomOrderFields(any(), Mockito.any(), Mockito.any())).thenReturn(caseData);
        stringObjectMap.putAll(manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        ));
        Assert.assertEquals(
            stringObjectMap,
            draftAnOrderService.handlePopulateDraftOrderFields(callbackRequest, authToken, null, PrlAppsConstants.ENGLISH)
        );
    }


    @Test
    public void testPrepareDraftOrderCollection() throws Exception {
        caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        doReturn(caseData).when(objectMapper).convertValue(
            caseData,
            CaseData.class
        );
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        when(manageOrderService.getLoggedInUserType("auth-token")).thenReturn("Solicitor");
        when(manageOrderService.getCurrentUploadDraftOrderDetails(Mockito.any(CaseData.class),Mockito.anyString(),
                                                                  Mockito.any(UserDetails.class)))
            .thenReturn(DraftOrder.builder().orderTypeId("abc").build());
        Map<String, Object> response = draftAnOrderService.prepareDraftOrderCollection(authToken,callbackRequest);
        Assert.assertEquals(
            stringObjectMap.get("applicantCaseName"),
            response.get("applicantCaseName")
        );
        Assert.assertNotNull(response.get("draftOrderCollection"));
        Assert.assertEquals(1, ((List<Element<DraftOrder>>) response.get("draftOrderCollection")).size());


    }

    @Test
    public void testPrepareDraftOrderCollectionWithHearingPage() throws Exception {
        caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .caseTypeOfApplication("FL401")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .manageOrders(ManageOrders.builder().c21OrderOptions(C21OrderOptionsEnum.c21other).build())
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap)
                .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        doReturn(caseData).when(objectMapper).convertValue(
            caseData,
            CaseData.class
        );
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        when(manageOrderService.getLoggedInUserType("auth-token")).thenReturn("Solicitor");
        when(manageOrderService.getCurrentUploadDraftOrderDetails(Mockito.any(CaseData.class),Mockito.anyString(),
            Mockito.any(UserDetails.class)))
            .thenReturn(DraftOrder.builder().orderTypeId("abc").build());
        Map<String, Object> response = draftAnOrderService.prepareDraftOrderCollection(authToken,callbackRequest);
        Assert.assertEquals(
            stringObjectMap.get("applicantCaseName"),
            response.get("applicantCaseName")
        );
        Assert.assertNotNull(response.get("draftOrderCollection"));
        Assert.assertEquals(1, ((List<Element<DraftOrder>>) response.get("draftOrderCollection")).size());
    }

    @Test
    public void testSelectedOrderForUploadScenario() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getSelectedOrderInfoForUpload(caseData)).thenReturn("Test order");
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.WELSH
        );
        assertEquals("Test order", response.getData().get("selectedOrder"));

    }

    @Test
    public void testSelectedOrdersForUploadScenario() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
            .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder()
                .isTheOrderAboutChildren(Yes)
                .build())
            .childArrangementOrders(ChildArrangementOrdersEnum.authorityC31)
            .domesticAbuseOrders(DomesticAbuseOrdersEnum.amendedDischargedVariedOrder)
            .fcOrders(FcOrdersEnum.contemptNotice)
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap)
                .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getSelectedOrderInfoForUpload(caseData)).thenReturn("Test order");
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.ENGLISH
        );
        assertEquals("Test order", response.getData().get("selectedOrder"));
        //assertEquals(1, response.getData().get("children").size());

    }

    @Test
    public void testSelectedOrderForUploadScenarioStandardDirection() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
            .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder()
                .isTheOrderAboutChildren(Yes)
                .build())
            .childArrangementOrders(ChildArrangementOrdersEnum.standardDirectionsOrder)
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap)
                .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getSelectedOrderInfoForUpload(caseData)).thenReturn("Test order");
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.ENGLISH
        );
        assertEquals("This order is not available to be drafted", response.getErrors().get(0));
        //assertEquals(1, response.getData().get("children").size());

    }

    @Test
    public void testSelectedOrderForUploadScenarioStandardDirectionWelsh() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
            .value(Child.builder().build()).build());
        CaseData caseData2 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder()
                .isTheOrderAboutChildren(Yes)
                .build())
            .childArrangementOrders(ChildArrangementOrdersEnum.standardDirectionsOrder)
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData2.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap)
                .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData2);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData2)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData2);
        when(manageOrderService.getSelectedOrderInfoForUpload(caseData2)).thenReturn("Test order");
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.WELSH
        );
        assertEquals("Nid yw’r gorchymyn hwn ar gael i’w drafftio", response.getErrors().get(0));
        //assertEquals(1, response.getData().get("children").size());

    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenario() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.ENGLISH
        );
        assertEquals(
            BOLD_BEGIN + "Child arrangements, specific issue or prohibited steps order (C43)" + BOLD_END,
            response.getData().get("selectedOrder")
        );
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioWelsh() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
            .value(Child.builder().build()).build());
        CaseData caseData2 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .manageOrders(ManageOrders.builder()
                .isTheOrderAboutChildren(Yes)
                .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData2.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap)
                .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData2);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData2)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData2);
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.WELSH
        );
        assertEquals(
            BOLD_BEGIN + "Gorchymyn Trefniadau Plant, Mater Penodol neu Gamau Gwaharddedig (C43)" + BOLD_END,
            response.getData().get("selectedOrder")
        );
        //assertEquals(1, response.getData().getChildren().size());
    }

    @Test
    public void testSelectedOrderForDraftAnOrderC21Scenario() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .manageOrders(ManageOrders.builder()
                              .c21OrderOptions(C21OrderOptionsEnum.c21other)
                              .typeOfC21Order("C21 - other")
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.ENGLISH
        );
        assertEquals(
            BOLD_BEGIN + "Child arrangements, specific issue or prohibited steps order (C43)" + BOLD_END,
            response.getData().get("selectedOrder")
        );
        //assertEquals(1, response.getData().getChildren().size());
    }


    @Test
    public void testSelectedOrderForDraftAnOrderScenarioBlankOrderAndDirections() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioParentalResponsability() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.parentalResponsibility)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.WELSH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioSpecialGuardianship() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioParties() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioNonParties() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioAppointGuardian() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioNoCaseType() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioNonMolestation() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioOccupation() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioAmendDischargedVaried() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.amendDischargedVaried)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioBlank() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioPowerOfArrest() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.powerOfArrest)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioGeneralForm() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.generalForm)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioFL401ThrowsError() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.transferOfCaseToAnotherCourt)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.ENGLISH
        );
        assertEquals(1, response.getErrors().size());
        assertEquals(ORDER_NOT_AVAILABLE_FL401, response.getErrors().get(0));
    }

    @Test
    public void testSelectedOrderForDraftAnOrderScenarioFL401ThrowsErrorWelsh() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
            .value(Child.builder().build()).build());
        CaseData caseData2 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.transferOfCaseToAnotherCourt)
            .manageOrders(ManageOrders.builder()
                .isTheOrderAboutChildren(Yes)
                .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData2.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap)
                .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData2);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData2)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData2);
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.WELSH
        );
        assertEquals(1, response.getErrors().size());
        assertEquals(ORDER_NOT_AVAILABLE_FL401_WELSH, response.getErrors().get(0));
    }

    @Test
    public void testSelectedOrderForSdoDraftAnOrderScenario() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.ENGLISH
        );
        assertEquals(1, response.getErrors().size());
        assertEquals("This order is not available to be drafted", response.getErrors().get(0));

    }

    @Test
    public void testSelectedOrderForDioDraftAnOrderScenario() throws Exception {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .children(children)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
            callbackRequest,
            authToken,
            PrlAppsConstants.ENGLISH
        );
        assertEquals(1, response.getErrors().size());
        assertEquals("This order is not available to be drafted", response.getErrors().get(0));

    }

    public void testRemoveDraftOrderAndAddToFinalOrderDaForApplicantSolicitorWithExp() {
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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .previewOrderDocWelsh(Document.builder().documentFileName("abc-welsh.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
                              .emailInformationDA(List.of(emailInformationElement))
                              .postalInformationDA(List.of(postalInformationElement))
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.getLoggedInUserType("auth-token")).thenReturn("Solicitor");
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(null);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "testevent",
            PrlAppsConstants.ENGLISH
        );
        assertEquals(2, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderWithEditYesForSdo() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .approvalDate(LocalDate.now())
            .dateOrderMade(LocalDate.now())
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .isOrderUploadedByJudgeOrAdmin(No)
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicants(List.of(applicants))
            .selectTypeOfOrder(SelectTypeOfOrderEnum.general)
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(FINAL_TEMPLATE_WELSH, "");
        when(documentLanguageService.docGenerateLang(any())).thenReturn(DocumentLanguage.builder()
                                                                            .isGenWelsh(true).isGenEng(false).build());
        when(manageOrderService.getOrderTemplateAndFile(any())).thenReturn(fieldMap);
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        Hearings hearings = Hearings.hearingsWith().build();
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(hearings);
        when(manageOrderService.setHearingDataForSdo(caseData, hearings, "test token")).thenReturn(caseData);
        Map<String, Object> caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "eventId",
            PrlAppsConstants.ENGLISH
        );

        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
    }

    @Test
    public void testUpdateDraftOrderCollectionForSdo() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .isOrderCreatedBySolicitor(Yes)
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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .doYouWantToEditTheOrder(Yes)
            .manageOrders(ManageOrders.builder()
                              .whatToDoWithOrderSolicitor(OrderApprovalDecisionsForSolicitorOrderEnum.editTheOrderAndServe)
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).build())
            .respondents(List.of(respondents))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        Hearings hearings = Hearings.hearingsWith().build();
        when(hearingService.getHearings(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(hearings);
        when(manageOrderService.setHearingDataForSdo(caseData, hearings, authToken)).thenReturn(caseData);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            Event.EDIT_AND_APPROVE_ORDER.getId(),
            null
        );

        assertEquals(
            JudgeOrMagistrateTitleEnum.districtJudge,
            ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).get(0).getValue().getJudgeOrMagistrateTitle()
        );
    }

    @Test
    public void testGenerateOrderDocumentPostValidations() throws Exception {
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


        caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .children(children)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).c21OrderOptions(
                    C21OrderOptionsEnum.c21NoOrderMade)
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
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

        stringObjectMap = draftAnOrderService.generateOrderDocumentPostValidations(authToken,
                                                                                   callbackRequest,
                                                                                   List.of(element(HearingData.builder().build())),
                                                                                   false,
                                                                                   CreateSelectOrderOptionsEnum.standardDirectionsOrder,
                                                                                   null,
            PrlAppsConstants.ENGLISH
        );
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testGenerateOrderDocumentPostValidationsTrue() throws Exception {
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


        caseData = CaseData.builder()
            .id(12345L)
            .courtName(SWANSEA_COURT_NAME)
            .caseTypeOfApplication("C100")
            .children(children)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge).c21OrderOptions(
                    C21OrderOptionsEnum.c21NoOrderMade)
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .respondents(List.of(respondents))
            .draftOrderCollection(draftOrderCollection)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
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

        stringObjectMap = draftAnOrderService.generateOrderDocumentPostValidations(authToken,
                                                                                   callbackRequest,
                                                                                   List.of(element(HearingData.builder().build())),
                                                                                   true,
                                                                                   CreateSelectOrderOptionsEnum.standardDirectionsOrder,
                                                                                   null,
            PrlAppsConstants.WELSH
        );
        assertNotNull(stringObjectMap);
    }

    @Test
    public void testPopulateDraftOrderHearingFieldsWhenDraftAnOrder() throws Exception {
        caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantCaseName("Jo Davis & Jon Smith")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderByConsent(Yes)
                              .recitalsOrPreamble("test recitals")
                              .orderDirections("test orders")
                              .furtherDirectionsIfRequired("test further directions")
                              .furtherInformationIfRequired("test further information")
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
                              .isTheOrderAboutChildren(Yes)
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().label(
                                                   "John (Child 1)").build())).build()
                              )
                              .cafcassCymruEmail("test@test.com")
                              .build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoAfterSecondGatekeeping("test")
                                        .sdoAllocateOrReserveJudge(AllocateOrReserveJudgeEnum.allocatedTo)
                                        .build())
            .selectedOrder("ABC")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        )).thenReturn(caseData);
        when(manageOrderService.populateCustomOrderFields(any(), Mockito.any(), Mockito.any())).thenReturn(caseData);

        Map<String, Object> caseDataMap = draftAnOrderService.handlePopulateDraftOrderFields(
            callbackRequest,
            authToken,
            null,
            PrlAppsConstants.ENGLISH
        );

        Assert.assertEquals(stringObjectMap, caseDataMap);
    }


    @Test
    public void testOrderNameForWaFieldInDraftOrderJourney() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime nowTime = LocalDateTime.parse(dtf.format(LocalDateTime.now()));
        DraftOrder draftOrder = DraftOrder.builder()
            .typeOfOrder(ChildArrangementOrdersEnum.declarationOfParentageOrder.getDisplayedValue())
            .orderTypeId(ChildArrangementOrdersEnum.declarationOfParentageOrder.getDisplayedValue())
            .manageOrderHearingDetails(List.of(element(HearingData.builder()

                                                           .confirmedHearingDates(DynamicList.builder().build()).build())))
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test title")
                              .dateCreated(nowTime)
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString(
            "ecc87361-d2bb-4400-a910-e5754888385b"))
            .value(draftOrder).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.parentalResponsibility)
            .draftOrdersDynamicList(DynamicList.builder().value(DynamicListElement.builder().code(TEST_UUID).build()).build())
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        String name = draftAnOrderService.getDraftOrderNameForWA(caseData, Event.DRAFT_AN_ORDER.getId(), null);
        assertNotNull(name);
        assertTrue(name.contains(CreateSelectOrderOptionsEnum.parentalResponsibility.getDisplayedValue()));
        assertTrue(name.contains(nowTime.format(DateTimeFormatter.ofPattern(
            PrlAppsConstants.D_MMM_YYYY_HH_MM,
            Locale.ENGLISH
        ))));
    }


    @Test
    public void testOrderNameForWaFieldInDraftUploadOrderJourney() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime nowTime = LocalDateTime.parse(dtf.format(LocalDateTime.now()));
        DraftOrder draftOrder = DraftOrder.builder()
            .typeOfOrder(ChildArrangementOrdersEnum.declarationOfParentageOrder.getDisplayedValue())
            .orderTypeId(ChildArrangementOrdersEnum.declarationOfParentageOrder.getDisplayedValue())
            .manageOrderHearingDetails(List.of(element(HearingData.builder()

                                                           .confirmedHearingDates(DynamicList.builder().build()).build())))
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test title")
                              .dateCreated(nowTime)
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString(
            "ecc87361-d2bb-4400-a910-e5754888385b"))
            .value(draftOrder).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .childArrangementOrders(ChildArrangementOrdersEnum.declarationOfParentageOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.parentalResponsibility)
            .draftOrdersDynamicList(DynamicList.builder().value(DynamicListElement.builder().code(TEST_UUID).build()).build())
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.getSelectedOrderInfoForUpload(
            caseData)).thenReturn(ChildArrangementOrdersEnum.declarationOfParentageOrder.getDisplayedValue());
        String name = draftAnOrderService.getDraftOrderNameForWA(caseData, Event.DRAFT_AN_ORDER.getId(), null);
        assertNotNull(name);
        assertTrue(name.contains(ChildArrangementOrdersEnum.declarationOfParentageOrder.getDisplayedValue()));
        assertTrue(name.contains(nowTime.format(DateTimeFormatter.ofPattern(
            PrlAppsConstants.D_MMM_YYYY_HH_MM,
            Locale.ENGLISH
        ))));
    }

    @Test
    public void testOrderNameForWaFieldInReturnOrderJourney() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime nowTime = LocalDateTime.parse(dtf.format(LocalDateTime.now()));
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .orderTypeId(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder.getDisplayedValue())
            .manageOrderHearingDetails(List.of(element(HearingData.builder()

                                                           .confirmedHearingDates(DynamicList.builder().build()).build())))
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test title")
                              .dateCreated(nowTime)
                              .build())
            .build();

        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString(TEST_UUID))
            .value(draftOrder).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder().build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(Mockito.any(), Mockito.any())).thenReturn(UUID.fromString(
            TEST_UUID));
        String name = draftAnOrderService.getDraftOrderNameForWA(caseData, EDIT_RETURNED_ORDER.getId(), null);
        assertNotNull(name);
        assertTrue(name.contains(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder.getDisplayedValue()));
        assertTrue(name.contains(nowTime.format(DateTimeFormatter.ofPattern(
            PrlAppsConstants.D_MMM_YYYY_HH_MM,
            Locale.ENGLISH
        ))));
    }


    @Test
    public void testHandleDocumentGenerationDraftOrder() throws Exception {
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.parentalResponsibility)
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .build())
            .draftOrdersDynamicList(DynamicList.builder().value(DynamicListElement.builder().code(TEST_UUID).build()).build())
            .draftOrderCollection(List.of(element(UUID.fromString(TEST_UUID), DraftOrder.builder().build())))
            .doYouWantToEditTheOrder(No)
            .build();
        when(elementUtils.getDynamicListSelectedValue(Mockito.any(), Mockito.any())).thenReturn(UUID.fromString(
            TEST_UUID));
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.DRAFT_AN_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testHandleDocumentGenerationDraftOrderWithError() throws Exception {
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .c21OrderOptions(C21OrderOptionsEnum.c21other)
                              .hasJudgeProvidedHearingDetails(Yes)
                              .build())
            .draftOrdersDynamicList(DynamicList.builder().value(DynamicListElement.builder().code(TEST_UUID).build()).build())
            .draftOrderCollection(List.of(element(UUID.fromString(TEST_UUID), DraftOrder.builder().build())))
            .doYouWantToEditTheOrder(No)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.DRAFT_AN_ORDER.getId())
            .build();
        when(elementUtils.getDynamicListSelectedValue(Mockito.any(), Mockito.any())).thenReturn(UUID.fromString(
            TEST_UUID));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testHandleDocumentGenerationDraftOrderWithOccupation() throws Exception {
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren(Yes)
                              .c21OrderOptions(C21OrderOptionsEnum.c21other)
                              .hasJudgeProvidedHearingDetails(Yes)
                              .fl404CustomFields(FL404.builder().fl404bAddMoreDetails("more").build())
                              .build())
            .draftOrdersDynamicList(TEST_UUID)
            .doYouWantToEditTheOrder(No)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(Event.DRAFT_AN_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }


    @Test
    public void testHandleDocumentGenerationEditOrder() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder().build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(EDIT_AND_APPROVE_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testGetDraftOrderDynamicListForManager() {
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(ElementUtils.element(
            caseData.getDraftOrderCollection().get(0).getId(),
            caseData.getDraftOrderCollection().get(0).getValue().toBuilder().otherDetails(OtherDraftOrderDetails.builder()
                                                                                              .dateCreated(LocalDateTime.now())
                                                                                              .createdBy("test title")
                                                                                              .isJudgeApprovalNeeded(No)
                                                                                              .reviewRequiredBy(
                                                                                                  AmendOrderCheckEnum
                                                                                                      .managerCheck)
                                                                                              .status(OrderStatusEnum.createdByCA
                                                                                                          .getDisplayedValue())
                                                                                              .build()).build()
        ));

        CaseData updatedCaseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .isCafcass(Yes)
            .build();
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(updatedCaseData)).thenReturn("test@test.com");
        when(manageOrderService.getLoggedInUserType(authToken)).thenReturn(UserRoles.COURT_ADMIN.name());

        Map<String, Object> caseDataMap = draftAnOrderService.getDraftOrderDynamicList(
            updatedCaseData,
            EDIT_AND_APPROVE_ORDER.getId(),
            clientContextCoded,
            authToken
        );
        assertEquals("C100", caseDataMap.get(CASE_TYPE_OF_APPLICATION));
        assertNotNull(caseDataMap.get("draftOrdersDynamicList"));
    }

    @Test
    public void testHandleDocumentGenerationYesToEditOrder() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
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
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder().fl404CustomFields(FL404.builder().fl404bApplicantName("test").build()).build())
            .doYouWantToEditTheOrder(Yes)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(EDIT_AND_APPROVE_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testHandleDocumentGenerationEditOrderWithoutError() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test")
                              .build())
            .isOrderCreatedBySolicitor(Yes)
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder()
                              .fl404CustomFields(FL404.builder()
                                                     .fl404bApplicantName("test")
                                                     .build())
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .doYouWantToEditTheOrder(Yes)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(EDIT_AND_APPROVE_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testHandleDocumentGenerationEditOrderWithHearing() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test")
                              .build())
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .build();

        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder()
                              .fl404CustomFields(FL404.builder()
                                                     .fl404bApplicantName("test")
                                                     .build())
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .doYouWantToEditTheOrder(Yes)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(EDIT_AND_APPROVE_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testSelectedOrderForC100ProhibitedOrdersScenario() throws Exception {

        CreateSelectOrderOptionsEnum[] prohibitedC100OrderIdsForSolicitors = {CreateSelectOrderOptionsEnum.noticeOfProceedingsParties,
            CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties};

        for (CreateSelectOrderOptionsEnum orderId : prohibitedC100OrderIdsForSolicitors) {

            List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                        .value(Child.builder().build()).build());
            caseData = CaseData.builder()
                .id(12345L)
                .caseTypeOfApplication("C100")
                .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
                .children(children)
                .createSelectOrderOptions(orderId)
                .manageOrders(ManageOrders.builder()
                                  .isTheOrderAboutChildren(Yes)
                                  .build())
                .selectedOrder("Test order")
                .build();
            Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                 .data(stringObjectMap)
                                 .build())
                .build();
            List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
                .getChildrenMultiSelectList(caseData);
            when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
            when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
            AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
                callbackRequest,
                authToken,
                PrlAppsConstants.ENGLISH
            );
            assertEquals(1, response.getErrors().size());
            assertEquals("This order is not available to be drafted", response.getErrors().get(0));

        }

    }

    @Test
    public void testSelectedOrderForFl402OrdersForBothCAandDAcasesScenario() {

        String[] caseTypes = {"C100","FL401"};

        for (String caseType : caseTypes) {
            List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                        .value(Child.builder().build()).build());
            caseData = CaseData.builder()
                .id(12345L)
                .caseTypeOfApplication(caseType)
                .draftOrderOptions(DraftOrderOptionsEnum.draftAnOrder)
                .children(children)
                .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
                .manageOrders(ManageOrders.builder()
                                  .isTheOrderAboutChildren(Yes)
                                  .build())
                .selectedOrder("Test order")
                .build();
            Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                 .data(stringObjectMap)
                                 .build())
                .build();
            List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
                .getChildrenMultiSelectList(caseData);
            when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
            when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
            AboutToStartOrSubmitCallbackResponse response = draftAnOrderService.handleSelectedOrder(
                callbackRequest,
                authToken,
                PrlAppsConstants.ENGLISH
            );
            assertEquals(1, response.getErrors().size());
            assertEquals("This order is not available to be drafted", response.getErrors().get(0));
        }
    }

    @Test
    public void testValidationIfDirectionForFactFindingSelectedScenario1Welsh() {
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        PartyDetails details1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement1 = element(details1);
        partyDetails.add(partyDetailsElement1);

        CaseData caseData2 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(partyDetails)
            .respondents(partyDetails)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
                                        .build())
            .doYouWantToEditTheOrder(No)
            .build();
        List<String> errorList = new ArrayList<>();
        assertFalse(draftAnOrderService.validationIfDirectionForFactFindingSelected(caseData2, errorList, PrlAppsConstants.WELSH));
    }

    @Test
    public void testValidationIfDirectionForFactFindingSelectedScenario1() throws Exception {
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        PartyDetails details1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement1 = element(details1);
        partyDetails.add(partyDetailsElement1);

        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(partyDetails)
            .respondents(partyDetails)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
                .build())
            .doYouWantToEditTheOrder(No)
            .build();
        List<String> errorList = new ArrayList<>();
        assertFalse(draftAnOrderService.validationIfDirectionForFactFindingSelected(caseData, errorList, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testValidationIfDirectionForFactFindingSelectedScenario2() throws Exception {
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);

        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(partyDetails)
            .respondents(partyDetails)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
                                        .build())
            .doYouWantToEditTheOrder(No)
            .build();
        List<String> errorList = new ArrayList<>();
        assertTrue(draftAnOrderService.validationIfDirectionForFactFindingSelected(caseData, errorList, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderAndUpdateCaseDataForDocmosisC100() {

        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .dateOrderMade(LocalDate.parse("2022-02-16"))
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .address(Address.builder().addressLine1("test").country("test").postCode("test").build())
            .dateOfBirth(LocalDate.now())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        Element<PartyDetails> respondents = element(partyDetails);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        List<Element<OrderDetails>> elementList = new ArrayList<>();
        elementList.add(element(OrderDetails.builder().dateCreated(LocalDateTime.now()).build()));
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .orderCollection(elementList)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicants(List.of(applicants))
            .respondents(List.of(respondents))
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .fl404CustomFields(FL404.builder().build())
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "editReturnedOrder",
            PrlAppsConstants.ENGLISH
        );
        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
        assertNotNull(((List<Element<DraftOrder>>) caseDataMap.get("orderCollection")));
        assertEquals("C100", caseDataMap.get("caseTypeOfApplication"));
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderAndUpdateCaseDataForDocmosisFl401() {

        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .dateOrderMade(LocalDate.parse("2022-02-16"))
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.generalForm)
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> applicants = element(partyDetails);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        List<Element<OrderDetails>> elementList = new ArrayList<>();
        elementList.add(element(OrderDetails.builder().dateCreated(LocalDateTime.now()).build()));
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .orderCollection(elementList)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicantsFL401(PartyDetails.builder().firstName("test").lastName("test").build())
            .respondentsFL401(PartyDetails.builder().firstName("test")
                                  .lastName("test")
                                  .address(Address.builder().addressLine1("test").county("test").postCode("123").build())
                                  .build())
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "editReturnedOrder",
            PrlAppsConstants.ENGLISH
        );
        System.out.println("NNNNN " + caseDataMap);
        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
        assertNotNull(((List<Element<DraftOrder>>) caseDataMap.get("orderCollection")));
        assertEquals("FL401", caseDataMap.get("caseTypeOfApplication"));
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderAndUpdateCaseDataForDocmosisForSdoOrder() {

        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .dateOrderMade(LocalDate.parse("2022-02-16"))
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        List<Element<OrderDetails>> elementList = new ArrayList<>();
        elementList.add(element(OrderDetails.builder().dateCreated(LocalDateTime.now()).build()));
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .orderCollection(elementList)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicantsFL401(PartyDetails.builder().firstName("test").lastName("test").build())
            .respondentsFL401(PartyDetails.builder().firstName("test")
                                  .lastName("test")
                                  .address(Address.builder().addressLine1("test").county("test").postCode("123").build())
                                  .build())
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        CaseData caseData2 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .standardDirectionOrder(StandardDirectionOrder.builder().sdoHearingsAndNextStepsList(List.of(
                SdoHearingsAndNextStepsEnum.factFindingHearing
            )).build())
            .orderCollection(elementList)
            .draftOrderCollection(draftOrderCollection)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicantsFL401(PartyDetails.builder().firstName("test").lastName("test").build())
            .respondentsFL401(PartyDetails.builder().firstName("test")
                                  .lastName("test")
                                  .address(Address.builder().addressLine1("test").county("test").postCode("123").build())
                                  .build())
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData2);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData2)).thenReturn(caseData2);
        when(manageOrderService.populateDirectionOfFactFindingHearingFieldsForDocmosis(caseData2)).thenReturn(caseData2);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "editReturnedOrder",
            PrlAppsConstants.ENGLISH
        );
        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
        assertNotNull(((List<Element<DraftOrder>>) caseDataMap.get("orderCollection")));
        assertEquals("FL401", caseDataMap.get("caseTypeOfApplication"));
    }


    @Test
    public void testAdminUpdateUploadDraftOrderCollection() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderTypeId("Blank order or directions")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .isJudgeApprovalNeeded(Yes)
                              .status("Drafted by CA")
                              .build())
            .isOrderUploadedByJudgeOrAdmin(Yes)
            .adminNotes("test")
            .build();
        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder()
                              .makeChangesToUploadedOrder(Yes)
                              .editedUploadOrderDoc(Document.builder().documentFileName("abc.pdf").build())
                              .hearingsType(dynamicList)
                              .build())
            .wasTheOrderApprovedAtHearing(Yes)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());

        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            ADMIN_EDIT_AND_APPROVE_ORDER.getId(),
            null
        );

        assertNotNull(caseDataMap);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
        List<Element<DraftOrder>> updatedDraftOrderCollection =  (List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection");
        DraftOrder updatedDraftOrder = updatedDraftOrderCollection.get(0).getValue();
        assertNotNull(updatedDraftOrder.getHearingsType());
        assertEquals("Yes", String.valueOf(updatedDraftOrder.getWasTheOrderApprovedAtHearing()));
        assertEquals("Yes", String.valueOf(updatedDraftOrder.getOtherDetails().getIsJudgeApprovalNeeded()));
    }

    @Test
    public void testJudgeUpdateUploadDraftOrderCollectionAndApprove() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderTypeId("Blank order or directions")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .isJudgeApprovalNeeded(Yes)
                              .status("Reviewed by Judge")
                              .build())
            .isOrderUploadedByJudgeOrAdmin(Yes)
            .build();
        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .children(children)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder()

                              .editedUploadOrderDoc(Document.builder().documentFileName("abc.pdf").build())
                              .hearingsType(dynamicList)
                              .whatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe)
                              .build())
            .judgeDirectionsToAdmin("test")
            .wasTheOrderApprovedAtHearing(Yes)
            .build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(listItems);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());

        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            "test-auth",
            EDIT_AND_APPROVE_ORDER.getId(),
            null
        );

        assertNotNull(caseDataMap);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
        List<Element<DraftOrder>> updatedDraftOrderCollection =  (List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection");
        DraftOrder updatedDraftOrder = updatedDraftOrderCollection.get(0).getValue();
        assertNotNull(updatedDraftOrder.getHearingsType());
        assertEquals("Yes", String.valueOf(updatedDraftOrder.getWasTheOrderApprovedAtHearing()));
        assertEquals("No", String.valueOf(updatedDraftOrder.getOtherDetails().getIsJudgeApprovalNeeded()));
    }

    @Test
    public void testSolicitorEditReturnedOrderUpdateDraftOrderCollection() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .isJudgeApprovalNeeded(Yes)
                              .build())
            .build();
        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        ordersHearingDetails.add(element(HearingData.builder().build()));
        caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder()
                              .hasJudgeProvidedHearingDetails(Yes)
                              .ordersHearingDetails(ordersHearingDetails)
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(
            caseData.getManageOrders().getRejectedOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(manageOrderService.getLoggedInUserType(authToken)).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            authToken,
            EDIT_RETURNED_ORDER.getId(),
            null
        );

        assertNotNull(caseDataMap);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
        List<Element<DraftOrder>> updatedDraftOrderCollection =  (List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection");
        DraftOrder updatedDraftOrder = updatedDraftOrderCollection.get(0).getValue();
        assertNotNull(updatedDraftOrder.getManageOrderHearingDetails());
        assertEquals(dateReservedWithListAssit.getDisplayedValue(),
                     updatedDraftOrder.getManageOrderHearingDetails().get(0).getValue().getHearingDateConfirmOptionEnum().getDisplayedValue());
    }

    @Test
    public void testRemoveDraftOrderAndAddToFinalOrderAndUpdateCaseDataForDocmosisC100ForGeneralFormOrder() {

        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .build())
            .dateOrderMade(LocalDate.parse("2022-02-16"))
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.generalForm)
            .build();

        Element<DraftOrder> draftOrderElement = customElement(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        PartyDetails applicantsPartyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .firstName("FIRST_NAME")
            .lastName("LAST_NAME")
            .solicitorReference("SOLICITOR_REFERENCE")
            .build();
        Element<PartyDetails> applicants = element(applicantsPartyDetails);
        PartyDetails respondentPartyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .firstName("FIRST_NAME")
            .lastName("LAST_NAME")
            .dateOfBirth(LocalDate.now().minusYears(20))
            .solicitorReference("SOLICITOR_REFERENCE")
            .build();
        Element<PartyDetails> respondents = element(respondentPartyDetails);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();

        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        List<Element<OrderDetails>> elementList = new ArrayList<>();
        elementList.add(element(OrderDetails.builder().dateCreated(LocalDateTime.now()).build()));
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .orderCollection(elementList)
            .draftOrderCollection(draftOrderCollection)
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            //.applicantsFL401(PartyDetails.builder().firstName("test").lastName("test").build())
            .applicants(List.of(applicants))
            .respondents(List.of(respondents))
            /*.respondentsFL401(PartyDetails.builder().firstName("test")
                                  .lastName("test")
                                  .address(Address.builder().addressLine1("test").county("test").postCode("123").build())
                                  .build())*/
            .manageOrders(ManageOrders.builder()
                              .serveToRespondentOptions(YesNoNotApplicable.Yes)
                              .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .build())
            .serveOrderData(ServeOrderData.builder()
                                .doYouWantToServeOrder(Yes).build())
            .build();
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(elementUtils.getDynamicListSelectedValue(caseData.getDraftOrdersDynamicList(), objectMapper))
            .thenReturn(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"));
        when(manageOrderService.filterEmptyHearingDetails(caseData)).thenReturn(caseData);
        when(manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData)).thenReturn(caseData);
        when(manageOrderService.populateJudgeNames(caseData)).thenReturn(caseData);
        when(manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData)).thenReturn(caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap = draftAnOrderService.removeDraftOrderAndAddToFinalOrder(
            "test token",
            caseData,
            "adminEditAndApproveAnOrder",
            PrlAppsConstants.ENGLISH
        );
        System.out.println("NNNNN " + caseDataMap);
        assertEquals(0, ((List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection")).size());
        assertNotNull(((List<Element<DraftOrder>>) caseDataMap.get("orderCollection")));
        assertEquals("C100", caseDataMap.get("caseTypeOfApplication"));
    }

    @Test
    public void testHandleDocumentGenerationWhenOrderEdited() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test")
                              .build())
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
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
            .manageOrders(ManageOrders.builder()
                              .fl404CustomFields(FL404.builder()
                                                     .fl404bApplicantName("test")
                                                     .build())
                              .whatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe)
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .doYouWantToEditTheOrder(Yes)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(EDIT_AND_APPROVE_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testHandleDocumentGenerationWhenBlankOrderEdited() throws Exception {
        DraftOrder draftOrder = DraftOrder.builder()
            .parentName("test")
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy("test")
                              .build())
            .isOrderCreatedBySolicitor(Yes)
            .orderType(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
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
            .manageOrders(ManageOrders.builder()
                              .fl404CustomFields(FL404.builder()
                                                     .fl404bApplicantName("test")
                                                     .build())
                              .whatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe)
                              .ordersHearingDetails(List.of(element(HearingData.builder().build())))
                              .build())
            .doYouWantToEditTheOrder(Yes)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(EDIT_AND_APPROVE_ORDER.getId())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        assertNotNull(draftAnOrderService.handleDocumentGeneration("testAuth", callbackRequest, null, PrlAppsConstants.ENGLISH));
    }

    @Test
    public void testGetSelectedDraftOrderDetails() throws Exception {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().id(UUID.fromString("048a6b7e-e2c5-4e6f-8f81-f4926c59bb74"))
            .value(DraftOrder.builder().otherDetails(OtherDraftOrderDetails.builder().dateCreated(LocalDateTime.now()).build()).build())
            .build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        String encodedString = "eyJjbGllbnRfY29udGV4dCI6eyJ1c2VyX3Rhc2siOnsidGFza19kYXRhIjp7ImlkIjoiNmI"
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
        DraftOrder draftOrder = draftAnOrderService
            .getSelectedDraftOrderDetails(draftOrderCollection, null, encodedString, Event.EDIT_AND_APPROVE_ORDER.getId());
        Assert.assertNotNull(draftOrder);
    }

    @Test
    public void testSolicitorEditReturnedSdoOrderUpdateDraftOrderCollection() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .isJudgeApprovalNeeded(Yes)
                              .build())
            .build();
        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        ordersHearingDetails.add(element(HearingData.builder().build()));
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(ManageOrders.builder()
                              .hasJudgeProvidedHearingDetails(Yes)
                              .ordersHearingDetails(ordersHearingDetails)
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(
            caseData.getManageOrders().getRejectedOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(manageOrderService.setHearingDataForSdo(any(), any(), anyString())).thenReturn(caseData);
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            authToken,
            EDIT_RETURNED_ORDER.getId(),
            null
        );

        assertNotNull(caseDataMap);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
        List<Element<DraftOrder>> updatedDraftOrderCollection =  (List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection");
        DraftOrder updatedDraftOrder = updatedDraftOrderCollection.get(0).getValue();
        assertNotNull(updatedDraftOrder.getManageOrderHearingDetails());
        assertEquals(dateReservedWithListAssit.getDisplayedValue(),
                     updatedDraftOrder.getManageOrderHearingDetails().get(0).getValue().getHearingDateConfirmOptionEnum().getDisplayedValue());
    }

    @Test
    public void testSolicitorEditReturnedC6OrderUpdateDraftOrderCollection() {
        DraftOrder draftOrder = DraftOrder.builder()
            .orderDocument(Document.builder().documentFileName("abc.pdf").build())
            .orderType(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .dateCreated(LocalDateTime.now())
                              .createdBy("test")
                              .isJudgeApprovalNeeded(Yes)
                              .build())
            .build();
        Element<DraftOrder> draftOrderElement = element(draftOrder);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);

        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        ordersHearingDetails.add(element(HearingData.builder().build()));
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> partyElemnets = element(partyDetails);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .draftOrderCollection(draftOrderCollection)
            .applicants(List.of(partyElemnets))
            .respondents(List.of(partyElemnets))
            .manageOrders(ManageOrders.builder()
                              .hasJudgeProvidedHearingDetails(Yes)
                              .ordersHearingDetails(ordersHearingDetails)
                              .rejectedOrdersDynamicList(DynamicList.builder()
                                                             .value(DynamicListElement.builder().code(PrlAppsConstants.TEST_UUID)
                                                                        .build())
                                                             .build()).build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(
            caseData.getManageOrders().getRejectedOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        when(manageOrderService.getLoggedInUserType(Mockito.anyString())).thenReturn(UserRoles.SOLICITOR.name());
        Map<String, Object> caseDataMap = draftAnOrderService.updateDraftOrderCollection(
            caseData,
            authToken,
            EDIT_RETURNED_ORDER.getId(),
            null
        );

        assertNotNull(caseDataMap);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
        List<Element<DraftOrder>> updatedDraftOrderCollection =  (List<Element<DraftOrder>>) caseDataMap.get("draftOrderCollection");
        DraftOrder updatedDraftOrder = updatedDraftOrderCollection.get(0).getValue();
        assertNotNull(updatedDraftOrder.getManageOrderHearingDetails());
        assertEquals(dateReservedWithListAssit.getDisplayedValue(),
                     updatedDraftOrder.getManageOrderHearingDetails().get(0).getValue().getHearingDateConfirmOptionEnum().getDisplayedValue());
    }
}
