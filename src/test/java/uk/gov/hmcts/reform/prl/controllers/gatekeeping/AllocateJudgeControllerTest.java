package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

 /*
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class AllocateJudgeControllerTest {

    @InjectMocks
    private AllocateJudgeController allocateJudgeController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Test
    public void shouldSeeLegalAdvisorDetails() throws Exception {
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
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


        AboutToStartOrSubmitCallbackResponse response = allocateJudgeController.prePopulateLegalAdvisorDetails(
             callbackRequest);
        assertNotNull(response.getData().containsKey("legalAdvisorList"));
    }


    @Test
    public void shouldSeeAllocatedJudgeDetailsInSummaryTab() throws Exception {

        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.DISTRICT_JUDGE)
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
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
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);

        assertNotNull(allocateJudgeController.allocateJudge("Bearer:test","s2stoken",callbackRequest));

    }*/

//}
