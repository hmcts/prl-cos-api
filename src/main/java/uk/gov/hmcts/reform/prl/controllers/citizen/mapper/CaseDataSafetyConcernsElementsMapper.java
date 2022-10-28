package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildSafetyConcernsElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

public class CaseDataSafetyConcernsElementsMapper {

    public static void updateSafetyConcernsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                        C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        caseDataBuilder
                .allegationOfHarm(buildAllegationOfHarm(c100RebuildSafetyConcernsElements));
    }

    private static AllegationOfHarm buildAllegationOfHarm(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {

        List<String> concernAboutChild = nonNull(c100RebuildSafetyConcernsElements.getConcernAboutChild())
                ? List.of(c100RebuildSafetyConcernsElements.getConcernAboutChild()) : Collections.emptyList();
        List<String> concernAboutApplicant = nonNull(c100RebuildSafetyConcernsElements.getConcernAboutApplicant())
                ? List.of(c100RebuildSafetyConcernsElements.getConcernAboutApplicant()) : Collections.emptyList();

        return AllegationOfHarm
                .builder()
                .allegationsOfHarmYesNo(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())
                .allegationsOfHarmDomesticAbuseYesNo(buildAllegationsOfHarmDomesticAbuseYesNo(concernAboutChild
                        , concernAboutApplicant))
                .physicalAbuseVictim(null)
                .emotionalAbuseVictim(null)
                .psychologicalAbuseVictim(null)
                .sexualAbuseVictim(null)
                .financialAbuseVictim(null)
                .build();

    }

    private static YesOrNo buildAllegationsOfHarmDomesticAbuseYesNo(List<String> concernAboutChild,
                                                                    List<String> concernAboutApplicant) {
        if (concernAboutChild.contains("witnessingDomesticAbuse")
                || concernAboutApplicant.contains("witnessingDomesticAbuse")) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }
}
