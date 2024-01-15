package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ListWithoutNoticeDetails;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.controllers.listwithoutnotice.ListWithoutNoticeController.CONFIRMATION_HEADER;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ListWithoutNoticeControllerTest {

    @InjectMocks
    ListWithoutNoticeController listWithoutNoticeController;

    @Mock
    AddCaseNoteService addCaseNoteService;

    @Mock
    UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuth = "serviceAuth";


    @Test
    public void testListWithoutNoticeSubmission() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.DISTRICT_JUDGE)
            .build();
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        when(userService.getUserDetails(anyString())).thenReturn(
            UserDetails.builder().email("abc@test.com").build());

        when(addCaseNoteService.addCaseNoteDetails(any(CaseData.class), any(UserDetails.class))).thenReturn(List.of(element(
            CaseNoteDetails.builder().build())));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController
            .listWithoutNoticeSubmission(authToken,serviceAuth,callbackRequest);
        assertTrue(response.getData().containsKey("caseNotes"));
    }

    @Test
    public void testExceptionForListWithoutNoticeSubmission() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken, serviceAuth)).thenReturn(false);
        assertExpectedException(() -> {
            listWithoutNoticeController.listWithoutNoticeSubmission(authToken,serviceAuth,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForCcdSubmitted() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest callbackRequest = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
                             .caseId("123")
                             .caseData(caseData)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken, serviceAuth)).thenReturn(false);
        assertExpectedException(() -> {
            listWithoutNoticeController.ccdSubmitted(authToken,serviceAuth,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testForCcdSubmitted() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest callbackRequest = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
                             .caseId("123")
                             .caseData(caseData)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken, serviceAuth)).thenReturn(true);
        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse =
            listWithoutNoticeController.ccdSubmitted(authToken,serviceAuth,callbackRequest);
        assertEquals(CONFIRMATION_HEADER, submittedCallbackResponse.getBody().getConfirmationHeader());
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}

