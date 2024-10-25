package uk.gov.hmcts.reform.prl.services.tab.summary.generators.refuge;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.refuge.RefugeCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.refuge.RefugeCaseGenerator;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RefugeCaseGeneratorTest {

    private final RefugeCaseGenerator generator = new RefugeCaseGenerator();


    @Test
    public void testGenerateForNoCaseType() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                .builder()
                .isRefugeCase(YesOrNo.No)
                .build())
            .build());
    }

    @Test
    public void testGenerateForC100NotRefuge() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .caseTypeOfApplication("C100")
                                                         .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                                                                               .builder()
                                                                               .isRefugeCase(YesOrNo.No)
                                                                               .build())
                                              .build());
    }

    @Test
    public void testGenerateForC100ApplicantLivesInReguge() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().value(PartyDetails.builder()
                .liveInRefuge(YesOrNo.Yes).build()).build()))
            .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                .builder()
                .isRefugeCase(YesOrNo.Yes)
                .build())
            .build());
    }

    @Test
    public void testGenerateForC100RespondentLivesInReguge() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(List.of(Element.<PartyDetails>builder().value(PartyDetails.builder()
                .liveInRefuge(YesOrNo.Yes).build()).build()))
            .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                .builder()
                .isRefugeCase(YesOrNo.Yes)
                .build())
            .build());
    }

    @Test
    public void testGenerateForC100OtherPartyLivesInReguge() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .caseTypeOfApplication("C100")
            .otherPartyInTheCaseRevised(List.of(Element.<PartyDetails>builder().value(PartyDetails.builder()
                .liveInRefuge(YesOrNo.Yes).build()).build()))
            .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                .builder()
                .isRefugeCase(YesOrNo.Yes)
                .build())
            .build());
    }

    @Test
    public void testGenerateForFL401NotRefuge() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .caseTypeOfApplication("FL401")
                                                         .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                                                                               .builder()
                                                                               .isRefugeCase(YesOrNo.No)
                                                                               .build())
                                              .build());
    }

    @Test
    public void testGenerateForFL401ApplicantRefuge() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .caseTypeOfApplication("FL401")
                .applicantsFL401(PartyDetails.builder().liveInRefuge(YesOrNo.Yes).build())
            .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                .builder()
                .isRefugeCase(YesOrNo.Yes)
                .build())
            .build());
    }

    @Test
    public void testGenerateForFL401RespondentRefuge() {
        CaseSummary caseSummary = generator.generate(CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(PartyDetails.builder().liveInRefuge(YesOrNo.Yes).build())
            .build());
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                .builder()
                .isRefugeCase(YesOrNo.Yes)
                .build())
            .build());
    }
}
