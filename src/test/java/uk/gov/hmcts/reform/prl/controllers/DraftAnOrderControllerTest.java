package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCourtEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DraftOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DirectionOnIssue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MANDATORY_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MANDATORY_MAGISTRATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OCCUPATIONAL_SCREEN_ERRORS;
import static uk.gov.hmcts.reform.prl.enums.Event.ADMIN_EDIT_AND_APPROVE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.noticeOfProceedingsParties;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.occupation;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@PropertySource(value = "classpath:application.yaml")
@ExtendWith(MockitoExtension.class)
class DraftAnOrderControllerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private ManageOrderService manageOrderService;

    @InjectMocks
    private DraftAnOrderController draftAnOrderController;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @BeforeEach
    void setUp() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
    }

    @Test
    void testResetFields() {
        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        assertEquals(
            0,
            draftAnOrderController.resetFields(AUTH_TOKEN, S2S_TOKEN, callbackRequest).getData().size()
        );
    }

    @Test
    void testPopulateHeader() {
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().hearingsType(dynamicList).build())
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .selectedOrder(CreateSelectOrderOptionsEnum.blankOrderOrDirections.getDisplayedValue())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(draftAnOrderService.handleSelectedOrder(any(), any(), any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().data(stringObjectMap).build());
        Map<String, Object> updatedCaseData = draftAnOrderController.populateHeader(
            AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest
        ).getData();

        assertEquals(caseData.getApplicantCaseName(), updatedCaseData.get("applicantCaseName"));
        assertEquals(caseData.getFamilymanCaseNumber(), updatedCaseData.get("familymanCaseNumber"));
    }

    @Test
    void testPopulateHeaderDio() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.directionOnIssue)
            .selectedOrder(CreateSelectOrderOptionsEnum.blankOrderOrDirections.getDisplayedValue())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(draftAnOrderService.handleSelectedOrder(any(),
                                                     any(), any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
            "This order is not available to be drafted")).build());

        assertEquals(
            "This order is not available to be drafted",
            draftAnOrderController.populateHeader(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest).getErrors().getFirst()
        );
    }

    @Test
    void testPopulateHeaderSdo() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .manageOrders(ManageOrders.builder().build())
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(draftAnOrderService.handleSelectedOrder(any(),
                                                     any(), any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
            "This order is not available to be drafted")).build());
        assertEquals(
            "This order is not available to be drafted",
            draftAnOrderController.populateHeader(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest).getErrors().getFirst()
        );
    }

    @Test
    void testHandlePopulateDraftOrderFieldsWhenUploadingOrder() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        )).thenReturn(caseData);
        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any(), any(), any())).thenReturn(stringObjectMap);
        assertEquals(
            stringObjectMap,
            draftAnOrderController.populateFl404Fields(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest).getData()
        );
    }


    @Test
    void testHandleSelectedOrderUploadingOrder() {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        stringObjectMap.put("selectedOrder", "Test order");
        when(draftAnOrderService.handleSelectedOrder(any(), any(), any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                                                                                                .data(stringObjectMap).build());
        assertEquals(
            stringObjectMap.get("selectedOrder"),
            draftAnOrderController.populateHeader(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest).getData().get("selectedOrder")
        );
    }

    @Test
    void testPopulateFl404Fields() throws Exception {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(123L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .manageOrders(ManageOrders.builder().build())
            .selectedOrder("Standard direction order")
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .caseTypeOfApplication("FL401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any(), any(), any())).thenReturn(caseDataUpdated);
        assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateFl404Fields(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest).getData()
        );
    }

    @Test
    void testPopulateFl404FieldsBlankOrder() throws Exception {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(123L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("FL401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);


        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any(), any(), any())).thenReturn(caseDataUpdated);
        assertEquals(caseDataUpdated, draftAnOrderController.populateFl404Fields(AUTH_TOKEN,S2S_TOKEN,
            PrlAppsConstants.ENGLISH,callbackRequest).getData());
    }

    @Test
    void testPopulateFl404FieldsBlankOrder_scenario2() throws Exception {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(123L)
            .standardDirectionOrder(null)
            .manageOrders(null)
            .standardDirectionOrder(null)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("FL401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);


        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any(), any(), any())).thenReturn(caseDataUpdated);
        assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateFl404Fields(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH,callbackRequest).getData()
        );
    }

    @Test
    void testGenerateDoc() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().build())
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(manageOrderService.getCaseData("test token", caseData, CreateSelectOrderOptionsEnum.blankOrderOrDirections));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(draftAnOrderService.handleDocumentGeneration(any(), any(), any(), any())).thenReturn(stringObjectMap);
        assertEquals(caseDataUpdated, draftAnOrderController.generateDoc(AUTH_TOKEN,S2S_TOKEN,"clcx", callbackRequest).getData());
    }

    @Test
    void testGenerateDocThrowError() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesLegalAdviser).build())
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderController.populateFl404Fields(AUTH_TOKEN,S2S_TOKEN,
            PrlAppsConstants.ENGLISH,callbackRequest);
        assertEquals(MANDATORY_JUDGE,
                            response.getErrors().getFirst());
    }

    @Test
    void testGenerateDocThrowErrorMagistrate() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.magistrate).build())
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderController.populateFl404Fields(AUTH_TOKEN,S2S_TOKEN,
            PrlAppsConstants.ENGLISH,callbackRequest);
        assertEquals(MANDATORY_MAGISTRATE,
                            response.getErrors().getFirst());
    }

    @Test
    void testGenerateDocTMagistrate() throws Exception {
        MagistrateLastName magistrateLastName = MagistrateLastName.builder().lastName("Smith").build();
        Element<MagistrateLastName> magistrateLastNameElement = Element.<MagistrateLastName>builder()
            .value(magistrateLastName).build();
        List<Element<MagistrateLastName>> lastNameList = Collections.singletonList(magistrateLastNameElement);

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .magistrateLastName(lastNameList)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.magistrate).build())
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderController.populateFl404Fields(AUTH_TOKEN,S2S_TOKEN,
            PrlAppsConstants.ENGLISH,callbackRequest);
        assertEquals(draftAnOrderController.populateFl404Fields(AUTH_TOKEN,S2S_TOKEN,
                PrlAppsConstants.ENGLISH,callbackRequest),
                            response);
    }

    @Test
    void testPrepareDraftOrderCollection() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(draftAnOrderService.prepareDraftOrderCollection(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class)
        )).thenReturn(stringObjectMap);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        ));

        assertEquals(
            caseDataUpdated,
            draftAnOrderController.prepareDraftOrderCollection(
                AUTH_TOKEN,
                S2S_TOKEN,
                callbackRequest
            ).getData()
        );

    }

    @Test
    void testPopulateSdoFields() {
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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateSdoFields(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest).getData()
        );
    }

    @Test
    void testPopulateSdoFieldsWithNoOptionSelected() {
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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        assertEquals(
            "Please select at least one options from below",
            draftAnOrderController.populateSdoFields(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest).getErrors().getFirst()
        );

    }

    @Test
    void testPopulateDioFields() {
        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioPreamblesList(List.of(DioPreamblesEnum.rightToAskCourt))
            .dioHearingsAndNextStepsList(List.of(DioHearingsAndNextStepsEnum.allocateNamedJudge))
            .dioCafcassOrCymruList(List.of(DioCafcassOrCymruEnum.cafcassCymruSafeguarding))
            .dioLocalAuthorityList(List.of(DioLocalAuthorityEnum.localAuthorityLetter))
            .dioCourtList(List.of(DioCourtEnum.transferApplication))
            .dioOtherList(List.of(DioOtherEnum.parentWithCare))
            .build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .directionOnIssue(directionOnIssue)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateDioFields(AUTH_TOKEN, S2S_TOKEN, callbackRequest).getData()
        );

    }

    @Test
    void testPopulateDioFieldsWithNoOptionSelected() {
        DirectionOnIssue directionOnIssue = DirectionOnIssue.builder()
            .dioPreamblesList(new ArrayList<>())
            .dioHearingsAndNextStepsList(new ArrayList<>())
            .dioCafcassOrCymruList(new ArrayList<>())
            .dioLocalAuthorityList(new ArrayList<>())
            .dioCourtList(new ArrayList<>())
            .dioOtherList(new ArrayList<>())
            .build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .directionOnIssue(directionOnIssue)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        assertEquals(
            "Please select at least one options from below",
            draftAnOrderController.populateDioFields(AUTH_TOKEN, S2S_TOKEN, callbackRequest).getErrors().getFirst()
        );

    }

    private CallbackRequest setupInvalidClientTestData() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        ));

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        return callbackRequest;
    }

    @Test
    void testExceptionForResetFields() {
        CallbackRequest callbackRequest = setupInvalidClientTestData();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            draftAnOrderController.resetFields(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPopulateHeader() {
        CallbackRequest callbackRequest = setupInvalidClientTestData();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            draftAnOrderController.populateHeader(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPopulateFl404Fields() {
        CallbackRequest callbackRequest = setupInvalidClientTestData();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            draftAnOrderController.populateFl404Fields(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPopulateSdoFields() {
        CallbackRequest callbackRequest = setupInvalidClientTestData();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            draftAnOrderController.populateSdoFields(AUTH_TOKEN, S2S_TOKEN, PrlAppsConstants.ENGLISH, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForGenerateDoc() {
        CallbackRequest callbackRequest = setupInvalidClientTestData();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            draftAnOrderController.generateDoc(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPrepareDraftOrderCollection() {
        CallbackRequest callbackRequest = setupInvalidClientTestData();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            draftAnOrderController.prepareDraftOrderCollection(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForPopulateDioFields() {
        CallbackRequest callbackRequest = setupInvalidClientTestData();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            draftAnOrderController.populateDioFields(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testNoHearingDataValidation() throws Exception {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(draftAnOrderService.handleDocumentGeneration(any(), any(), any(), any()))
            .thenReturn(Map.of("errorList", List.of("Please provide at least one hearing details")));

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please provide at least one hearing details", callbackResponse.getErrors().getFirst());
    }

    @Test
    void testNoHearingDataSelectedValidation() throws Exception {
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(HearingData.builder().build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("errorList", List.of("Please provide at least one hearing details"));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN,S2S_TOKEN)).thenReturn(true);
        when(draftAnOrderService.handleDocumentGeneration(any(), any(), any(), any())).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController.generateDoc(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please provide at least one hearing details", callbackResponse.getErrors().getFirst());
    }

    @Test
    void testMoreThanOneHearingsSelectedValidation() throws Exception {
        HearingData hearingData1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .build();
        HearingData hearingData2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .build();
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(hearingData1), element(hearingData2))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("errorList", List.of("Only one hearing can be created"));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN,S2S_TOKEN)).thenReturn(true);
        when(draftAnOrderService.handleDocumentGeneration(any(), any(), any(), any())).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController.generateDoc(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Only one hearing can be created", callbackResponse.getErrors().getFirst());
    }

    @Test
    void testHearingTypeAndEstimatedTimingsValidations() throws Exception {
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingEstimatedDays("ABC")
            .hearingEstimatedHours("DEF")
            .hearingEstimatedMinutes("XYZ")
            .build();
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(hearingData))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("errorList", List.of("HearingType cannot be empty, please select a hearingType",
                                                           "Please enter numeric value for Hearing estimated days",
                                                           "Please enter numeric value for Hearing estimated hours",
                                                           "Please enter numeric value for Hearing estimated minutes"));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN,S2S_TOKEN)).thenReturn(true);
        when(draftAnOrderService.handleDocumentGeneration(any(), any(), any(), any())).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("HearingType cannot be empty, please select a hearingType", callbackResponse.getErrors().getFirst());
        assertEquals("Please enter numeric value for Hearing estimated days", callbackResponse.getErrors().get(1));
        assertEquals("Please enter numeric value for Hearing estimated hours", callbackResponse.getErrors().get(2));
        assertEquals("Please enter numeric value for Hearing estimated minutes", callbackResponse.getErrors().get(3));
    }

    @Test
    void testOccupationTypeScreenValidations() throws Exception {

        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(occupation)
            .manageOrders(ManageOrders.builder().fl404CustomFields(FL404.builder().build()).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("errorList", List.of("Please select either applicant or participant section"));
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN,S2S_TOKEN)).thenReturn(true);
        when(draftAnOrderService.handleDocumentGeneration(any(), any(), any(), any())).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please select either applicant or participant section", callbackResponse.getErrors().getFirst());
    }

    @Test
    void testOccupationTypeScreenValidationsForNoErrors() throws Exception {

        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(occupation)
            .manageOrders(ManageOrders.builder().fl404CustomFields(FL404.builder().build()).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put(OCCUPATIONAL_SCREEN_ERRORS, new ArrayList<String>());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN,S2S_TOKEN)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(AUTH_TOKEN, S2S_TOKEN,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNull(callbackResponse.getErrors());
    }
}
