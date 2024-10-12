package uk.gov.hmcts.reform.prl.controllers;

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
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
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
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DirectionOnIssue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MANDATORY_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MANDATORY_MAGISTRATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OCCUPATIONAL_SCREEN_ERRORS;
import static uk.gov.hmcts.reform.prl.enums.Event.ADMIN_EDIT_AND_APPROVE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.noticeOfProceedingsParties;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.occupation;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class DraftAnOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private ManageOrderService manageOrderService;

    @Mock
    private CaseData caseData;

    @Mock
    private CaseDetails caseDetails;

    @Mock
    private UserDetails userDetails;

    @Mock
    private HearingDataService hearingDataService;

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private DraftAnOrderController draftAnOrderController;

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setUp() {
        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(hearingDataService.populateHearingDynamicLists(Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any()))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());

        when(hearingDataService.getHearingDataForOtherOrders(Mockito.any(),Mockito.any(),Mockito.any()))
            .thenReturn(List.of(Element.<HearingData>builder().build()));
        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());
    }

    @Test
    public void testResetFields() {
        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        Assert.assertEquals(
            0,
            draftAnOrderController.resetFields(authToken, s2sToken, callbackRequest).getData().size()
        );
    }

    @Test
    public void testPopulateHeader() {
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
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populateHearingsDropdown(authToken, caseData)).thenReturn(dynamicList);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(draftAnOrderService.handleSelectedOrder(callbackRequest, authToken))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().data(stringObjectMap).build());
        Map<String, Object> updatedCaseData = draftAnOrderController.populateHeader(
            authToken, s2sToken, callbackRequest
        ).getData();

        Assert.assertEquals(caseData.getApplicantCaseName(), updatedCaseData.get("applicantCaseName"));
        Assert.assertEquals(caseData.getFamilymanCaseNumber(), updatedCaseData.get("familymanCaseNumber"));
        //Assert.assertEquals(caseData.getCreateSelectOrderOptions(), updatedCaseData.get("createSelectOrderOptions"));

    }

    @Test
    public void testPopulateHeaderDio() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.directionOnIssue)
            .selectedOrder(CreateSelectOrderOptionsEnum.blankOrderOrDirections.getDisplayedValue())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(draftAnOrderService.handleSelectedOrder(any(),
                                                     any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
            "This order is not available to be drafted")).build());

        Assert.assertEquals(
            "This order is not available to be drafted",
            draftAnOrderController.populateHeader(authToken, s2sToken, callbackRequest).getErrors().get(0)
        );
    }

    @Test
    public void testPopulateHeaderSdo() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .manageOrders(ManageOrders.builder().build())
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(draftAnOrderService.handleSelectedOrder(any(),
                                                     any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
            "This order is not available to be drafted")).build());
        Assert.assertEquals(
            "This order is not available to be drafted",
            draftAnOrderController.populateHeader(authToken, s2sToken, callbackRequest).getErrors().get(0)
        );
    }

    @Test
    public void testHandlePopulateDraftOrderFieldsWhenUploadingOrder() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
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
        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any())).thenReturn(stringObjectMap);
        Assert.assertEquals(
            stringObjectMap,
            draftAnOrderController.populateFl404Fields(authToken, s2sToken, callbackRequest).getData()
        );
    }


    @Test
    public void testHandleSelectedOrderUploadingOrder() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .draftOrderOptions(DraftOrderOptionsEnum.uploadAnOrder)
            .selectedOrder("Test order")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(manageOrderService.getSelectedOrderInfoForUpload(caseData)).thenReturn("Test order");
        when(objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        )).thenReturn(caseData);
        stringObjectMap.put("selectedOrder", "Test order");
        when(draftAnOrderService.handleSelectedOrder(callbackRequest,authToken)).thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                                                                                                .data(stringObjectMap).build());
        Assert.assertEquals(
            stringObjectMap.get("selectedOrder"),
            draftAnOrderController.populateHeader(authToken, s2sToken, callbackRequest).getData().get("selectedOrder")
        );
    }

    @Test
    public void testPopulateFl404Fields() throws Exception {

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
        when(manageOrderService.populateCustomOrderFields(caseData,CreateSelectOrderOptionsEnum.nonMolestation))
            .thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = manageOrderService.populateCustomOrderFields(caseData,CreateSelectOrderOptionsEnum.nonMolestation);
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any())).thenReturn(caseDataUpdated);
        Assert.assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateFl404Fields(authToken, s2sToken, callbackRequest).getData()
        );
    }

    @Test
    public void testPopulateFl404FieldsBlankOrder() throws Exception {

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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(draftAnOrderService.updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData)).thenReturn(caseData);

        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = manageOrderService.populateCustomOrderFields(caseData, CreateSelectOrderOptionsEnum.blankOrderOrDirections);
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any())).thenReturn(caseDataUpdated);
        Assert.assertEquals(caseDataUpdated, draftAnOrderController.populateFl404Fields(authToken,s2sToken,callbackRequest).getData());
    }

    @Test
    public void testPopulateFl404FieldsBlankOrder_scenario2() throws Exception {

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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(draftAnOrderService.updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData)).thenReturn(caseData);

        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = manageOrderService.populateCustomOrderFields(caseData,CreateSelectOrderOptionsEnum.blankOrderOrDirections);
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        when(draftAnOrderService.handlePopulateDraftOrderFields(any(), any())).thenReturn(caseDataUpdated);
        Assert.assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateFl404Fields(authToken, s2sToken, callbackRequest).getData()
        );
    }

    @Test
    public void testGenerateDoc() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().build())
            .caseTypeOfApplication("fl401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();

        when(draftAnOrderService.generateOrderDocument(Mockito.anyString(), Mockito.any(CallbackRequest.class), Mockito.any()))
            .thenReturn(stringObjectMap);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(manageOrderService.getCaseData("test token", caseData, CreateSelectOrderOptionsEnum.blankOrderOrDirections));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(DraftOrder.builder().build());
        when(draftAnOrderService.handleDocumentGeneration(authToken, callbackRequest, null)).thenReturn(stringObjectMap);
        Assert.assertEquals(caseDataUpdated, draftAnOrderController.generateDoc(authToken,s2sToken,"clcx", callbackRequest).getData());
    }

    @Test
    public void testGenerateDocThrowError() throws Exception {
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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();;
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderController.populateFl404Fields(authToken,s2sToken, callbackRequest);
        Assert.assertEquals(MANDATORY_JUDGE,
                            response.getErrors().get(0));
    }

    @Test
    public void testGenerateDocThrowErrorMagistrate() throws Exception {
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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();;
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderController.populateFl404Fields(authToken,s2sToken, callbackRequest);
        Assert.assertEquals(MANDATORY_MAGISTRATE,
                            response.getErrors().get(0));
    }

    @Test
    public void testGenerateDocTMagistrate() throws Exception {
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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .eventId(ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .build();;
        AboutToStartOrSubmitCallbackResponse response = draftAnOrderController.populateFl404Fields(authToken,s2sToken, callbackRequest);
        Assert.assertEquals(draftAnOrderController.populateFl404Fields(authToken,s2sToken, callbackRequest),
                            response);
    }

    @Test
    public void testPrepareDraftOrderCollection() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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

        Assert.assertEquals(
            caseDataUpdated,
            draftAnOrderController.prepareDraftOrderCollection(
                authToken,
                s2sToken,
                callbackRequest
            ).getData()
        );

    }

    @Test
    public void testPopulateSdoFields() throws Exception {
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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        Assert.assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateSdoFields(authToken, s2sToken, callbackRequest).getData()
        );
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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
            draftAnOrderController.populateSdoFields(authToken, s2sToken, callbackRequest).getErrors().get(0)
        );

    }

    @Test
    public void testPopulateDioFields() throws Exception {
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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Assert.assertEquals(
            caseDataUpdated,
            draftAnOrderController.populateDioFields(authToken, s2sToken, callbackRequest).getData()
        );

    }

    @Test
    public void testPopulateDioFieldsWithNoOptionSelected() throws Exception {
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
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Assert.assertEquals(
            "Please select at least one options from below",
            draftAnOrderController.populateDioFields(authToken, s2sToken, callbackRequest).getErrors().get(0)
        );

    }

    @Test
    public void testExceptionForResetFields() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            draftAnOrderController.resetFields(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateHeader() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            draftAnOrderController.populateHeader(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateFl404Fields() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            draftAnOrderController.populateFl404Fields(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateSdoFields() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            draftAnOrderController.populateSdoFields(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForGenerateDoc() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        when(draftAnOrderService.handleDocumentGenerationForaDraftOrder(Mockito.anyString(), Mockito.any())).thenReturn(stringObjectMap);
        assertExpectedException(() -> {
            draftAnOrderController.generateDoc(authToken, s2sToken,"clcx", callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPrepareDraftOrderCollection() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            draftAnOrderController.prepareDraftOrderCollection(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateDioFields() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            draftAnOrderController.populateDioFields(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testNoHearingDataValidation() throws Exception {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("errorList", List.of("Please provide at least one hearing details"));
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(noticeOfProceedingsParties)
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);
        when(draftAnOrderService.handleDocumentGeneration(authToken, callbackRequest, null)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(authToken, s2sToken,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please provide at least one hearing details", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testNoHearingDataSelectedValidation() throws Exception {
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(HearingData.builder().build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("errorList", List.of("Please provide at least one hearing details"));
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(noticeOfProceedingsParties)
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);
        when(draftAnOrderService.handleDocumentGeneration(authToken, callbackRequest, null)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController.generateDoc(authToken, s2sToken,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please provide at least one hearing details", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testMoreThanOneHearingsSelectedValidation() throws Exception {
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
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(noticeOfProceedingsParties)
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);
        when(draftAnOrderService.handleDocumentGeneration(authToken, callbackRequest, null)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController.generateDoc(authToken, s2sToken,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Only one hearing can be created", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testHearingTypeAndEstimatedTimingsValidations() throws Exception {
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
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(noticeOfProceedingsParties)
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);
        when(draftAnOrderService.handleDocumentGeneration(authToken, callbackRequest, null)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(authToken, s2sToken,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("HearingType cannot be empty, please select a hearingType", callbackResponse.getErrors().get(0));
        assertEquals("Please enter numeric value for Hearing estimated days", callbackResponse.getErrors().get(1));
        assertEquals("Please enter numeric value for Hearing estimated hours", callbackResponse.getErrors().get(2));
        assertEquals("Please enter numeric value for Hearing estimated minutes", callbackResponse.getErrors().get(3));
    }

    @Test
    public void testOccupationTypeScreenValidations() throws Exception {

        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(occupation)
            .manageOrders(ManageOrders.builder().fl404CustomFields(FL404.builder().build()).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("errorList", List.of("Please select either applicant or participant section"));
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(occupation)
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);
        when(draftAnOrderService.handleDocumentGeneration(authToken, callbackRequest, null)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(authToken, s2sToken,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please select either applicant or participant section", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testOccupationTypeScreenValidationsForNoErrors() throws Exception {

        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(occupation)
            .manageOrders(ManageOrders.builder().fl404CustomFields(FL404.builder().build()).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put(OCCUPATIONAL_SCREEN_ERRORS, new ArrayList<String>());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(occupation)
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(), Mockito.any())).thenReturn(draftOrder);
        when(draftAnOrderService.handleDocumentGenerationForaDraftOrder(Mockito.anyString(), Mockito.any())).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = draftAnOrderController
            .generateDoc(authToken, s2sToken,"clcx", callbackRequest);

        assertNotNull(callbackResponse);
        assertNull(callbackResponse.getErrors());
    }
}
