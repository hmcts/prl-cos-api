package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCourtEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DirectionOnIssue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

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

    @InjectMocks
    private DraftAnOrderController draftAnOrderController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();
    }

    @Test
    public void testResetFields() {
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        Assert.assertEquals(0, draftAnOrderController.resetFields(callbackRequest).getData().size());
    }

    @Test
    public void testPopulateHeader() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Assert.assertEquals(stringObjectMap.get("applicantCaseName"),
                            draftAnOrderController.populateHeader(callbackRequest).getData().get("applicantCaseName"));
        Assert.assertEquals(stringObjectMap.get("familymanCaseNumber"),
                            draftAnOrderController.populateHeader(callbackRequest).getData().get("familymanCaseNumber"));

        if (draftAnOrderController.populateHeader(callbackRequest)
            .getData().get("createSelectOrderOptions") != null) {
            Assert.assertEquals(stringObjectMap.get("createSelectOrderOptions"),
                                draftAnOrderController.populateHeader(callbackRequest).getData().get("createSelectOrderOptions"));
        } else {
            Assert.assertEquals("",
                                draftAnOrderController.populateHeader(callbackRequest).getData().get("createSelectOrderOptions"));
        }

    }

    @Test
    public void testPopulateFl404Fields() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Jo Davis & Jon Smith")
            .familymanCaseNumber("sd5454256756")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .caseTypeOfApplication("FL401")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populateCustomOrderFields(caseData)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()

                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Assert.assertEquals(caseDataUpdated, draftAnOrderController.populateFl404Fields("test token", callbackRequest).getData());
    }

    @Test
    public void testPopulateFl404FieldsBlankOrder() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
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

        when(draftAnOrderService.generateDocument(callbackRequest, caseData)).thenReturn(caseData);

        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = manageOrderService.populateCustomOrderFields(caseData);
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Assert.assertEquals(caseDataUpdated, draftAnOrderController.populateFl404Fields("test token", callbackRequest).getData());
    }

    @Test
    public void testGenerateDoc() throws Exception {
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
        when(draftAnOrderService.generateDocument(callbackRequest, caseData)).thenReturn(caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(manageOrderService.getCaseData("test token", caseData, CreateSelectOrderOptionsEnum.blankOrderOrDirections));

        Assert.assertEquals(caseDataUpdated, draftAnOrderController.generateDoc("test token", callbackRequest).getData());
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
        when(draftAnOrderService.generateDocument(callbackRequest, caseData)).thenReturn(caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.putAll(manageOrderService.getCaseData("test token", caseData, CreateSelectOrderOptionsEnum.blankOrderOrDirections));

        Assert.assertEquals(caseDataUpdated, draftAnOrderController.prepareDraftOrderCollection("test token", callbackRequest).getData());

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
        Assert.assertEquals(caseDataUpdated, draftAnOrderController.populateSdoFields("test token", callbackRequest).getData());

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
            draftAnOrderController.populateSdoFields("test token", callbackRequest).getErrors().get(0)
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
        Assert.assertEquals(caseDataUpdated, draftAnOrderController.populateDioFields("test token", callbackRequest).getData());

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
            draftAnOrderController.populateDioFields("test token", callbackRequest).getErrors().get(0)
        );

    }
}
