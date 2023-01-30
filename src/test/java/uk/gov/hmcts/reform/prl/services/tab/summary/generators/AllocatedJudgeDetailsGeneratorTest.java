package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllocatedJudgeDetailsGenerator;

public class AllocatedJudgeDetailsGeneratorTest {

    private final AllocatedJudgeDetailsGenerator generator = new AllocatedJudgeDetailsGenerator();

    /*  @Test
    public void testGenerateWhenNoAllocatedJudgeDetails() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .courtName("Test Court")
            .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
            .allocatedJudgeDetails(AllocatedJudge.builder()
                .judgeTitle(" ").emailAddress(" ").lastName(" ")
                .courtName("Test Court")
                .build())
            .build());

    }

    @Test
    public void testGenerateWhenJudgeDetailsProvided() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                .courtName("Test Court")
                .allocatedJudge(uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge
                     .builder().isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.JUDGE).isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes)
                .judgeEmail("test1(test1@xxx.com)")
                    .judgeName("test1").build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
            .allocatedJudgeDetails(AllocatedJudge.builder().judgeTitle(" ").tierOfJudiciaryType(" ").emailAddress("test1@xxx.com")
                .lastName("test1")
                .courtName("Test Court")
                .build())
            .build());
    }

    @Test
    public void testGenerateWhenLegalAdvisorDetailsProvided() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .courtName("Test Court")
            .allocatedJudge(uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge
                .builder().isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.LEGAL_ADVISER).isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes)
                .judgeEmail("test1(test1@xxx.com)")
                .judgeName("test1").build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
            .allocatedJudgeDetails(AllocatedJudge.builder().judgeTitle(" ").tierOfJudiciaryType(" ").emailAddress("test2@xxx.com")
                .lastName("test2")
                .courtName("Test Court")
                .build())
            .build());
    }

    @Test
    public void testGenerateWhenTierOfJudiciaryDetailsProvided() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .courtName("Test Court")
            .allocatedJudge(uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge
                .builder().isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
                .tierOfJudiciary(TierOfJudiciaryEnum.CIRCUIT_JUDGE).build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
            .allocatedJudgeDetails(AllocatedJudge.builder().judgeTitle(" ").tierOfJudiciaryType(TierOfJudiciaryEnum.CIRCUIT_JUDGE.getDisplayedValue())
                .emailAddress(" ")
                .lastName(" ")
                .courtName("Test Court")
                .build())
            .build());
    }*/
}
