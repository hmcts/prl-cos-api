package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedingEmptyTable;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.OtherProceedingsGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

public class OtherProceedingsGeneratorTest {

    private final OtherProceedingsGenerator generator = new OtherProceedingsGenerator();

    @Test
    public void testGenerate() {
        ProceedingDetails proceedingDetails = ProceedingDetails.builder().build();
        Element<ProceedingDetails> wrappedProceedings = Element.<ProceedingDetails>builder().value(proceedingDetails).build();
        List<Element<ProceedingDetails>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .existingProceedings(listOfProceedings)
            .build();

        CaseSummary caseSummary = generator.generate(caseData);

        Element<OtherProceedings> wrappedOtherProceedings = Element.<OtherProceedings>builder()
            .value(OtherProceedings.builder().build()).build();
        List<Element<OtherProceedings>> otherProceedingList = Collections.singletonList(wrappedOtherProceedings);

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .otherProceedingsForSummaryTab(otherProceedingList)
                                              .otherProceedingEmptyTable(OtherProceedingEmptyTable.builder()
                                                                             .otherProceedingEmptyField("")
                                                                             .build())
                                                               .build());

    }

    @Test
    public void testIfProcedingsSelectedAsNo() {
        ProceedingDetails proceedingDetails = ProceedingDetails.builder().build();
        Element<ProceedingDetails> wrappedProceedings = Element.<ProceedingDetails>builder().value(proceedingDetails).build();
        List<Element<ProceedingDetails>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.no)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .existingProceedings(listOfProceedings)
            .build();

        CaseSummary caseSummary = generator.generate(caseData);

        ;
        Element<OtherProceedings> wrappedOtherProceedings = Element.<OtherProceedings>builder()
            .value(OtherProceedings.builder().build()).build();
        List<Element<OtherProceedings>> otherProceedingList = Collections.singletonList(wrappedOtherProceedings);

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .otherProceedingsForSummaryTab(otherProceedingList)
                                              .otherProceedingEmptyTable(OtherProceedingEmptyTable.builder()
                                                                             .otherProceedingEmptyField(" ")
                                                                             .build())
                                              .build());

    }

    @Test
    public void testWithOtherProceedings() {
        ProceedingDetails proceedingDetails = ProceedingDetails.builder().caseNumber("123")
            .nameOfCourt("Test Court").typeOfOrder(Arrays.asList(TypeOfOrderEnum.careOrder)).nameOfJudge("Test").build();
        Element<ProceedingDetails> wrappedProceedings = Element.<ProceedingDetails>builder().value(proceedingDetails).build();
        List<Element<ProceedingDetails>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .existingProceedings(listOfProceedings)
            .build();

        CaseSummary caseSummary = generator.generate(caseData);

        ;
        Element<OtherProceedings> wrappedOtherProceedings = Element.<OtherProceedings>builder()
            .value(OtherProceedings.builder().caseNumber("123").nameOfCourt("Test Court")
                       .typeOfOrder(TypeOfOrderEnum.careOrder.getDisplayedValue()).build()).build();
        List<Element<OtherProceedings>> otherProceedingList = Collections.singletonList(wrappedOtherProceedings);

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .otherProceedingsForSummaryTab(otherProceedingList)
                                              .otherProceedingEmptyTable(OtherProceedingEmptyTable.builder()
                                                                             .otherProceedingEmptyField("")
                                                                             .build())
                                              .build());

    }

    @Test
    public void testFl401WithOtherProceedings() {
        FL401Proceedings proceedingDetails = FL401Proceedings.builder().caseNumber("123")
            .nameOfCourt("Test Court").typeOfCase("case").build();
        Element<FL401Proceedings> wrappedProceedings = Element.<FL401Proceedings>builder().value(proceedingDetails).build();
        List<Element<FL401Proceedings>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes)
                                             .fl401OtherProceedings(listOfProceedings)
                                             .build())
            .build();

        CaseSummary caseSummary = generator.generate(caseData);


        Element<OtherProceedings> wrappedOtherProceedings = Element.<OtherProceedings>builder()
            .value(OtherProceedings.builder().caseNumber("123").nameOfCourt("Test Court")
                       .typeOfOrder("case").build()).build();
        List<Element<OtherProceedings>> otherProceedingList = Collections.singletonList(wrappedOtherProceedings);

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .otherProceedingsForSummaryTab(otherProceedingList)
                                              .otherProceedingEmptyTable(OtherProceedingEmptyTable.builder()
                                                                             .otherProceedingEmptyField("")
                                                                             .build())
                                              .build());

    }
}
