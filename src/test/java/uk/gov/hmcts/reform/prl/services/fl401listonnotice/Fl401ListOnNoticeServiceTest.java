package uk.gov.hmcts.reform.prl.services.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.State;
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
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.Fl401ListOnNotice;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_LIST_ON_NOTICE_FL404B_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class Fl401ListOnNoticeServiceTest {

    @InjectMocks
    Fl401ListOnNoticeService fl401ListOnNoticeService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HearingDataService hearingDataService;

    public static final String authToken = "Bearer TestAuthToken";

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    AllocatedJudgeService allocatedJudgeService;

    @Mock
    private DocumentGenService documentGenService;

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
        when(hearingDataService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder().build()));
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));

        Map<String, Object> responseDataMap = fl401ListOnNoticeService
            .prePopulateHearingPageDataForFl401ListOnNotice(authToken, caseData);
        assertTrue(responseDataMap.containsKey("fl401ListOnNoticeHearingDetails"));
        assertTrue(responseDataMap.containsKey("legalAdviserList"));
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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
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
        when(hearingDataService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                          .build()));
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));

        Map<String, Object> responseDataMap = fl401ListOnNoticeService
            .prePopulateHearingPageDataForFl401ListOnNotice(authToken, caseData);
        assertTrue(responseDataMap.containsKey("fl401ListOnNoticeHearingDetails"));
    }

    @Test
    public void shouldGenerateFL404bDocForListOnNotice() throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

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
                                                                  .documentFileName("Fl404B_Document.pdf")
                                                                  .build())
                                   .build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("Fl404B_Document.pdf")
            .build();
        when(documentGenService.generateSingleDocument(authToken,caseData,DA_LIST_ON_NOTICE_FL404B_DOCUMENT,false)).thenReturn(document);
        when(hearingDataService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                          .build()));
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));

        Map<String, Object> responseDataMap = fl401ListOnNoticeService
            .generateFl404bDocument(authToken, caseData);
        assertTrue(responseDataMap.containsKey("fl401ListOnNoticeDocument"));
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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
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
            .state(State.JUDICIAL_REVIEW.getValue())
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        when(allocatedJudgeService.getAllocatedJudgeDetails(caseDataUpdated, caseData.getLegalAdviserList(), refDataUserService)).thenReturn(
            allocatedJudge);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        when(hearingDataService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                          .build()));
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));

        Map<String, Object> responseDataMap = fl401ListOnNoticeService
            .fl401ListOnNoticeSubmission(caseDetails);
        assertTrue(responseDataMap.containsKey("fl401ListOnNoticeHearingDetails"));

    }
}
