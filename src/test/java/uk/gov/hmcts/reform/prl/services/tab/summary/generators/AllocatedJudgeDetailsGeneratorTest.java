package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.AllocatedJudgeDetailsGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AllocatedJudgeDetailsGeneratorTest {

    private final AllocatedJudgeDetailsGenerator generator = new AllocatedJudgeDetailsGenerator();

    @Test
    public void testSummaryDetailsWhenJudgeDetailsProvided() {
        CaseSummary caseSummary = generator.generate(CaseData.builder().courtName("Test Court").allocatedJudge(
            uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge.builder().isJudgeOrLegalAdviser(
                    AllocatedJudgeTypeEnum.judge)
                .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes).judgeEmail("test1@xxx.com").judgeName("test1")
                .judgePersonalCode("1234").build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().allocatedJudgeDetails(
                AllocatedJudge.builder().tierOfJudiciaryType(" ").emailAddress("test1@xxx.com").lastName("test1")
                    .courtName("Test Court").judgePersonalCode("1234").isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.judge)
                    .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes).build())
            .build());
    }

    @Test
    public void testSummaryDetailsWhenLegalAdvisorDetailsProvided() {
        CaseSummary caseSummary = generator.generate(CaseData.builder().courtName("Test Court").allocatedJudge(
            uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge.builder().isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.legalAdviser)
                .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes)
                .legalAdviserList(DynamicList.builder().value(DynamicListElement.builder().code("test1").label("legalAdvisor1").build()).build())
                .build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().allocatedJudgeDetails(
                AllocatedJudge.builder().tierOfJudiciaryType(" ").emailAddress(" ").lastName(" ").courtName("Test Court")
                .isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.legalAdviser)
                .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes).build())
            .build());
    }

    @Test
    public void testGenerateWhenTierOfJudiciaryDetailsProvided() {
        CaseSummary caseSummary = generator.generate(CaseData.builder().courtName("Test Court").allocatedJudge(
            uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge.builder().isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
                .tierOfJudiciary(TierOfJudiciaryEnum.CIRCUIT_JUDGE).build()).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().allocatedJudgeDetails(
            AllocatedJudge.builder().tierOfJudiciaryType(TierOfJudiciaryEnum.CIRCUIT_JUDGE.getDisplayedValue())
                .emailAddress(" ").lastName(" ").isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
                .courtName("Test Court")
                .build()).build());
    }
}
