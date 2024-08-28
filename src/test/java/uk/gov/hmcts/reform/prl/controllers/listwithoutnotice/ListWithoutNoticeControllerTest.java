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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ListWithoutNoticeDetails;
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
import static uk.gov.hmcts.reform.prl.controllers.listwithoutnotice.ListWithoutNoticeController.CONFIRMATION_BODY_PREFIX_CA;
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
    public void shouldSeeHearingDetailswithoutListWithoutNotice() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .caseTypeOfApplication("FL401")
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

        when(hearingPrePopulateService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                                 .build()));
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController
            .prePopulateHearingPageData(authToken, serviceAuth,callbackRequest);
        assertTrue(response.getData().containsKey("listWithoutNoticeHearingDetails"));
        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());
    }

    @Test
    public void shouldSeeHearingDetailswithoutListWithNotice() throws Exception {

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("listWithoutNoticeHearingDetails",listWithoutNoticeHearingDetails);


        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(
                ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails).build())
            .caseTypeOfApplication("FL401")
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

        when(hearingPrePopulateService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                                 .build()));
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController.prePopulateHearingPageData(authToken,serviceAuth,callbackRequest);
        assertTrue(response.getData().containsKey("listWithoutNoticeHearingDetails"));
    }


    @Test
    public void testListWithoutNoticeSubmission() throws Exception {

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(
                ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails).build())
            .caseTypeOfApplication("FL401")
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
            .tierOfJudiciary(TierOfJudiciaryEnum.districtJudge)
            .isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.judge)
            .build();
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        when(allocatedJudgeService.getAllocatedJudgeDetails(caseDataUpdated, caseData.getLegalAdviserList(), refDataUserService)).thenReturn(
            allocatedJudge);

        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);

        when(hearingPrePopulateService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                                 .build()));
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController
            .listWithoutNoticeSubmission(authToken,serviceAuth,callbackRequest);
        assertTrue(response.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

    @Test
    public void testExceptionForPrePopulateHearingPageData() throws Exception {
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("listWithoutNoticeHearingDetails",listWithoutNoticeHearingDetails);


        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(
                ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails).build())
            .caseTypeOfApplication("FL401")
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
            listWithoutNoticeController.prePopulateHearingPageData(authToken,serviceAuth,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForListWithoutNoticeSubmission() throws Exception {
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("listWithoutNoticeHearingDetails",listWithoutNoticeHearingDetails);


        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(
                ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails).build())
            .caseTypeOfApplication("FL401")
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
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("listWithoutNoticeHearingDetails",listWithoutNoticeHearingDetails);


        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(
                ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails).build())
            .caseTypeOfApplication("FL401")
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
    public void testListWithoutNoticeSubmission_CA() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .caseTypeOfApplication("C100")
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

        when(userService.getUserDetails(anyString())).thenReturn(
            UserDetails.builder().email("abc@test.com").build());

        when(addCaseNoteService.addCaseNoteDetails(any(CaseData.class), any(UserDetails.class))).thenReturn(List.of(element(
            CaseNoteDetails.builder().build())));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController
            .c100ListWithoutNoticeSubmission(authToken,serviceAuth,callbackRequest);
        assertTrue(response.getData().containsKey("caseNotes"));
    }

    @Test
    public void testExceptionForListWithoutNoticeSubmission_CA() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .caseTypeOfApplication("C100")
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
            listWithoutNoticeController.c100ListWithoutNoticeSubmission(authToken,serviceAuth,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForCcdSubmitted_CA() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .caseTypeOfApplication("C100")
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
            listWithoutNoticeController.c100CcdSubmitted(authToken,serviceAuth,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testForCcdSubmitted_CA() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingInstruction(
                "test").build())
            .caseTypeOfApplication("C100")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken, serviceAuth)).thenReturn(true);
        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse =
            listWithoutNoticeController.c100CcdSubmitted(authToken,serviceAuth,callbackRequest);
        assertEquals(CONFIRMATION_BODY_PREFIX_CA, submittedCallbackResponse.getBody().getConfirmationBody());
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}

