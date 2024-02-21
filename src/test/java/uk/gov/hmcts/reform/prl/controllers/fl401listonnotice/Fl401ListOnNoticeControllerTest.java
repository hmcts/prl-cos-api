package uk.gov.hmcts.reform.prl.controllers.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ListWithoutNoticeDetails;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.Fl401ListOnNotice;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.fl401listonnotice.Fl401ListOnNoticeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class Fl401ListOnNoticeControllerTest {

    @InjectMocks
    Fl401ListOnNoticeController fl401ListOnNoticeController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    Fl401ListOnNoticeService fl401ListOnNoticeService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private DocumentLanguageService documentLanguageService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    private CaseData caseData;
    private CallbackRequest callbackRequest;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Before
    public void setUp() {
        caseData = CaseData.builder()
            .courtName("testcourt")
            .caseTypeOfApplication(FL401_CASE_TYPE)
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

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    public void prepopulateHearingDetailsForFl401ListOnNotice() throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(YesOrNo.Yes)
                                                      .build())
            .fl401ListOnNotice(Fl401ListOnNotice.builder()
                                   .fl401ListOnNoticeDocument(Document.builder()
                                                                  .documentUrl(generatedDocumentInfo.getUrl())
                                                                  .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                                  .documentHash(generatedDocumentInfo.getHashToken())
                                                                  .documentFileName("fl404BFilename.pdf")
                                                                  .build())
                                   .build())
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

        when(fl401ListOnNoticeService.prePopulateHearingPageDataForFl401ListOnNotice(caseData))
            .thenReturn(stringObjectMap);
        AboutToStartOrSubmitCallbackResponse response = fl401ListOnNoticeController
            .prePopulateHearingPageDataForFl401ListOnNotice(authToken,s2sToken, callbackRequest);
        assertNotNull(response);

    }

    @Test
    public void shouldPrepopulateHearingDetailsListOnNotice() throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

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
        List<Element<HearingData>> listOnNoticeHearingDetails = Collections.singletonList(childElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("fl401ListOnNoticeHearingDetails",listOnNoticeHearingDetails);


        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(YesOrNo.Yes)
                                                      .build())
            .fl401ListOnNotice(Fl401ListOnNotice.builder()
                                    .fl401ListOnNoticeDocument(Document.builder()
                                                                   .documentUrl(generatedDocumentInfo.getUrl())
                                                                   .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                                   .documentHash(generatedDocumentInfo.getHashToken())
                                                                   .documentFileName("fl404BFilename.pdf")
                                                                   .build())
                                   .fl401ListOnNoticeHearingDetails(listOnNoticeHearingDetails)
                                    .build())
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
        when(fl401ListOnNoticeService.prePopulateHearingPageDataForFl401ListOnNotice(caseData))
            .thenReturn(stringObjectMap);
        AboutToStartOrSubmitCallbackResponse response = fl401ListOnNoticeController
            .prePopulateHearingPageDataForFl401ListOnNotice(authToken, s2sToken, callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testListOnNoticeSubmission() throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

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
        List<Element<HearingData>> listOnNoticeHearingDetails = Collections.singletonList(childElement);
        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.DISTRICT_JUDGE)
            .build();
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(YesOrNo.Yes)
                                                      .build())
            .fl401ListOnNotice(Fl401ListOnNotice.builder()
                                   .fl401ListOnNoticeDocument(Document.builder()
                                                                  .documentUrl(generatedDocumentInfo.getUrl())
                                                                  .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                                  .documentHash(generatedDocumentInfo.getHashToken())
                                                                  .documentFileName("fl404BFilename.pdf")
                                                                  .build())
                                   .fl401ListOnNoticeHearingDetails(listOnNoticeHearingDetails)
                                   .build())
            .allocatedJudge(allocatedJudge)
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

        when(fl401ListOnNoticeService.fl401ListOnNoticeSubmission(caseDetails, authToken))
            .thenReturn(stringObjectMap);
        AboutToStartOrSubmitCallbackResponse response = fl401ListOnNoticeController
            .fl401ListOnNoticeSubmission(authToken,s2sToken,callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testExceptionForFl401ListOnNoticeSubmission() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            fl401ListOnNoticeController.fl401ListOnNoticeSubmission(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForPrePopulateHearingPageDataForFl401ListOnNotice() throws Exception {

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
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(
                listWithoutNoticeHearingDetails).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            fl401ListOnNoticeController.prePopulateHearingPageDataForFl401ListOnNotice(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testSendListOnNoticeNotification() throws Exception {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(fl401ListOnNoticeService.sendNotification(anyMap(), anyString()))
            .thenReturn(ok(SubmittedCallbackResponse.builder()
                               .confirmationHeader("test")
                               .confirmationBody("test").build()));
        ResponseEntity<SubmittedCallbackResponse> response = fl401ListOnNoticeController
            .sendListOnNoticeNotification(authToken,s2sToken,callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testExceptionSendListOnNoticeNotification() throws Exception {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            fl401ListOnNoticeController.sendListOnNoticeNotification(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }
}
