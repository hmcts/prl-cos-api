package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MiamForOrderServiceTest {

    @InjectMocks
    private MiamForOrderService miamForOrderService;
    @Mock
    private DraftAnOrderService draftAnOrderService;
    @Mock
    private ObjectMapper objectMapper;

    private static final String CLIENT_CONTEXT = """
        {
          "client_context": {
            "user_task": {
              "task_data": {
                "additional_properties": {
                  "hearingId": "12345"
                }
              },
              "complete_task" : true
            }
          }
        }
        """;

    @BeforeEach
    public void setUp() {
        DraftOrder draftOrder = mock(DraftOrder.class);
        when(draftAnOrderService.getSelectedDraftOrderDetails(any(), any(), any(), any())).thenReturn(draftOrder);
        // Set up any necessary test data or configurations here
    }


    @Test
    public void testFlagSetToNoForMiamOrderWhenStateIsSubmittedNotPaid() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", 12345L);
        stringObjectMap.put("state", State.SUBMITTED_NOT_PAID);

        CaseData customCaseData = setUpCaseDataForMiamTest(State.SUBMITTED_NOT_PAID);
        CaseDetails caseDetails = setUpCaseDetailsForMiamTest(stringObjectMap, State.SUBMITTED_NOT_PAID);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(customCaseData);

        Map<String, Object> response = miamForOrderService.updateCaseDataWithMiamForOrderDetails(
            caseDetails,
            "EditAndApproveOrder",
            CLIENT_CONTEXT,
            stringObjectMap
        );

        assertNotNull(response);
        assertEquals(No, response.get("eligibleStateForMiam"));
        Assertions.assertNull(response.get("miamForOrder"));
    }

    @Test
    public void testFlagSetToNoForMiamOrderWhenStateIsSubmittedPaid() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", 12345L);
        stringObjectMap.put("state", State.SUBMITTED_PAID);

        CaseData customCaseData = setUpCaseDataForMiamTest(State.SUBMITTED_PAID);
        CaseDetails caseDetails = setUpCaseDetailsForMiamTest(stringObjectMap, State.SUBMITTED_PAID);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(customCaseData);

        Map<String, Object> response = miamForOrderService.updateCaseDataWithMiamForOrderDetails(
            caseDetails,
            "EditAndApproveOrder",
            CLIENT_CONTEXT,
            stringObjectMap
        );

        assertNotNull(response);
        assertEquals(No, response.get("eligibleStateForMiam"));
        Assertions.assertNull(response.get("miamForOrder"));
    }

    @Test
    public void testFlagSetToNoForMiamOrderWhenStateIsCaseIssued() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", 12345L);
        stringObjectMap.put("state", State.CASE_ISSUED);

        CaseData customCaseData = setUpCaseDataForMiamTest(State.CASE_ISSUED);
        CaseDetails caseDetails = setUpCaseDetailsForMiamTest(stringObjectMap, State.CASE_ISSUED);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(customCaseData);

        Map<String, Object> response = miamForOrderService.updateCaseDataWithMiamForOrderDetails(
            caseDetails,
            "EditAndApproveOrder",
            CLIENT_CONTEXT,
            stringObjectMap
        );

        assertNotNull(response);
        assertEquals(No, response.get("eligibleStateForMiam"));
        Assertions.assertNull(response.get("miamForOrder"));
    }

    @Test
    public void testFlagSetToNoForMiamOrderWhenStateIsJudicialReview() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", 12345L);
        stringObjectMap.put("state", State.JUDICIAL_REVIEW);

        CaseData customCaseData = setUpCaseDataForMiamTest(State.JUDICIAL_REVIEW);
        CaseDetails caseDetails = setUpCaseDetailsForMiamTest(stringObjectMap, State.JUDICIAL_REVIEW);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(customCaseData);

        Map<String, Object> response = miamForOrderService.updateCaseDataWithMiamForOrderDetails(
            caseDetails,
            "EditAndApproveOrder",
            CLIENT_CONTEXT,
            stringObjectMap
        );

        assertNotNull(response);
        assertEquals(No, response.get("eligibleStateForMiam"));
        Assertions.assertNull(response.get("miamForOrder"));
    }

    @Test
    public void testFlagSetToNoForMiamOrderWhenStateIsAwaitingFl401SubmissionToHmcts() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", 12345L);
        stringObjectMap.put("state", State.AWAITING_FL401_SUBMISSION_TO_HMCTS);

        CaseData customCaseData = setUpCaseDataForMiamTest(State.AWAITING_FL401_SUBMISSION_TO_HMCTS);
        CaseDetails caseDetails = setUpCaseDetailsForMiamTest(stringObjectMap, State.AWAITING_FL401_SUBMISSION_TO_HMCTS);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(customCaseData);

        Map<String, Object> response = miamForOrderService.updateCaseDataWithMiamForOrderDetails(
            caseDetails,
            "EditAndApproveOrder",
            CLIENT_CONTEXT,
            stringObjectMap
        );

        assertNotNull(response);
        assertEquals(No, response.get("eligibleStateForMiam"));
        Assertions.assertNull(response.get("miamForOrder"));
    }

    @Test
    public void testStateIsPrepareForHearingConductHearingAndUserSelectsYesMiamForOrder() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", 12345L);
        stringObjectMap.put("state", State.PREPARE_FOR_HEARING_CONDUCT_HEARING);

        CaseData customCaseData = setUpCaseDataForMiamTest(State.PREPARE_FOR_HEARING_CONDUCT_HEARING);
        CaseDetails caseDetails = setUpCaseDetailsForMiamTest(stringObjectMap, State.PREPARE_FOR_HEARING_CONDUCT_HEARING);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(customCaseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(any(), any(), any(), any())).thenReturn(DraftOrder.builder().miamForOrder(Yes).build());

        Map<String, Object> response = miamForOrderService.updateCaseDataWithMiamForOrderDetails(
            caseDetails,
            "EditAndApproveOrder",
            CLIENT_CONTEXT,
            stringObjectMap
        );

        assertNotNull(response);
        assertEquals(Yes, response.get("eligibleStateForMiam"));
        assertEquals(Yes, response.get("miamForOrder"));
    }

    @Test
    public void testStateIsPrepareForHearingConductHearingAndUserSelectsNoMiamForOrder() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        CaseDetails caseDetails = setUpCaseDetailsForMiamTest(stringObjectMap, State.PREPARE_FOR_HEARING_CONDUCT_HEARING);
        CaseData customCaseData = setUpCaseDataForMiamTest(State.PREPARE_FOR_HEARING_CONDUCT_HEARING);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(customCaseData);
        when(draftAnOrderService.getSelectedDraftOrderDetails(any(), any(), any(), any())).thenReturn(DraftOrder.builder().miamForOrder(No).build());

        Map<String, Object> response = miamForOrderService.updateCaseDataWithMiamForOrderDetails(
            caseDetails,
            "EditAndApproveOrder",
            CLIENT_CONTEXT,
            caseDetails.getData()
        );

        assertNotNull(response);
        assertEquals(Yes, response.get("eligibleStateForMiam"));
        assertEquals(No, response.get("miamForOrder"));
    }


    private CaseDetails setUpCaseDetailsForMiamTest(Map<String, Object> stringObjectMap, State state) {

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

        stringObjectMap.put(PrlAppsConstants.DRAFT_ORDER_COLLECTION, draftOrderCollection);
        stringObjectMap.put(
            "draftOrdersDynamicList",
            DynamicList.builder().value(DynamicListElement.EMPTY).listItems(List.of(DynamicListElement.EMPTY)).build()
        );
        return CaseDetails.builder()
                 .id(12345L)
                 .data(stringObjectMap)
                 .state(state.getValue())
                 .build();
    }

    private CaseData setUpCaseDataForMiamTest(State state) {

        return CaseData.builder()
            .id(12345L)
            .state(state)
            .build();
    }
}
