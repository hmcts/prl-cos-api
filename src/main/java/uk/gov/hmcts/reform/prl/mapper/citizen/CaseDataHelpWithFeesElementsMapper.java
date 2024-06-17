package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHelpWithFeesElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;


public class CaseDataHelpWithFeesElementsMapper {

    private CaseDataHelpWithFeesElementsMapper() {
    }

    public static void updateHelpWithFeesDetailsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                            C100RebuildHelpWithFeesElements c100RebuildHelpWithFeesElements) {
        caseDataBuilder
                .helpWithFees(nonNull(c100RebuildHelpWithFeesElements.getNeedHelpWithFees())
                        ? c100RebuildHelpWithFeesElements.getNeedHelpWithFees() : No)
                .helpWithFeesNumber(nonNull(c100RebuildHelpWithFeesElements.getFeesAppliedDetails())
                        && YesOrNo.Yes.equals(c100RebuildHelpWithFeesElements.getFeesAppliedDetails())
                ? c100RebuildHelpWithFeesElements.getHelpWithFeesReferenceNumber() : null);
    }
}
