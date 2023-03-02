package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;



import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.services.HearingDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ListWithoutNoticeControllerTest {

    @InjectMocks
    ListWithoutNoticeController listWithoutNoticeController;

    @Mock
    HearingDataService hearingPrePopulateService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuth = "serviceAuth";

    @Test
    public void shouldSeeHearingDetailswithoutListWithoutNotice() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
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

        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController.prePopulateHearingPageData(authToken,callbackRequest);
        assertNotNull(response.getData().containsKey("listWithoutNoticeHearingDetails"));
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
            .hearingChannelDynamicRadioList(dynamicList)
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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .mainApplicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("listWithoutNoticeHearingDetails",listWithoutNoticeHearingDetails);


        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails)
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


        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController.prePopulateHearingPageData(authToken,callbackRequest);
        assertNotNull(response.getData().containsKey("listWithoutNoticeHearingDetails"));
    }


    @Test
    public void testListWithoutNoticeSubmission() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
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
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("listWithoutNoticeHearingDetails",List.of(DynamicListElement.builder()
                                                                          .build()));

        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController.listWithoutNoticeSubmission(authToken,callbackRequest);
        assertNotNull(response.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

}

