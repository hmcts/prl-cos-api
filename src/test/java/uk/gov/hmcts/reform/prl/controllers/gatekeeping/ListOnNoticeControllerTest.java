package uk.gov.hmcts.reform.prl.controllers.gatekeeping;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.ListOnNoticeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REASONS_SELECTED_FOR_LIST_ON_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;


@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ListOnNoticeControllerTest {

    @InjectMocks
    ListOnNoticeController listOnNoticeController;

    @Mock
    ListOnNoticeService listOnNoticeService;
    @Mock
    private ObjectMapper objectMapper;

    public static final String authToken = "Bearer TestAuthToken";

    @Mock
    private AddCaseNoteService addCaseNoteService;

    @Mock
    private UserService userService;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    AllocatedJudgeService allocatedJudgeService;

    private CaseData caseData;

    private Map<String, Object> stringObjectMap;

    private CallbackRequest callbackRequest;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;


    @Before
    public void setUp() {
        caseData = CaseData.builder()
            .courtName("testcourt")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
    }

    @Test
    public void testListOnNoticeMidEvent() throws Exception {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<String> reasonsSelected = new ArrayList<>();
        reasonsSelected.add("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder");
        reasonsSelected.add("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice");

        caseDataUpdated.put(LIST_ON_NOTICE_REASONS_SELECTED,reasonsSelected);
        String reasonsSelectedString = ListOnNoticeReasonsEnum.getDisplayedValue("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder")
            + "\n" + ListOnNoticeReasonsEnum.getDisplayedValue("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice") + "\n";
        when(listOnNoticeService.getReasonsSelected(reasonsSelected, Long.valueOf("123"))).thenReturn(reasonsSelectedString);
        AboutToStartOrSubmitCallbackResponse response = listOnNoticeController.listOnNoticeMidEvent(authToken,callbackRequest);
        assertNotNull(response);
        assertEquals(reasonsSelectedString,response.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
    }

    @Test
    public void testListOnNoticeSubmission() throws Exception {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<String> reasonsSelected = new ArrayList<>();
        reasonsSelected.add("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder");
        reasonsSelected.add("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice");

        caseDataUpdated.put(LIST_ON_NOTICE_REASONS_SELECTED,reasonsSelected);
        String reasonsSelectedString = ListOnNoticeReasonsEnum.getDisplayedValue("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder")
            + "\n" + ListOnNoticeReasonsEnum.getDisplayedValue("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice") + "\n";
        caseDataUpdated.put(SELECTED_AND_ADDITIONAL_REASONS,reasonsSelectedString + "testAdditionalReasons\n");
        List<CaseNoteDetails> caseNoteDetails = new ArrayList<>();
        CaseNoteDetails caseNoteDetails1 = CaseNoteDetails.builder()
            .subject(REASONS_SELECTED_FOR_LIST_ON_NOTICE).caseNote((String) caseDataUpdated.get("SELECTED_AND_ADDITIONAL_REASONS"))
            .dateAdded(LocalDate.now().toString()).dateCreated(LocalDateTime.now()).build();
        caseNoteDetails.add(caseNoteDetails1);
        when(listOnNoticeService.getReasonsSelected(reasonsSelected, Long.valueOf("123"))).thenReturn(reasonsSelectedString);
        when(userService.getUserDetails(authToken)).thenReturn(UserDetails.builder().forename("PRL").surname("Judge").build());
        when(addCaseNoteService.addCaseNoteDetails(caseData,UserDetails.builder().forename("PRL").surname("Judge").build()))
            .thenReturn(ElementUtils.wrapElements(caseNoteDetails));
        AboutToStartOrSubmitCallbackResponse response = listOnNoticeController.listOnNoticeSubmission(authToken,callbackRequest);
        assertNotNull(response);
        assertEquals(reasonsSelectedString + "testAdditionalReasons\n",response.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
        assertEquals(ElementUtils.wrapElements(caseNoteDetails), response.getData().get(CASE_NOTES));
    }

    @Test
    public void testListOnNoticeSubmissionWithoutSelectingAnyReasons() throws Exception {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(LIST_ON_NOTICE_REASONS_SELECTED,null);
        caseDataUpdated.put(SELECTED_AND_ADDITIONAL_REASONS,null);
        when(listOnNoticeService.getReasonsSelected(null, Long.valueOf("123"))).thenReturn("");
        when(userService.getUserDetails(authToken)).thenReturn(UserDetails.builder().forename("PRL").surname("Judge").build());
        when(addCaseNoteService.addCaseNoteDetails(caseData,UserDetails.builder().forename("PRL").surname("Judge").build()))
            .thenReturn(null);
        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.DISTRICT_JUDGE)
            .build();
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        AboutToStartOrSubmitCallbackResponse response = listOnNoticeController.listOnNoticeSubmission(authToken,callbackRequest);
        assertNotNull(response);
        assertNull(response.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
        assertNull(response.getData().get(CASE_NOTES));
    }

    @Test
    public void testListOnNoticePrePopulateListOnNotice() throws Exception {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));
        AboutToStartOrSubmitCallbackResponse response = listOnNoticeController.prePopulateListOnNotice(authToken,callbackRequest);
        assertNotNull(response);
    }

}
