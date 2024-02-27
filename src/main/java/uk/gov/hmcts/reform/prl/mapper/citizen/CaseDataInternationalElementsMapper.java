package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildInternationalElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;

public class CaseDataInternationalElementsMapper {

    private CaseDataInternationalElementsMapper() {
    }

    public static void updateInternationalElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                              C100RebuildInternationalElements c100RebuildInternationalElements) {
        caseDataBuilder
                .habitualResidentInOtherState(buildHabitualResidentInOtherState(c100RebuildInternationalElements))
                .habitualResidentInOtherStateGiveReason(buildHabitualResidentInOtherStateReason(
                        c100RebuildInternationalElements))
                .jurisdictionIssue(c100RebuildInternationalElements.getDoesApplicationLinkedPeopleHaveInternationalOrder())
                .jurisdictionIssueGiveReason(c100RebuildInternationalElements
                        .getApplicationLinkedPeopleHaveInternationalOrderDetails())
                .requestToForeignAuthority(c100RebuildInternationalElements
                        .getHasAnotherCountryRequestedChildInformation())
                .requestToForeignAuthorityGiveReason(c100RebuildInternationalElements
                        .getAnotherCountryRequestedChildInformationDetails());
    }

    private static String buildHabitualResidentInOtherStateReason(C100RebuildInternationalElements c100RebuildInternationalElements) {
        return Stream.of(c100RebuildInternationalElements.getChildInternationalResidenceDetails(),
                        c100RebuildInternationalElements.getChildsParentHaveInternationalResidenceDetails())
                .filter(s -> nonNull(s) && !s.isEmpty())
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static YesOrNo buildHabitualResidentInOtherState(C100RebuildInternationalElements c100RebuildInternationalElements) {
        if (Yes.equals(c100RebuildInternationalElements.getDoChildHaveInternationalResidence())
                || Yes.equals(c100RebuildInternationalElements.getDoChildsParentHaveInternationalResidence())) {
            return Yes;
        } else {
            return No;
        }
    }

}
